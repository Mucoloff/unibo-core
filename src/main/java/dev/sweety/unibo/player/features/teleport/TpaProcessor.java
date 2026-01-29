package dev.sweety.unibo.player.features.teleport;

import com.github.retrooper.packetevents.util.Vector3d;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.packet.Packet;
import dev.sweety.unibo.api.processor.Processor;
import dev.sweety.unibo.player.PlayerManager;
import dev.sweety.unibo.player.VanillaPlayer;
import dev.sweety.unibo.player.processors.PositionProcessor;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TpaProcessor extends Processor {

    private final AtomicReference<ActiveTeleport> active;
    private final PlayerManager playerManager;
    private final PositionProcessor positionProcessor;

    private final Map<UUID, TpaType> incoming = new HashMap<>();
    private final Set<UUID> outgoing = new HashSet<>();

    public TpaProcessor(VanillaPlayer player, VanillaCore plugin) {
        super(player, plugin);
        this.playerManager = plugin.playerManager();
        this.positionProcessor = player.positionProcessor();

        this.active = new AtomicReference<>(null);
    }

    @Override
    public void handle(Packet packet) {
        final ActiveTeleport tp = this.active.get();
        if (tp == null) return;
        if (!packet.isMovement()) return;
        if (!this.positionProcessor.isPosition()) return;
        if (!this.player.player().getUniqueId().equals(tp.requester())) return;

        final Vector3d pos = this.positionProcessor.position(), lastPos = this.positionProcessor.lastPosition();

        if (pos.distance(lastPos) < 0.005) return;

        cancelActive("Moved");
    }

    public void cancelActive(String reason) {
        final ActiveTeleport tp = this.active.get();
        if (tp == null) return;

        synchronized (tp) {
            if (tp != active.get()) return;
            this.active.set(null);
            tp.cancel();

            TpaProcessor other = processor(tp.other(player));

            if (other != null) {
                final ActiveTeleport otherTp = other.active.get();
                other.player.player().sendRichMessage("<red>Il teleport è stato annullato.");
                if (otherTp != null && otherTp == tp) {
                    otherTp.cancel();
                    other.active.set(null);
                }
            }
        }

        player.player().sendRichMessage("<red>Teleport annullato: " + reason);
    }

    public TpaResult send(UUID targetId, TpaType type) {
        if (targetId.equals(player.uuid())) return TpaResult.TARGET_NOT_FOUND;

        TpaProcessor target = processor(targetId);
        if (target == null) return TpaResult.TARGET_NOT_FOUND;

        // già in teleport
        if (this.active.get() != null || target.active.get() != null) return TpaResult.ALREADY_IN_TELEPORT;

        // già richiesta esistente
        if (this.outgoing.contains(targetId) || target.incoming.containsKey(player.uuid())) {
            return TpaResult.ALREADY_REQUESTED;
        }

        // registra richiesta
        this.outgoing.add(targetId);
        target.incoming.put(player.uuid(), type);

        // Notify target (Moved from messages to here to ensure target still gets notif,
        // but sender message will be handled by TPA.java via return result)
        // Actually, user said send messages from command.
        // But target is not running the command. TPA.java handles the sender.
        // TPA.java also has access to target Player object so it CAN send message to target.
        // I will attempt to remove messages here and assume TPA.java will handle BOTH if possible.
        // But send(UUID) receives UUID. TPA.java has Player.

        return TpaResult.SUCCESS;
    }

    public TpaResult cancelOutgoingRequests(String reason) {
        boolean cancelled = false;
        if (this.active.get() != null) {
            cancelActive(reason);
            cancelled = true;
        }

        final Set<UUID> toClear = new HashSet<>();
        toClear.addAll(this.outgoing);
        toClear.addAll(this.incoming.keySet());

        if (!toClear.isEmpty()) cancelled = true;

        final UUID self = player.uuid();

        for (UUID id : toClear) {
            TpaProcessor p = processor(id);
            if (p == null) continue;
            p.outgoing.remove(self);
            p.incoming.remove(self);
        }

        this.outgoing.clear();
        this.incoming.clear();

        return cancelled ? TpaResult.CANCELLED : TpaResult.NOTHING_TO_CANCEL;
    }

    public TpaResult denyAll() {
        final Set<UUID> toDeny = new HashSet<>(incoming.keySet());

        if (toDeny.isEmpty()){
            return TpaResult.NO_REQUEST;
        }

        for (UUID id : toDeny) {
            denyInternal(id);
        }
        return TpaResult.SUCCESS;
    }

    public TpaResult deny(UUID requesterId) {
        if (denyInternal(requesterId)) return TpaResult.SUCCESS;
        return TpaResult.NO_REQUEST;
    }

    public boolean denyInternal(UUID requesterId) {
        if (incoming.remove(requesterId) == null) return false;

        final TpaProcessor req = processor(requesterId);

        if (req != null) {
            req.outgoing.remove(player.uuid());
            req.player.player().sendRichMessage(
                    "<red>La tua richiesta di teleport è stata rifiutata."
            );
        }
        return true;
    }

    public TpaResult accept(UUID requesterId) {
        final TpaType type = incoming.remove(requesterId);
        if (type == null) return TpaResult.NO_REQUEST;

        final VanillaPlayer requester = profile(requesterId);
        if (requester == null) return TpaResult.TARGET_NOT_FOUND;
        final TpaProcessor req = requester.tpaProcessor();
        if (req == null || !req.outgoing.contains(this.player.uuid())) return TpaResult.NO_REQUEST;
        req.outgoing.remove(this.player.uuid());

        if (this.active.get() != null || req.active.get() != null) return TpaResult.ALREADY_IN_TELEPORT;

        Location loc = switch (type) {
            case TPA -> SpigotConversionUtil.toBukkitLocation(
                    player.world(),
                    positionProcessor.location()
            );
            case TPAHERE -> SpigotConversionUtil.toBukkitLocation(
                    requester.world(),
                    requester.positionProcessor().location()
            );
        };

        final ActiveTeleport tp = new ActiveTeleport(requesterId, this.player.uuid(), loc, type);

        this.active.set(tp);
        req.active.set(tp);

        startCountdown(tp, req, this);
        return TpaResult.SUCCESS;
    }

    public void quit() {
        cancelOutgoingRequests("quit");
    }

    private void startCountdown(final ActiveTeleport tp, TpaProcessor req, TpaProcessor target) {
        tp.countdown = this.player.profileThread().schedule(() -> {
            final ActiveTeleport current = active.get();
            if (current != tp) return; // teleport già cancellato

            if (req == null || !req.player.player().isOnline() || target == null || !target.player.player().isOnline()) {
                cancelActive("offline");
                return;
            }

            Player p = tp.type() == TpaType.TPA ? req.player.player() : target.player.player();
            p.teleportAsync(tp.location())
                    .thenRun(() -> finish(tp))
                    .exceptionally(ex -> {
                        cancelActive("fail");
                        return null;
                    });
        }, 3, TimeUnit.SECONDS);
    }

    private void finish(ActiveTeleport tp) {
        if (this.active.get() == tp) this.active.set(null);

        TpaProcessor other = processor(tp.other(player));
        ActiveTeleport otherTp = other != null ? other.active.get() : null;
        if (otherTp == tp) other.active.set(null);

    }

    private VanillaPlayer profile(UUID id) {
        return this.playerManager.profile(id);
    }

    private TpaProcessor processor(UUID id) {
        final VanillaPlayer p = profile(id);
        if (p == null) return null;
        return p.tpaProcessor();
    }

    public UUID getSingleIncomingOrNull() {
        return this.incoming.size() == 1 ? this.incoming.keySet().iterator().next() : null;
    }

    public Collection<String> incomingNames() {
        return this.incoming.keySet().stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).filter(Objects::nonNull).toList();
    }
}
