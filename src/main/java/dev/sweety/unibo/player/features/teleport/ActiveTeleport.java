package dev.sweety.unibo.player.features.teleport;

import dev.sweety.unibo.feature.teleport.tpa.TpaType;
import dev.sweety.unibo.player.VanillaPlayer;
import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class ActiveTeleport {
    private final UUID requester, target;
    private final Location location;
    private volatile CompletableFuture<?> countdown;

    public ActiveTeleport(UUID requester, UUID target, Location location, TpaType type) {
        if (type == TpaType.TPA) {
            this.requester = requester;
            this.target = target;
        } else {
            this.requester = target;
            this.target = requester;
        }

        this.location = location;
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

    public synchronized void assignTask(CompletableFuture<?> task) {
        if (this.cancelled) {
            task.cancel(true);
            return;
        }
        this.countdown = task;
    }

    public synchronized void stopTimer() {
        if (this.countdown != null) {
            this.countdown.cancel(false);
            this.countdown = null;
        }
    }

    public UUID requester() {
        return this.requester;
    }

    public Location location() {
        return this.location;
    }

    public UUID other(final VanillaPlayer player) {
        return this.requester.equals(player.uuid()) ? this.target : this.requester;
    }
}
