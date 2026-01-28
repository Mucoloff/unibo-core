package dev.sweety.unibo.feature.region.event.movement;

import com.github.retrooper.packetevents.util.Vector3d;
import dev.sweety.unibo.feature.region.Region;
import lombok.Getter;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

@Getter
public class RegionSwitchEvent extends RegionMoveEvent {

    @Nullable
    private final Region lastRegion;

    public RegionSwitchEvent(final Player player, Region rg, Vector3d position, float yaw, float pitch, @Nullable Region lastRegion) {
        super(player, rg, position, yaw, pitch);
        this.lastRegion = lastRegion;
    }
}
