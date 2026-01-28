package dev.sweety.unibo.feature.region;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;

public class DefaultRegion extends Region {

    public static final Location NONE = new Location(null, 0, 0, 0, 0, 0);

    public DefaultRegion(World world) {
        this(world.getName());
    }

    public DefaultRegion(String world) {
        super("default-" + world, NONE, NONE);
    }

    public DefaultRegion(Map<String, Object> me) {
        super(me);
    }

    @Override
    public void redefine(Location pos1, Location pos2) {
        min(new Vector3i());
        max(new Vector3i());
    }

    @Override
    public boolean inRegion(Location location) {
        return false;
    }

    @Override
    public boolean inRegion(String worldName, Vector3d position) {
        return false;
    }

    @Override
    public boolean isDefault() {
        return true;
    }
}
