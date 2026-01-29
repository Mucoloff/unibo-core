package dev.sweety.unibo.player.processors;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import dev.sweety.core.math.mask.Mask;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.packet.Packet;
import dev.sweety.unibo.api.processor.Processor;
import dev.sweety.unibo.player.VanillaPlayer;

public class PositionProcessor extends Processor implements Mask {

    private final Location location;
    private final Location lastLocation;
    private final byte[] _mask;

    public PositionProcessor(final VanillaPlayer player, final VanillaCore plugin) {
        super(player, plugin);

        org.bukkit.Location l = player.player().getLocation();
        this.location = new Location(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
        this.lastLocation = location.clone();

        this._mask = new byte[]{0x3};
    }

    @Override
    public void handle(final Packet packet) {
        if (!packet.isMovement() || !(packet.getEvent() instanceof PacketReceiveEvent receive)) return;
        final WrapperPlayClientPlayerFlying wrap = new WrapperPlayClientPlayerFlying(receive);
        final Location location = wrap.getLocation();
        final boolean pos = wrap.hasPositionChanged(), rot = wrap.hasRotationChanged();

        set(0, 0x4, pos);
        set(0, 0x8, rot);

        if (pos) {
            this.lastLocation.setPosition(this.location.getPosition());
            this.location.setPosition(location.getPosition());

            set(0, 0x1, onGround());
            set(0, 0x2, wrap.isOnGround());
        }

        if (rot) {
            this.lastLocation.setYaw(this.location.getYaw());
            this.lastLocation.setPitch(this.location.getPitch());
            this.location.setYaw(location.getYaw());
            this.location.setPitch(location.getPitch());
        }
    }

    public void setBack() {
        if (this.lastLocation == null) return;
        final Vector3d last = this.lastLocation.getPosition();

        final Vector3d direction = this.location.getPosition().subtract(last).normalize();
        final Vector3d safePos = last.subtract(direction);

        final org.bukkit.Location location = new org.bukkit.Location(this.player.world(), safePos.getX(), last.getY(), safePos.getZ(), this.lastLocation.getYaw(), this.lastLocation.getPitch());
        this.player.player().teleportAsync(location);
    }

    public boolean onGround() {
        return has(0, (byte) 0x1);
    }

    public boolean lastOnGround() {
        return has(0, (byte) 0x2);
    }

    public boolean isPosition() {
        return has(0, (byte) 0x4);
    }

    public boolean isRotation() {
        return has(0, (byte) 0x8);
    }

    @Override
    public byte[] masks() {
        return _mask;
    }

    public Location location() {
        return location;
    }

    public Location lastLocation() {
        return lastLocation;
    }

    public Vector3d position() {
        return this.location.getPosition();
    }

    public Vector3d lastPosition() {
        return this.lastLocation.getPosition();
    }

    public float yaw() {
        return this.location.getYaw();
    }

    public float lastYaw() {
        return this.lastLocation.getYaw();
    }

    public float pitch() {
        return this.location.getPitch();
    }

    public float lastPitch() {
        return this.lastLocation.getPitch();
    }

}