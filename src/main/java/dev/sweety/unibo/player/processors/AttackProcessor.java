package dev.sweety.unibo.player.processors;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.packet.Packet;
import dev.sweety.unibo.api.processor.Processor;
import dev.sweety.unibo.player.PlayerManager;
import dev.sweety.unibo.player.VanillaPlayer;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Getter
public class AttackProcessor extends Processor {

    private final PlayerManager playerManager;

    public AttackProcessor(final VanillaPlayer player, final VanillaCore plugin) {
        super(player, plugin);
        this.playerManager = plugin.playerManager();
    }

    private Player lastPlayerHit = null;
    private Entity lastHit = null;
    private boolean attack = false, isPlayer = false;

    @Override
    public void handle(final Packet packet) {
        if (!(packet.getWrapper() instanceof WrapperPlayClientInteractEntity wrap)) return;

        this.attack = false;
        this.isPlayer = false;

        if (!wrap.getAction().equals(WrapperPlayClientInteractEntity.InteractAction.ATTACK)) return;

        this.attack = true;
        this.lastHit = SpigotConversionUtil.getEntityById(this.player.world(), wrap.getEntityId());

        if (!(lastHit instanceof Player victim)) return;
        this.isPlayer = true;
        this.lastPlayerHit = victim;
    }

}
