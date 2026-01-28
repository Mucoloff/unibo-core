package dev.sweety.unibo.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import dev.sweety.core.math.MathUtils;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.packet.Packet;
import dev.sweety.unibo.file.language.Language;
import dev.sweety.unibo.utils.SoundUtils;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
public class PlayerManager implements Listener, PacketListener {

    protected final VanillaCore plugin;
    private final Map<UUID, VanillaPlayer> profiles = new HashMap<>();

    public PlayerManager(final VanillaCore plugin) {
        this.plugin = plugin;
    }

    public void register() {
        this.plugin.registerPacket(this, PacketListenerPriority.HIGHEST);
        this.plugin.registerEvent(this);
    }

    public VanillaPlayer createProfile(final Player player, final User user) {
        final UUID uuid = player.getUniqueId();
        if (this.profiles.containsKey(uuid)) {
            return this.profiles.get(uuid);
        }
        final VanillaPlayer profile = new VanillaPlayer(player, user, this.plugin);
        this.profiles.put(uuid, profile);
        return profile;
    }

    public VanillaPlayer removeProfile(final UUID player) {
        return this.profiles.remove(player);
    }

    public VanillaPlayer removeProfile(final Player player) {
        return this.profiles.remove(player.getUniqueId());
    }

    public VanillaPlayer getProfile(final UUID uuid) {
        return this.profiles.get(uuid);
    }

    public VanillaPlayer getProfile(final Player player) {
        return this.profiles.get(player.getUniqueId());
    }

    public void foreachProfile(final Consumer<VanillaPlayer> action) {
        MathUtils.parallel(this.profiles.values()).forEach(action);
    }

    public List<VanillaPlayer> getNearbyPlayers(final VanillaPlayer player, double radius) {
        return this.profiles.values().stream()
                .filter(p -> player.entityId() != p.entityId())
                .filter(p -> p.worldName().equals(player.worldName()))
                .filter(p -> !(player.distance(p.position()) > radius))
                .toList();
    }

    public void shutdown() {
        this.foreachProfile(VanillaPlayer::shutdown);
    }

    public void writePacket(final PacketWrapper<?> wrap) {
        foreachProfile(profile -> profile.writePacket(wrap));
    }

    public void writePackets(final PacketWrapper<?>... wraps) {
        foreachProfile(profile -> {
            for (PacketWrapper<?> wrap : wraps) {
                profile.writePacket(wrap);
            }
        });
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        final UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;
        final VanillaPlayer profile = this.getProfile(uuid);
        if (profile == null) return;
        profile.handle(new Packet(event));
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        final UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;
        final VanillaPlayer profile = this.getProfile(uuid);
        if (profile == null) return;
        profile.handle(new Packet(event));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        final User user = PacketEvents.getAPI().getPlayerManager().getUser(player);

        final VanillaPlayer profile = this.createProfile(player, user);

        SoundUtils.playSound(user, Sounds.ENTITY_FIREWORK_ROCKET_LAUNCH);
        SoundUtils.playSound(user, Sounds.ENTITY_FIREWORK_ROCKET_BLAST);

        e.joinMessage(Language.JOIN__MESSAGE.component("{player}", player.getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(final PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        e.quitMessage(Language.QUIT__MESSAGE.component("{player}", player.getName()));
        final VanillaPlayer profile = this.removeProfile(player.getUniqueId());
        if (profile == null) return;
        profile.shutdown();
    }
}
