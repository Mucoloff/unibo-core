package dev.sweety.unibo.file.language;

import dev.sweety.unibo.utils.McUtils;
import dev.sweety.unibo.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

import static dev.sweety.unibo.file.Files.LANGUAGE;

public enum Language {

    COMBAT_END,
    COMBAT_LOG__OUT,
    COMBAT_START,
    COMBAT_TIMER,
    DEATH__MESSAGES_DROWNING__DEATH,
    DEATH__MESSAGES_END__CRYSTAL__KILLED,
    DEATH__MESSAGES_FALL_DEATH,
    DEATH__MESSAGES_PLAYER__KILLED,
    DEATH__MESSAGES_SHOW__INV,
    DEATH__MESSAGES_UNKNOWN__DEATH,
    GAMEMODE_ERROR,
    GAMEMODE_OTHER,
    GAMEMODE_SET,
    JOIN__MESSAGE,
    NO__PERMISSION,
    PLAYER__NOT__FOUND,
    QUIT__MESSAGE,
    REGIONS_ERRORS_ALREADY__EXISTS,
    REGIONS_ERRORS_INVALID__FLAG,
    REGIONS_ERRORS_MISSING__POSITIONS,
    REGIONS_ERRORS_REGION__NOT__FOUND,
    REGIONS_FLAG_FLAG__SET,
    REGIONS_INFO_CLICK__COMMAND,
    REGIONS_INFO_FLAG__ENTRY,
    REGIONS_INFO_HEADER,
    REGIONS_INFO_HOVER__TEXT,
    REGIONS_LIST__HEADER,
    REGIONS_POS_FIRST,
    REGIONS_POS_SECOND,
    REGIONS_REGION__ENTRY_CLICK__COMMAND,
    REGIONS_REGION__ENTRY_HOVER__TEXT,
    REGIONS_RELOAD,
    REGIONS_SUCCESS_CREATE,
    REGIONS_SUCCESS_DELETE,

    REGIONS_SUCCESS_REDEFINE,
    REGIONS_SUCCESS_RESET__FLAG,

    REGIONS_SUCCESS_SET__SPAWN,
    REGIONS_SUCCESS_SPAWN;

    private final String path;
    private final FileConfiguration config = LANGUAGE.getConfig();

    Language() {
        this.path = name().toLowerCase().replace("__", "-").replace("_", ".");
    }

    public String get(String... replace) {
        String string = this.config.getString(this.path, this.path + ": null");
        for (int i = 0; i < replace.length - 1; i += 2) string = string.replace(replace[i], replace[i + 1]);
        return ColorUtils.color(string);
    }

    public String get() {
        return ColorUtils.color(this.config.getString(this.path, this.path + ": null"));
    }

    public Component component(String... replace) {
        return McUtils.component(get(replace));
    }

    public Component component() {
        return McUtils.component(get());
    }

    public List<String> getList() {
        return ColorUtils.colorList(this.config.getStringList(this.path));
    }
}
