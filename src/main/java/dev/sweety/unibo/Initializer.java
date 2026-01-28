package dev.sweety.unibo;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import dev.sweety.unibo.file.Files;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Initializer extends JavaPlugin {

    private static VanillaCore PLUGIN = null;

    @Override
    public void onLoad() {
        PLUGIN = new VanillaCore(this);
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this, new PacketEventsSettings().checkForUpdates(false)));
        PacketEvents.getAPI().load();
        PLUGIN.load();
    }

    @Override
    public void onEnable() {
        Files.init(this);

        saveDefaultConfig();
        reloadConfig();

        PLUGIN.enable();
        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
        PLUGIN.disable();
        PacketEvents.getAPI().terminate();
    }

    public static VanillaCore plugin() {
        if (PLUGIN == null) throw new IllegalStateException("Plugin not loaded yet!");
        return PLUGIN;
    }


}