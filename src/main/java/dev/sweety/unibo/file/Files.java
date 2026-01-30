package dev.sweety.unibo.file;

import dev.sweety.unibo.file.language.LanguageYml;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.java.JavaPlugin;

@UtilityClass
public class Files {

    public static LanguageYml LANGUAGE;
    public static GravePositions GRAVES;
    public static PlayerStats PLAYER_STATS;
    public static PlayerHomes PLAYER_HOMES;

    public void init(final JavaPlugin resource) {
        LANGUAGE = new LanguageYml(resource);
        GRAVES = new GravePositions(resource);
        PLAYER_STATS = new PlayerStats(resource);
        PLAYER_HOMES = new PlayerHomes(resource);
    }

    public static void reload() {
        LANGUAGE.reload();
        GRAVES.reload();
        PLAYER_STATS.reload();
        PLAYER_HOMES.reload();
    }
}
