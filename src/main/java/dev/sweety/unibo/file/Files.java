package dev.sweety.unibo.file;

import dev.sweety.unibo.file.language.LanguageYml;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.java.JavaPlugin;

@UtilityClass
public class Files {

    public static LanguageYml LANGUAGE;
    public static PlayerStats PLAYER_ELO;
    public static GravePositions GRAVES;

    public void init(final JavaPlugin resource) {
        LANGUAGE = new LanguageYml(resource);
        PLAYER_ELO = new PlayerStats(resource);
        GRAVES = new GravePositions(resource);
    }

}
