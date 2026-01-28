package dev.sweety.unibo.file;

import dev.sweety.unibo.file.language.LanguageYml;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.java.JavaPlugin;

@UtilityClass
public class Files {

    public static LanguageYml LANGUAGE;
    public static PlayerElo PLAYER_ELO;

    public void init(final JavaPlugin resource) {
        LANGUAGE = new LanguageYml(resource);
        PLAYER_ELO = new PlayerElo(resource);
    }

}
