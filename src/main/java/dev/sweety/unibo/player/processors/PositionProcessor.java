package dev.sweety.unibo.player.processors;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.packet.Packet;
import dev.sweety.unibo.api.processor.Processor;
import dev.sweety.unibo.player.VanillaPlayer;
import lombok.Getter;

@Getter
public class PositionProcessor extends Processor {

    private float yaw, pitch;
    private float lastYaw, lastPitch;
    private boolean onGround, lastOnGround;
    private Vector3d position, lastPosition;

    public PositionProcessor(final VanillaPlayer player, final VanillaCore plugin) {
        super(player, plugin);
        org.bukkit.Location location = player.player().getLocation();
        this.position = new Vector3d(location.getX(), location.getY(), location.getZ());
        this.lastPosition = this.position;
        this.yaw = location.getYaw();
        this.lastYaw = this.yaw;
        this.pitch = location.getPitch();
        this.lastPitch = this.pitch;
        this.onGround = true;
        this.lastOnGround = true;
    }

    @Override
    public void handle(final Packet packet) {
        if (!packet.isMovement() || !(packet.getEvent() instanceof PacketReceiveEvent receive)) return;
        final WrapperPlayClientPlayerFlying wrap = new WrapperPlayClientPlayerFlying(receive);
        final Location location = wrap.getLocation();

        if (wrap.hasPositionChanged()) {
            this.lastPosition = this.position;
            this.position = location.getPosition();
            this.lastOnGround = this.onGround;
            this.onGround = wrap.isOnGround();
        }

        if (wrap.hasRotationChanged()) {
            this.lastYaw = this.yaw;
            this.lastPitch = this.pitch;
            this.yaw = location.getYaw();
            this.pitch = location.getPitch();
        }
    }

    public void setBack() {
        final Vector3d pos = this.lastPosition;
        if (pos == null) return;

        final Vector3d direction = this.position.subtract(pos).normalize();
        final Vector3d safePos = pos.subtract(direction);

        final org.bukkit.Location location = new org.bukkit.Location(this.player.world(), safePos.getX(), pos.getY(), safePos.getZ(), lastYaw, lastPitch);
        this.player.player().teleportAsync(location);
    }

}