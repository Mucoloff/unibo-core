package dev.sweety.unibo.file;


import dev.sweety.unibo.api.file.BukkitFile;
import dev.sweety.unibo.feature.info.Stats;
import dev.sweety.unibo.player.VanillaPlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PlayerStats extends BukkitFile {

    public PlayerStats(final JavaPlugin resource) {
        super(resource, "player-stats.yml", "data");
    }

    public void save(final UUID uuid, final Stats stats) {
        final YamlConfiguration config = this.getConfig();

        this.setStats(uuid, stats, config);
        this.save();
    }

    private void setStats(UUID uuid, Stats stats, YamlConfiguration config) {
        final String id = uuid.toString();
        config.set(id + ".elo", stats.getElo());
        config.set(id + ".wins", stats.getWins());
        config.set(id + ".losses", stats.getLosses());
        config.set(id + ".winStreak", stats.getWinStreak());
        config.set(id + ".loseStreak", stats.getLoseStreak());
        config.set(id + ".combat", stats.isCombat());
    }

    public Stats load(UUID uuid) {
        final String id = uuid.toString();
        final YamlConfiguration config = this.getConfig();
        return new Stats(
                config.getDouble(id + ".elo", 0),
                config.getInt(id + ".wins", 0),
                config.getInt(id + ".losses", 0),
                config.getInt(id + ".winStreak", 0),
                config.getInt(id + ".loseStreak", 0),
                config.getBoolean(id + ".combat", true)
        );
    }

    public void save(final VanillaPlayer profile) {
        this.save(profile.player().getUniqueId(), profile.stats());
    }

    public void save(final VanillaPlayer... profiles) {
        final YamlConfiguration config = this.getConfig();

        for (VanillaPlayer profile : profiles) setStats(profile.player().getUniqueId(), profile.stats(), config);

        this.save();
    }
}
