package dev.sweety.unibo.file.language;

import dev.sweety.unibo.api.file.BukkitFile;
import org.bukkit.plugin.java.JavaPlugin;

public class LanguageYml extends BukkitFile {

    public LanguageYml(JavaPlugin resource) {
        super(resource, "language.yml");
    }

}
