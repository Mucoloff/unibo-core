package dev.sweety.unibo.feature.grave;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public record Grave(UUID uuid, String playerName, Inventory inventory, Location deathLocation, long time) {

    public String name(){
        return playerName + "#" + Long.toHexString(time);
    }

    public String holoName(){
        return name();
        //return playerName + "_holo_" + Long.toHexString(time);
    }

}
