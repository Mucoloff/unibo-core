package dev.sweety.unibo.feature.region;

import com.github.retrooper.packetevents.util.Vector3d;
import dev.sweety.core.math.vector.CoordUtils;
import com.github.retrooper.packetevents.util.Vector3i;
import dev.sweety.core.util.ObjectUtils;
import dev.sweety.unibo.api.VanillaAPI;
import dev.sweety.unibo.api.flag.Flag;
import dev.sweety.unibo.api.flag.FlagType;
import dev.sweety.unibo.api.flag.impl.BooleanFlag;
import dev.sweety.unibo.api.serializable.Position;
import dev.sweety.unibo.file.language.Language;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.sweety.record.annotations.RecordData;
import dev.sweety.unibo.api.serializable.NamedSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RecordData
public class Region extends NamedSerializable implements RegionAccessors {

    private final Map<Flag<?>, Object> flags = new HashMap<>();
    private final Map<String, List<String>> exempts = new HashMap<>();
    public World worldInstance;
    private String world;
    private Vector3i min, max, center;
    private int priority = -1;

    public Region(final String name, final Location loc, final Location loc2) {
        super(name);
        redefine(loc, loc2);
    }

    public Region(Map<String, Object> me) {
        super(me);

        this.world = (String) me.get("world");
        this.priority = (int) me.get("priority");
        this.min = new Vector3i(CoordUtils.toVec3i(((String) me.get("min")).split("\\|")));
        this.max = new Vector3i( CoordUtils.toVec3i(((String) me.get("max")).split("\\|")));

        try {
            // noinspection unchecked
            Map<String, String> flags = (Map<String, String>) me.get("flags");
            for (Map.Entry<String, String> entry : flags.entrySet()) {
                Flag<?> flag = VanillaAPI.getFlag(entry.getKey());
                if (flag == null) continue;

                this.flags.put(flag, getFromString(flag, entry.getValue()));
            }
        } catch (Throwable t) {
            throw new IllegalArgumentException("Error deserializing flags: ", t);
        }

        // noinspection unchecked
        Map<String, List<String>> exempted = (Map<String, List<String>>) me.get("exempts");
        if (exempted != null) this.exempts.putAll(exempted);

    }

    public void redefine(final Location pos1,final  Location pos2) {
        this.world = pos1.getWorld() == null ? "default" : pos1.getWorld().getName();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());

        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        this.min = new Vector3i(minX, minY, minZ);
        this.max = new Vector3i(maxX, maxY, maxZ);
        this.center = new Vector3i((int) ((max.x + min.x) * 0.5), (int) ((max.y + min.y) * 0.5), (int) ((max.z + min.z) * 0.5));
    }

    public boolean isDefault() {
        return false;
    }

    @Override
    public void serialize(@NotNull Map<String, Object> me) {
        super.serialize(me);
        me.put("world", this.world);
        me.put("priority", this.priority);
        me.put("min", CoordUtils.fromVec3i(Position.SEPARATOR, this.min.x, this.min.y, this.min.z));
        me.put("max", CoordUtils.fromVec3i(Position.SEPARATOR, this.max.x, this.max.y, this.max.z));

        Map<String, String> flags = new HashMap<>();
        for (Flag<?> flag : this.flags.keySet()) {
            flags.put(flag.getName(), getAsString(flag));
        }
        me.put("flags", flags);

        Map<String, List<String>> exempted = new HashMap<>(this.exempts);
        me.put("exempts", exempted);
    }

    public boolean inRegion(Location location) {
        return inRegion(location.getWorld().getName(), new Vector3d(location.getX(), location.getY(), location.getZ()));
    }

    public boolean inRegion(final String worldName, final Vector3d position) {
        if (ObjectUtils.isNull(this.world)) return false;
        if (!this.world.equals(worldName)) return false;

        final double size = 0.3d;
        final double minX = position.getX() - size, maxX = position.getX() + size;

        if (maxX < this.min.x || minX > this.max.x) return false;

        final double minZ = position.getZ() - size, maxZ = position.getZ() + size;

        if (maxZ < this.min.z || minZ > this.max.z) return false;

        final double minY = position.getY(), maxY = position.getY() + 1.8d;
        return maxY >= this.min.y && minY <= this.max.y;
    }

    public boolean isFlagActive(final FlagType flagType, final Player player) {
        return isFlagActive(flagType.getFlag()) || isExempt(player.getName(), flagType.getName());
    }

    public boolean isFlagActive(final BooleanFlag flag) {
        return getFlagStatus(flag);
    }

    public void setFlagActive(final FlagType flagType, final boolean active) {
        setFlagStatus(flagType.getFlag(), active);
    }

    @SuppressWarnings("unchecked")
    public <T> T getFlagStatus(final Flag<T> flag) {
        if (flags.containsKey(flag)) return (T) flags.get(flag);
        return flag.getDefaultValue();
    }

    public <T> void setFlagStatus(final Flag<T> flag, final T action) {
        if (action == null) flags.remove(flag);
        else flags.put(flag, action);
    }

    public void reset() {
        flags.clear();
    }

    public <T> void resetFlagStatus(final Flag<T> flag) {
        flags.remove(flag);
    }

    public <T> String getAsString(final Flag<T> flag) {
        return flag.serialize(getFlagStatus(flag));
    }

    public <T> T getFromString(final Flag<T> flag, final String arg) {
        T deserialized = flag.deserialize(arg);
        return deserialized == null ? flag.getDefaultValue() : deserialized;
    }

    public <T> void setFromString(final Flag<T> flag, String arg) {
        setFlagStatus(flag, flag.deserialize(arg));
    }

    public void addExemptPlayer(final String flagName, final String playerName) {
        this.exempts.computeIfAbsent(flagName, k -> new ArrayList<>()).add(playerName);
    }

    public void removeExemptPlayer(final String flagName, final String playerName) {
        final List<String> exempted = this.exempts.get(flagName);

        exempted.remove(playerName);

        if (exempted.isEmpty()) this.exempts.remove(flagName);
    }

    public boolean isExempt(final String playerName, final String flagName) {
        final List<String> exempted = this.exempts.get(flagName);
        return exempted != null && exempted.contains(playerName);
    }

    public World getBukkitWorld() {
        if (worldInstance == null) worldInstance = Bukkit.getWorld(this.world);
        return worldInstance;
    }

    public boolean is(final Region region) {
        if (region == null) return false;
        return id(region.id());
    }

    public Component info() {
        Component info = Language.REGIONS_INFO_HEADER.component("%region%", getName());

        info = info.appendNewline().append(Language.REGIONS_INFO_FLAG__ENTRY.component("%flag%", "min", "%value%", min().toString()))
                .appendNewline().append(Language.REGIONS_INFO_FLAG__ENTRY.component("%flag%", "max", "%value%", max().toString()));

        for (Flag<?> flag : VanillaAPI.getFlags()) {
            info = info.appendNewline().append(
                    (Language.REGIONS_INFO_FLAG__ENTRY.component("%flag%", flag.getName(), "%value%", getAsString(flag)))
                            .hoverEvent(HoverEvent.showText(Language.REGIONS_INFO_HOVER__TEXT.component("%flag%", flag.getName())))
                            .clickEvent(ClickEvent.suggestCommand(Language.REGIONS_INFO_CLICK__COMMAND.get("%region%", getName(), "%flag%", flag.getName())))
            );
        }
        return info;
    }

    @Override
    public String toString() {
        return "Region{" +
                "name='" + name + '\'' +
                ", world='" + world + '\'' +
                '}';
    }
}
