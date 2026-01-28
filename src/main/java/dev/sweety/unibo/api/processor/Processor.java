package dev.sweety.unibo.api.processor;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.packet.PacketHandler;
import dev.sweety.unibo.player.VanillaPlayer;
import lombok.Getter;

@Getter
public abstract class Processor implements PacketHandler {

    protected final VanillaPlayer player;
    protected final VanillaCore plugin;

    public Processor(final VanillaPlayer player, final VanillaCore plugin) {
        this.player = player;
        this.plugin = plugin;
    }

}
