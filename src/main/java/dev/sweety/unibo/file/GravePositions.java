package dev.sweety.unibo.file;


import dev.sweety.unibo.api.file.BukkitFile;
import dev.sweety.unibo.feature.grave.Grave;
import dev.sweety.unibo.feature.grave.GraveListener;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class GravePositions extends BukkitFile {

    public GravePositions(final JavaPlugin resource) {
        super(resource, "graves.yml", "data");
    }

    public void saveData() {
        final YamlConfiguration config = this.getConfig();

        for (Grave grave : GraveListener.graves.values()) {

            final String name = grave.name();

            config.set(name + ".location", grave.deathLocation().serialize());
            config.set(name + ".player.name", grave.playerName());

            ItemStack[] contents = grave.inventory().getContents();

            config.set(name + ".inventory.size", contents.length);

            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];

                if (item != null) config.set(name + ".inventory." + i, item);
            }

        }

        this.save();
    }

    public void loadData() {
        final YamlConfiguration config = this.getConfig();

        for (String name : config.getKeys(false)) {
            Location location = Location.deserialize(config.getConfigurationSection(name + ".location").getValues(false));

            ItemStack[] contents = new ItemStack[config.getInt(name + ".inventory.size", 36)];
            String playerName = config.getString(name + ".player.name", "");

            if (config.isConfigurationSection(name + ".inventory")) {
                for (String key : config.getConfigurationSection(name + ".inventory").getKeys(false)) {
                    int index = Integer.parseInt(key);
                    ItemStack item = config.getItemStack(name + ".inventory." + key);
                    contents[index] = item;
                }
            }

            UUID uuid = UUID.fromString(name.split("#")[0]);
            long time = Long.parseLong(name.split("#")[1], 16);

            Inventory inv = Bukkit.createInventory(null, 4 * 9, Component.text(playerName + "' grave"));

            inv.setContents(contents);

            Grave grave = new Grave(uuid, playerName, inv, location, time);
            Block block = location.getBlock();
            GraveListener.graves.put(block, grave);
        }
    }

}
