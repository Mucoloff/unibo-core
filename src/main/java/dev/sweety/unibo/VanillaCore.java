package dev.sweety.unibo;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import dev.sweety.core.logger.SimpleLogger;
import dev.sweety.core.logger.backend.SLF4JBackend;
import dev.sweety.core.thread.ProfileThread;
import dev.sweety.core.thread.ThreadManager;
import dev.sweety.record.annotations.RecordGetter;
import dev.sweety.unibo.api.VanillaAPI;
import dev.sweety.unibo.api.flag.FlagManager;
import dev.sweety.unibo.api.menu.MenuListener;
import dev.sweety.unibo.api.papi.StatsExpansion;
import dev.sweety.unibo.feature.discord.DiscordBot;
import dev.sweety.unibo.feature.essential.CommandRegistry;
import dev.sweety.unibo.feature.info.leaderboard.Leaderboard;
import dev.sweety.unibo.feature.inventory.Views;
import dev.sweety.unibo.feature.region.DefaultRegion;
import dev.sweety.unibo.feature.region.Region;
import dev.sweety.unibo.feature.region.RegionManager;
import dev.sweety.unibo.feature.region.listener.RegionBukkitListener;
import dev.sweety.unibo.feature.region.listener.RegionCreate;
import dev.sweety.unibo.player.MatchHandler;
import dev.sweety.unibo.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@RecordGetter(includeStatic = true)
public class VanillaCore implements VanillaCoreAccessors {

    private final ThreadManager threadManager = new ThreadManager();

    public static final SimpleLogger logger = new SimpleLogger("Vanilla");

    private final RegionManager regionManager;
    private final PlayerManager playerManager;
    private final Leaderboard leaderboard;
    private final MatchHandler matchHandler;
    private final CommandRegistry commandRegistry;
    private final DiscordBot discordBot = new DiscordBot(this);
    private final FlagManager flagManager = new FlagManager();

    private final StatsExpansion statsExpansion;
    private final ProfileThread thread;

    private final JavaPlugin instance;
    public VanillaCore(final JavaPlugin instance) {
        this.instance = instance;
        logger.setBackend(new SLF4JBackend(instance.getSLF4JLogger()));
        this.regionManager = new RegionManager(this);
        this.playerManager = new PlayerManager(this);
        this.matchHandler = new MatchHandler(this);
        this.leaderboard = new Leaderboard(this);
        this.commandRegistry = new CommandRegistry(this);
        this.statsExpansion = new StatsExpansion(this.playerManager);
        this.thread = this.threadManager.getAvailableProfileThread();
    }

    public void load() {
        List.of(
                Region.class,
                DefaultRegion.class
        ).forEach(ConfigurationSerialization::registerClass);
    }

    public void enable() {
        VanillaAPI.init(this);

        this.regionManager.init();
        this.playerManager.register();

        this.leaderboard.start();

        this.statsExpansion.register();

        registerEvents(
                new RegionBukkitListener(this),
                new RegionCreate(this),
                new MenuListener(),
                new Views.Handler()
        );

        this.commandRegistry.register();

        this.discordBot.start();

    }

    public void disable() {
        this.discordBot.shutdown();
        this.statsExpansion.unregister();
        this.regionManager.shutdown();
        this.playerManager.shutdown();
        this.threadManager.shutdown();
    }

    public void registerEvent(final Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, this.instance);
    }

    public void registerEvents(final Listener... listeners) {
        final PluginManager manager = Bukkit.getServer().getPluginManager();
        for (Listener listener : listeners) manager.registerEvents(listener, this.instance);
    }

    public void registerPacket(final PacketListener listener, final PacketListenerPriority priority) {
        PacketEvents.getAPI().getEventManager().registerListener(listener, priority);
    }

    public FileConfiguration config() {
        return this.instance.getConfig();
    }

    public String name() {
        return this.instance.getName();
    }

}
