package dev.sweety.unibo.player.features.teleport;

import dev.sweety.unibo.player.VanillaPlayer;
import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class ActiveTeleport {
    private final UUID requester, target;
    private final Location location;
    private final TpaType type;
    volatile CompletableFuture<?> countdown;

    public ActiveTeleport(UUID requester, UUID target, Location location, TpaType type) {
        this.requester = requester;
        this.target = target;
        this.location = location;
        this.type = type;
    }

    private boolean cancelled = false;

    public synchronized void cancel() {
        if (this.cancelled) return;
        this.cancelled = true;

        if (this.countdown != null) {
            this.countdown.cancel(true);
            this.countdown = null;
        }
    }

    public boolean cancelled() {
        return this.cancelled;
    }

    public UUID requester() {
        return this.requester;
    }

    public UUID target() {
        return this.target;
    }

    public Location location() {
        return this.location;
    }

    public TpaType type() {
        return this.type;
    }

    public UUID other(final VanillaPlayer player) {
        return this.requester.equals(player.uuid()) ? this.target : this.requester;
    }
}
