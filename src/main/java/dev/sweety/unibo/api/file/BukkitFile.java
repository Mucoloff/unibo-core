package dev.sweety.unibo.api.file;

import dev.sweety.unibo.utils.McUtils;
import dev.sweety.unibo.utils.ColorUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

@Getter
public class BukkitFile {

    protected final String fileName, dirName;
    protected final File file, dir;
    protected final boolean hasDir;
    private final JavaPlugin plugin;
    protected YamlConfiguration config;

    public BukkitFile(final @NotNull JavaPlugin resource, final @NotNull String fileName, final @NotNull String dirName) {
        this.plugin = resource;
        this.hasDir = true;
        this.fileName = fileName.endsWith(".yml") ? fileName : (fileName + ".yml");
        this.dirName = dirName;
        this.dir = new File(resource.getDataFolder(), dirName);
        this.file = new File(this.dir, this.fileName);

        this.init();
    }

    public BukkitFile(final @NotNull JavaPlugin resource, final @NotNull String fileName) {
        this.plugin = resource;
        this.hasDir = false;
        this.fileName = fileName.endsWith(".yml") ? fileName : fileName + ".yml";
        this.dirName = "";
        this.dir = resource.getDataFolder();
        this.file = new File(dir, this.fileName);

        this.init();
    }

    @SneakyThrows
    private void init() {
        if (!this.file.exists()) {
            if (this.hasDir && !this.dir.exists()) this.dir.mkdirs();

            String path = (this.hasDir ? (this.dirName + "/") : "") + this.fileName;

            if (this.plugin.getResource(path) == null) {
                this.file.createNewFile();
                create();
            } else this.plugin.saveResource(path, false);

        }

        reload();
    }

    public void create() {

    }

    @SneakyThrows
    public void save() {
        getConfig().save(this.file);
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public YamlConfiguration getConfig() {
        if (this.config == null) reload();
        return this.config;
    }

    @SneakyThrows
    public void delete() {
        this.file.delete();
    }

    public Component getComponent(final String path) {
        return McUtils.component(getString(path));
    }

    public Component getComponent(final String path, final String def) {
        return McUtils.component(getString(path, def));
    }

    public String getString(final String path, final String def) {
        return ColorUtils.color(getConfig().getString(path, def));
    }

    public String getString(final String path) {
        return ColorUtils.color(getConfig().getString(path));
    }

    public @NotNull List<String> getStringList(final String path) {
        return ColorUtils.colorList(getConfig().getStringList(path));
    }

}
