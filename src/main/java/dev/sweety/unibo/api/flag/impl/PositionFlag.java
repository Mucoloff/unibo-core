package dev.sweety.unibo.api.flag.impl;

import com.github.retrooper.packetevents.util.Vector3d;
import dev.sweety.unibo.api.serializable.Position;
import dev.sweety.unibo.api.flag.Flag;
import dev.sweety.unibo.feature.region.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PositionFlag extends Flag<Position> {

    public PositionFlag(@NotNull String name, @NotNull Position defaultValue) {
        super(name, defaultValue);
    }


    @Override
    public String serialize(Position value) {
        return value.serializeString();
    }

    @Override
    public Position deserialize(String value) {
        return Position.deserializeString(value);
    }

    public Vector3d teleport(Region region, Collection<Player> players) {
        Position position = region.getFlagStatus(this);
        if (position.isEmpty()) {
            players.forEach(p -> p.sendRichMessage("<red>No position set for this region."));
            return null;
        }

        position.teleport(players);
        return position.getPosition();
    }

    public Vector3d teleport(Region region, Player... players) {
        Position position = region.getFlagStatus(this);
        if (position.isEmpty()) {
            for (Player p : players) p.sendRichMessage("<red>No position set for this region.");
            return null;
        }

        position.teleport(players);
        return position.getPosition();
    }

    public Location asLocation(Region region) {
        Position position = region.getFlagStatus(this);
        if (position.isEmpty()) return null;

        return position.toBukkitLocation();
    }
}
