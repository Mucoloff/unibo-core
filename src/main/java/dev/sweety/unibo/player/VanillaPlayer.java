package dev.sweety.unibo.player;

import dev.sweety.core.thread.ProfileThread;
import dev.sweety.core.thread.ThreadManager;
import dev.sweety.record.annotations.DataIgnore;
import dev.sweety.record.annotations.RecordData;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.VanillaAPI;
import dev.sweety.unibo.api.flag.FlagType;
import dev.sweety.unibo.api.packet.Packet;
import dev.sweety.unibo.api.packet.PacketHandler;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.google.common.base.Function;
import dev.sweety.unibo.feature.home.Homes;
import dev.sweety.unibo.feature.info.Stats;
import dev.sweety.unibo.feature.region.Region;
import dev.sweety.unibo.file.Files;
import dev.sweety.unibo.player.features.ChatProcessor;
import dev.sweety.unibo.player.features.combat.CombatLogProcessor;
import dev.sweety.unibo.player.features.combat.CombatStatus;
import dev.sweety.unibo.player.features.teleport.TpaProcessor;
import dev.sweety.unibo.player.processors.AttackProcessor;
import dev.sweety.unibo.player.processors.DamageProcessor;
import dev.sweety.unibo.player.processors.PositionProcessor;
import dev.sweety.unibo.player.processors.RegionProcessor;
import dev.sweety.unibo.utils.McUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RecordData
public class VanillaPlayer implements PacketHandler, VanillaPlayerAccessors {

    private final UUID uuid;
    protected final User user;
    private final int entityId;
    protected final Player player;
    protected final ProfileThread profileThread;

    //util processors
    private final AttackProcessor attackProcessor;
    private final DamageProcessor damageProcessor;
    private final PositionProcessor positionProcessor;

    //features
    private final TpaProcessor tpaProcessor;
    private final ChatProcessor chatProcessor;
    private final RegionProcessor regionProcessor;
    private final CombatLogProcessor combatLogProcessor;

    @DataIgnore
    private final AtomicInteger retainCount = new AtomicInteger(1);

    @DataIgnore
    private final AtomicReference<String> regionName, lastRegionName;

    @DataIgnore
    private final Runnable shutdown, removal;

    private final Stats stats;
    private final Homes homes;

    public VanillaPlayer(final Player player, final User user, final VanillaCore plugin) {
        this.entityId = (this.player = player).getEntityId();
        this.uuid = (this.user = user).getUUID();

        final ThreadManager threadManager = plugin.threadManager();
        this.profileThread = threadManager.getAvailableProfileThread();
        this.shutdown = () -> threadManager.shutdown(profileThread);
        this.removal = () -> plugin.playerManager().finalizeRemoval(this);

        this.stats = Files.PLAYER_STATS.load(player.getUniqueId());
        this.homes = Files.PLAYER_HOMES.load(player.getUniqueId());

        this.attackProcessor = new AttackProcessor(this, plugin);
        this.damageProcessor = new DamageProcessor(this, plugin);
        this.positionProcessor = new PositionProcessor(this, plugin);

        this.tpaProcessor = new TpaProcessor(this, plugin);
        this.chatProcessor = new ChatProcessor(this, plugin);
        this.regionProcessor = new RegionProcessor(this, plugin);
        this.combatLogProcessor = new CombatLogProcessor(this, plugin);

        this.regionName = new AtomicReference<>();
        this.lastRegionName = new AtomicReference<>();
    }

    @Override
    public void handle(final Packet packet) {
        this.positionProcessor.handle(packet);
        this.attackProcessor.handle(packet);
        this.damageProcessor.handle(packet);

        this.regionProcessor.handle(packet);

        this.combatLogProcessor.handle(packet);
        this.tpaProcessor.handle(packet);

        this.chatProcessor.handle(packet);
    }

    public void retain() {
        this.retainCount.incrementAndGet();
    }

    public void release() {
        if (this.retainCount.decrementAndGet() == 0) this.removal.run();
    }

    // actions executed on quit that requires the player to be still loaded in playerManager
    public void quit() {
        this.combatLogProcessor.quit();
        this.tpaProcessor.quit();
    }

    public void shutdown() {
        this.shutdown.run();
        Files.PLAYER_STATS.save(this);
    }

    public World world() {
        return this.player.getWorld();
    }

    public String worldName() {
        return this.player.getWorld().getName();
    }

    public String name() {
        return this.player.getName();
    }

    public void writePacket(final PacketWrapper<?> wrapper) {
        this.user.writePacket(wrapper);
    }

    public void sendPacketSilently(final PacketWrapper<?> wrapper) {
        this.user.sendPacketSilently(wrapper);
    }

    public void receiveSilently(final PacketWrapper<?> wrapper) {
        this.user.receivePacketSilently(wrapper);
    }

    public void receivePacket(final PacketWrapper<?> wrapper) {
        this.user.receivePacket(wrapper);
    }

    public Vector3d position() {
        return this.positionProcessor.position();
    }

    //todo remove all this shit
    // ex use insteag getPositionProcessor().getPosition()

    public void tag(final Player victim) {
        this.combatLogProcessor.tag(victim);
    }

    public void tag(final VanillaPlayer victim) {
        this.combatLogProcessor.tag(victim);
    }

    public void removeCombat() {
        this.combatLogProcessor.clear();
    }

    public void setBack() {
        this.positionProcessor.setBack();
    }

    public void region(@Nullable Region previous) {
        this.lastRegionName.set(regionName.get());
        this.regionName.set(previous == null ? null : previous.getName());
    }

    public double distance(Vector3d position) {
        return this.position().distance(position);
    }

    public boolean exempt(FlagType flag) {
        return exempt(flag.getName());
    }

    public boolean exempt(String flagName) {
        return this.regionName.get() == null || VanillaAPI.getRegion(this.regionName.get()).isExempt(this.player.getName(), flagName);
    }

    public Region region() {
        return VanillaAPI.getRegion(regionName.get());
    }

    public Region lastRegion() {
        return VanillaAPI.getRegion(lastRegionName.get());
    }

    public String regionName() {
        return this.regionName.get();
    }

    public String lastRegionName() {
        return this.lastRegionName.get();
    }

    public boolean exempt(final Region region, final FlagType flag) {
        return exempt(region, flag.getName());
    }

    public boolean exempt(final Region region, final String flagName) {
        return region.isExempt(this.player.getName(), flagName);
    }

    public void sendMessage(final String msg) {
        this.player.sendMessage(McUtils.component(msg));
    }

    public void reloadStats(Function<UUID, Stats> load) {
        stats.apply(load.apply(player.getUniqueId()));
    }

    public void reloadHomes(Function<UUID, Homes> load) {
        homes.apply(load.apply(player.getUniqueId()));
    }

    public CombatStatus combatStatus() {
        return this.combatLogProcessor.getCombatStatus().get();
    }

    public void combatStatus(CombatStatus combatStatus) {
        this.combatLogProcessor.getCombatStatus().set(combatStatus);
    }
}
