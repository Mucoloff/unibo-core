package dev.sweety.unibo.file;

import dev.sweety.unibo.api.file.BukkitFile;
import dev.sweety.unibo.api.flag.impl.PositionFlag;
import dev.sweety.unibo.api.serializable.Position;
import dev.sweety.unibo.feature.home.Homes;
import dev.sweety.unibo.player.VanillaPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerHomes extends BukkitFile {

    public PlayerHomes(final JavaPlugin resource) {
        super(resource, "player-homes.yml", "data");
    }

    public void save(final UUID uuid, final Homes homes) {
        final YamlConfiguration config = this.getConfig();

        this.setHomes(uuid, homes, config);
        this.save();
    }

    private void setHomes(UUID uuid, Homes homes, YamlConfiguration config) {
        final String id = uuid.toString();
        config.set(id + ".homes", null); // Clear previous homes
        for (Map.Entry<String, Position> entry : homes.getHomes().entrySet()) {
            config.set(id + ".homes." + entry.getKey(), entry.getValue().serializeString());
        }
    }

    public Homes load(UUID uuid) {
        final String id = uuid.toString();
        final YamlConfiguration config = this.getConfig();

        final int count = getPlugin().getConfig().getInt("homes-count", 3);

        final Map<String, Position> homeMap = new HashMap<>(count);

        final ConfigurationSection section = config.getConfigurationSection(id + ".homes");
        if (section != null)
            for (String name : section.getKeys(false)) {
                final String stringPosition = config.getString(id + ".homes." + name);
                if (stringPosition != null) {
                    Position position = Position.deserializeString(stringPosition);
                    homeMap.put(name, position);
                }
            }

        return new Homes(homeMap, count);
    }

    public void save(final VanillaPlayer profile) {
        this.save(profile.player().getUniqueId(), profile.homes());
    }

    public void save(final VanillaPlayer... profiles) {
        final YamlConfiguration config = this.getConfig();

        for (VanillaPlayer profile : profiles) setHomes(profile.player().getUniqueId(), profile.homes(), config);

        this.save();
    }

}
