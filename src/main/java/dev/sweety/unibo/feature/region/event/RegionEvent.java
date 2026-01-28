package dev.sweety.unibo.feature.region.event;

import com.github.retrooper.packetevents.util.Vector3d;
import dev.sweety.core.math.vector.d2.Vector2f;
import dev.sweety.unibo.api.serializable.Position;
import dev.sweety.unibo.feature.region.Region;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class RegionEvent extends Event implements Cancellable {

    private boolean cancelled = false;

    private Player player;
    private Region region;
    private Position location;

    public RegionEvent(final Player player, Region region, Vector3d position, float yaw, float pitch) {
        super(true);
        this.region = region;
        this.player = player;
        this.location = new Position(player.getWorld().getName(), position, new Vector2f(yaw, pitch));
    }

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
