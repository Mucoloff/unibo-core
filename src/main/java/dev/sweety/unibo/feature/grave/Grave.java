package dev.sweety.unibo.feature.grave;

import dev.sweety.unibo.utils.ColorUtils;
import dev.sweety.unibo.utils.McUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public final class Grave {
    private final UUID uuid;
    private final String playerName;
    private final Inventory inventory;
    private final Location deathLocation;
    private final long time;
    private ItemStack item;

    public Grave(UUID uuid, String playerName, Inventory inventory, Location deathLocation, long time) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.inventory = inventory;
        this.deathLocation = deathLocation;
        this.time = time;
    }

    public String name() {
        return playerName + "#" + Long.toHexString(time);
    }

    public String holoName() {
        //return name();
        return playerName + "_holo_" + Long.toHexString(time);
    }

    public @NotNull ItemStack item() {
        if (item == null) {
            this.item = new ItemStack(Material.PAPER);
            item.editMeta(meta -> {
                String date = new SimpleDateFormat("dd/MM hh:mm:ss").format(new Date(time));
                String lore = String.format("%s\n&aDeath Time: &c%s", location(deathLocation, true), date);
                String name = String.format("&e%s's &7Grave", playerName);
                meta.displayName(McUtils.component(name));
                meta.lore(McUtils.colorList(Arrays.stream(lore.split("\n")).toList()));

                meta.getPersistentDataContainer().set(GraveListener.key, PersistentDataType.BYTE, (byte) 1);
            });

        }

        return item;
    }

    private static String location(Location location, boolean world) {
        return ColorUtils.color(world ? (String.format("&eWorld: &f%s\n&e[&6 %s / %s / %s &e]",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ())) : (String.format("&e[&6 %s / %s / %s &e]",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ())));
    }

    public UUID uuid() {
        return uuid;
    }

    public String playerName() {
        return playerName;
    }

    public Inventory inventory() {
        return inventory;
    }

    public Location deathLocation() {
        return deathLocation;
    }

    public long time() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Grave) obj;
        return Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.playerName, that.playerName) &&
                Objects.equals(this.inventory, that.inventory) &&
                Objects.equals(this.deathLocation, that.deathLocation) &&
                this.time == that.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, playerName, inventory, deathLocation, time);
    }

    @Override
    public String toString() {
        return "Grave[" +
                "uuid=" + uuid + ", " +
                "playerName=" + playerName + ", " +
                "inventory=" + inventory + ", " +
                "deathLocation=" + deathLocation + ", " +
                "time=" + time + ']';
    }

}
