package dev.sweety.unibo.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import dev.sweety.core.math.MathUtils;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.VanillaCoreAccessors;
import dev.sweety.unibo.api.packet.Packet;
import dev.sweety.unibo.feature.essential.Spawn;
import dev.sweety.unibo.file.language.Language;
import dev.sweety.unibo.utils.SoundUtils;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.SpawnChangeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    public VanillaPlayer create(final Player player, final User user) {
        final UUID uuid = player.getUniqueId();
        if (this.profiles.containsKey(uuid)) {
            return this.profiles.get(uuid);
        }
        final VanillaPlayer profile = new VanillaPlayer(player, user, this.plugin);
        this.profiles.put(uuid, profile);
        return profile;
    }

    public void finalizeRemoval(final VanillaPlayer player) {
        this.profiles.remove(player.user().getUUID());
        player.shutdown();
    }

    public VanillaPlayer remove(final UUID player) {
        final VanillaPlayer profile = this.profiles.get(player);
        if (profile != null) profile.release();
        return profile;
    }

    public VanillaPlayer remove(final Player player) {
        return this.remove(player.getUniqueId());
    }

    public VanillaPlayer profile(final UUID uuid) {
        return this.profiles.get(uuid);
    }

    public VanillaPlayer profile(final Player player) {
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
        final User user = event.getUser();
        final VanillaPlayer profile = this.profile(user.getUUID());
        if (profile == null) return;
        final Packet packet = new Packet(event);

        profile.handle(packet);
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        final User user = event.getUser();
        final VanillaPlayer profile;
        profile = this.profile(user.getUUID());
        if (profile == null) return;
        final Packet packet = new Packet(event);

        profile.handle(packet);
    }

    private VanillaPlayer login(Player player, User user) {
        final VanillaPlayer profile = this.create(player, user);
        SoundUtils.playSound(user, Sounds.ENTITY_FIREWORK_ROCKET_LAUNCH);
        SoundUtils.playSound(user, Sounds.ENTITY_FIREWORK_ROCKET_BLAST);
        return profile;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        final User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        this.login(player, user);
        e.joinMessage(Language.JOIN__MESSAGE.component("%player%", player.getName()));

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFirsJoin(final AsyncPlayerSpawnLocationEvent e){
        if (e.isNewPlayer()) e.setSpawnLocation(Spawn.spawn());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRespawn(final PlayerRespawnEvent e) {
        e.setRespawnLocation(Spawn.spawn());
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        final User user = event.getUser();

        final VanillaPlayer profile = this.profile(user.getUUID());

        if (profile == null) return;

        profile.combatLogProcessor().quit();

        profile.release();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(final PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        e.quitMessage(Language.QUIT__MESSAGE.component("%player%", player.getName()));
    }

}
