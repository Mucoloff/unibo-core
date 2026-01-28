package dev.sweety.unibo.api.serializable;

import com.github.retrooper.packetevents.util.Vector3d;
import dev.sweety.core.math.vector.CoordUtils;
import dev.sweety.core.math.vector.d2.Vector2f;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class Position extends ObjSerializable {

    public static final Position EMPTY =
            new Position("none",
                    new Vector3d(Double.NaN, Double.NaN, Double.NaN),
                    new Vector2f(Float.NaN, Float.NaN));
    public static final String SEPARATOR = "|";


    private String world;
    private Vector3d position;
    private Vector2f rotation;

    public Position(final Map<String, Object> me) {
        world = String.valueOf(me.get("world"));
        position = new Vector3d(CoordUtils.toVec3d(String.valueOf(me.get("position")).split("\\|")));
        rotation = CoordUtils.toVec2f(String.valueOf(me.get("rotation")).split("\\|"));
    }

    @Override
    public void serialize(@NotNull Map<String, Object> me) {
        me.put("world", this.world);
        me.put("position", CoordUtils.fromVec3d(SEPARATOR, this.position.x, this.position.y, this.position.z));
        me.put("rotation", CoordUtils.fromVec2f(SEPARATOR, this.rotation));
    }

    @Override
    public String id() {
        return serializeString();
    }

    public String serializeString() {
        return world
                + "#" + CoordUtils.fromVec3d(SEPARATOR, this.position.x, this.position.y, this.position.z)
                + "#" + CoordUtils.fromVec2f(SEPARATOR, this.rotation);
    }

    public static @NotNull Position deserializeString(final String value) {
        final String[] parts = value.split("#");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Position string: " + value);
        }

        final String world = parts[0];

        return new Position(
                world,
                new Vector3d(CoordUtils.toVec3d(parts[1].split("\\|"))),
                CoordUtils.toVec2f(parts[2].split("\\|"))
        );
    }

    public boolean isEmpty() {
        return "none".equals(this.world);
    }

    public static @NotNull Position asPosition(Location location) {
        return new Position(location.getWorld().getName(), new Vector3d(location.getX(), location.getY(), location.getZ()), new Vector2f(location.getYaw(), location.getPitch()));
    }

    public @NotNull Location toLocation() {
        final World world = Bukkit.getWorld(this.getWorld());
        final Vector3d pos = this.getPosition();
        final Vector2f rot = this.getRotation();
        return new Location(world, pos.x, pos.y, pos.z, rot.x, rot.y);
    }

    public void teleport(Collection<Player> players) {
        final Location location = toLocation();
        players.forEach(player -> {
            if (location.getWorld() == null) {
                location.setWorld(player.getWorld());
            }
            player.teleportAsync(location);
        });
    }

    public void teleport(Player... players) {
        teleport(Arrays.stream(players).toList());
    }

}