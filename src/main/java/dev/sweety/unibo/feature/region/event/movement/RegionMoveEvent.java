package dev.sweety.unibo.feature.region.event.movement;

import com.github.retrooper.packetevents.util.Vector3d;
import dev.sweety.unibo.feature.region.Region;
import dev.sweety.unibo.feature.region.event.RegionEvent;
import org.bukkit.entity.Player;

public class RegionMoveEvent extends RegionEvent {

    public RegionMoveEvent(final Player player, Region rg, Vector3d position, float yaw, float pitch) {
        super(player, rg, position, yaw, pitch);
    }
}
