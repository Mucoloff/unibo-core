package dev.sweety.unibo.api;

import com.github.retrooper.packetevents.util.Vector3d;
import dev.sweety.core.thread.ProfileThread;
import dev.sweety.core.thread.ThreadManager;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.flag.Flag;
import dev.sweety.unibo.api.flag.FlagManager;
import dev.sweety.unibo.api.papi.StatsExpansion;
import dev.sweety.unibo.feature.discord.DiscordBot;
import dev.sweety.unibo.feature.essential.CommandRegistry;
import dev.sweety.unibo.feature.essential.Spawn;
import dev.sweety.unibo.feature.info.leaderboard.Leaderboard;
import dev.sweety.unibo.feature.region.Region;
import dev.sweety.unibo.feature.region.RegionManager;
import dev.sweety.unibo.player.MatchHandler;
import dev.sweety.unibo.player.PlayerManager;
import dev.sweety.unibo.player.VanillaPlayer;
import lombok.experimental.UtilityClass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@UtilityClass
public final class VanillaAPI {

    private VanillaCore plugin;

    public void init(VanillaCore plugin) {
        VanillaAPI.plugin = plugin;
    }

    public VanillaCore plugin() {
        if (plugin == null)
            throw new IllegalStateException("VanillaAPI not initialized");
        return plugin;
    }

    public LuckPerms luckperms() {
        return LuckPermsProvider.get();
    }

    public ThreadManager threadManager() {
        return plugin().threadManager();
    }


    // Region Management
    public RegionManager regionManager() {
        return plugin().regionManager();
    }

    public Collection<Region> regions() {
        return regionManager().values();
    }

    public @NotNull Region getRegionFromLocation(final String worldName, final Vector3d position) {
        return regionManager().getRegionFromLocation(worldName, position);
    }

    public void addRegion(final Region region) {
        regionManager().add(region);
    }

    public void removeRegion(final Region region) {
        regionManager().delete(region);
    }

    public Region getRegion(final String name) {
        return regionManager().get(name);
    }

    public Collection<Region> getRegions() {
        return regionManager().values();
    }

    public Set<String> getRegionNames() {
        return regionManager().getNames();
    }

    public FlagManager flagManager() {
        return plugin().flagManager();
    }

    // Flag Management
    public void addFlag(final Flag<?> flag) {
        flagManager().add(flag);
    }

    public void removeFlag(final Flag<?> flag) {
        flagManager().remove(flag);
    }

    public Flag<?> getFlag(final String flagName) {
        return flagManager().get(flagName);
    }

    public List<Flag<?>> getFlags() {
        return flagManager().getFlags();
    }

    public List<String> getFlagNames() {
        return flagManager().getNames();
    }

    // Player Management
    public PlayerManager playerManager() {
        return plugin().playerManager();
    }

    public VanillaPlayer getPlayer(final UUID uuid) {
        return playerManager().getProfile(uuid);
    }


    public Leaderboard leaderboard() {
        return plugin().leaderboard();
    }

    public MatchHandler matchHandler() {
        return plugin().matchHandler();
    }

    public CommandRegistry commandRegistry() {
        return plugin().commandRegistry();
    }

    public DiscordBot discordBot() {
        return plugin().discordBot();
    }

    public StatsExpansion statsExpansion() {
        return plugin().statsExpansion();
    }

    public ProfileThread thread() {
        return plugin().thread();
    }

    public JavaPlugin instance() {
        return plugin().instance();
    }

    public FileConfiguration config() {
        return plugin().config();
    }

    public void spawn(final Player... player) {
        Spawn.spawn(player);
    }

    public void tagCombat(Player player1, Player player2) {
        final PlayerManager manager = playerManager();
        final VanillaPlayer profile1 = manager.getProfile(player1.getUniqueId());
        final VanillaPlayer profile2 = manager.getProfile(player2.getUniqueId());
        profile1.tag(profile2);
    }
}
