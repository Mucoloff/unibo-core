package dev.sweety.unibo.api.serializable;

import dev.sweety.core.logger.SimpleLogger;
import dev.sweety.core.util.ObjectUtils;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.VanillaCoreAccessors;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

@Getter
public abstract class SerializableManager<T extends ObjSerializable> {

    protected final VanillaCore resource;
    private final Class<T> clazz;
    protected final Map<String, T> values = new HashMap<>();
    private final SimpleLogger logger;
    private final String name;
    protected File folder;

    public SerializableManager(final VanillaCore resource, final Class<T> clazz) {
        this.name = (this.clazz = clazz).getSimpleName().toLowerCase();
        this.resource = resource;
        this.logger = VanillaCoreAccessors.logger();
        this.folder = new File(resource.instance().getDataFolder(), this.name + "s");
    }

    public void init() {
        if (this.folder.exists() || this.folder.mkdir()) {
            load();
            return;
        }
        this.logger.warn("Error while loading configuration.");
        this.logger.warn("Plugin initialization disabled");
        throw new IllegalStateException("Could not make " + this.name + " folder!!");
    }

    public void load() {
        this.values.clear();
        if (!folder.exists() || !this.folder.isDirectory()) {
            this.logger.info("Loaded 0 " + name + "s");
            return;
        }

        File[] files = ObjectUtils.nullOption(listFiles(), new File[0]);

        for (File file : files) load(file);
        this.logger.info("Loaded " + this.values.size() + " " + this.name + "s: " + Arrays.toString(this.values.keySet().toArray()));
    }

    protected File @Nullable [] listFiles() {
        return this.folder.listFiles();
    }

    public T load(final File file) {
        YamlConfiguration fileConf = YamlConfiguration.loadConfiguration(file);
        try {
            fileConf.save(file);
            fileConf = YamlConfiguration.loadConfiguration(file);
            T obj = fileConf.getSerializable(this.name, clazz);

            return put(obj);
        } catch (Throwable t) {
            this.logger.warn("Unable to load " + this.name + " " + file.getName(), t.toString());
        }
        return null;
    }

    public void shutdown() {
        save();
    }

    public void save() {
        values().forEach(this::save);
    }

    public Set<String> getNames() {
        return this.values.keySet();
    }

    public T get(final String name) {
        if (ObjectUtils.isNull(name)) return null;
        final T t = this.values.get(name);
        if (t == null) logger.warn("unable to find " + this.name + ": " + name);
        return t;
    }

    public Collection<T> values() {
        return this.values.values();
    }

    public File getFile(final String o) {
        return new File(this.folder, o.replace(" ", "") + "." + name + ".yml");
    }

    public void save(final T o) {
        File file = getFile(o.id());
        FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
        conf.set(this.name, o);
        try {
            conf.save(file);
            YamlConfiguration.loadConfiguration(file);
            logger.info(file.getName() + " saved!");
        } catch (IOException e) {
            logger.warn("Unable to serialize " + this.name + " " + o.id(), e);
        }

    }

    @SuppressWarnings("ALL")
    public void delete(final T obj) {
        final String id = obj.id();
        this.values.remove(id);
        getFile(id).delete();
    }

    public List<T> match(final Predicate<T> match) {
        List<T> matches = new LinkedList<>();
        for (T o : values()) {
            if (!match.test(o)) continue;
            matches.add(o);
        }
        return matches;
    }

    public List<T> match(final String id) {
        return match(t -> t.id(id));
    }

    public Optional<T> getOptional(final String name) {
        return Optional.ofNullable(get(name));
    }

    public void add(T t) {
        save(t);
        put(t);
    }

    protected T put(T obj) {
        this.values.put(obj.id(), obj);
        return obj;
    }

}
