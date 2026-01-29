package dev.sweety.unibo.player.processors;

import com.github.retrooper.packetevents.protocol.world.damagetype.DamageType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDamageEvent;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.packet.Packet;
import dev.sweety.unibo.api.processor.Processor;
import dev.sweety.unibo.player.PlayerManager;
import dev.sweety.unibo.player.VanillaPlayer;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Entity;

@Getter
public class DamageProcessor extends Processor {

    private final PlayerManager playerManager;
    private DamageType sourceType;

    public DamageProcessor(final VanillaPlayer player, final VanillaCore plugin) {
        super(player, plugin);
        this.playerManager = plugin.playerManager();
    }

    private Entity damager = null;
    private Entity cause = null;

    private boolean isPlayer = false;

    @Override
    public void handle(final Packet packet) {
        if (!(packet.getWrapper() instanceof WrapperPlayServerDamageEvent wrap)) return;
        this.isPlayer = wrap.getEntityId() == this.player.entityId();
        if (!this.isPlayer) return;
        this.sourceType = wrap.getSourceType();

        final int sourceCauseId = wrap.getSourceCauseId();
        final int sourceDirectId = wrap.getSourceDirectId();

        final World world = this.player.world();

        this.damager = sourceCauseId > 0 ? SpigotConversionUtil.getEntityById(world, sourceCauseId - 1) : null;
        if (sourceDirectId > 0) {
            this.cause = sourceDirectId == sourceCauseId ? null : SpigotConversionUtil.getEntityById(world, sourceDirectId - 1);
        }
    }

    public boolean isSame() {
        return this.cause == null;
    }

}
