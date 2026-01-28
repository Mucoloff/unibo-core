package dev.sweety.unibo.player.processors;

import com.github.retrooper.packetevents.protocol.world.damagetype.DamageTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDamageEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.VanillaAPI;
import dev.sweety.unibo.api.packet.Packet;
import dev.sweety.unibo.api.packet.PacketType;
import dev.sweety.unibo.api.processor.Processor;
import dev.sweety.unibo.api.flag.FlagType;
import dev.sweety.unibo.feature.region.Region;
import dev.sweety.unibo.feature.region.RegionManager;
import dev.sweety.unibo.feature.region.event.movement.RegionMoveEvent;
import dev.sweety.unibo.feature.region.event.movement.RegionSwitchEvent;
import dev.sweety.unibo.player.VanillaPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class RegionProcessor extends Processor {

    private final PositionProcessor positionProcessor;
    private final RegionManager regionManager;

    public RegionProcessor(final VanillaPlayer player, final VanillaCore plugin) {
        super(player, plugin);
        this.positionProcessor = player.positionProcessor();
        this.regionManager = plugin.regionManager();
    }

    @Override
    public void handle(final Packet packet) {

        this.handleMovement(packet);

        final Region currentRegion = this.player.region();
        if (currentRegion == null) return;

        switch (packet.getWrapper()) {

            case WrapperPlayServerDamageEvent wrap -> {
                if (wrap.getSourceType().equals(DamageTypes.FALL)) {
                    if (!currentRegion.isFlagActive(FlagType.FALL, player.player())) {
                        packet.cancel();
                    }
                }
            }

            case WrapperPlayClientInteractEntity wrap -> {
                if (wrap.getAction().equals(WrapperPlayClientInteractEntity.InteractAction.ATTACK)) {
                    if (!currentRegion.isFlagActive(FlagType.ATTACK, player.player())) {
                        packet.cancel();
                    }
                }
                if (!currentRegion.isFlagActive(FlagType.INTERACT_ENTITY, player.player())) {
                    packet.cancel();
                }
            }
            case WrapperPlayClientUseItem wrap -> {
                if (!currentRegion.isFlagActive(FlagType.THROWABLE, player.player())) {
                    packet.cancel();
                }
            }

            case WrapperPlayClientChatCommand wrap -> {
                String cmd = wrap.getCommand();
                if (currentRegion.getFlagStatus(FlagType.BANNED_COMMANDS).contains(cmd)) {
                    packet.cancel();
                }
            }
            case WrapperPlayClientEntityAction wrap -> {
                if (!currentRegion.isFlagActive(FlagType.ENTITY_ACTION, player.player())) {
                    packet.cancel();
                }
            }
            case WrapperPlayClientPlayerAbilities wrap -> {
                if (!currentRegion.isFlagActive(FlagType.ELYTRA, player.player())) {
                    packet.cancel();
                }
            }
            case null, default -> {
            }
        }
    }

    private void handleMovement(final Packet packet) {
        final Vector3d position = this.positionProcessor.getPosition();

        if (position == null) return;
        if (!(packet.isMovement() || !packet.is(PacketType.PacketServer.PLAYER_POSITION_AND_LOOK))) return;

        final String worldName = this.player.worldName();
        final Player bukkitPlayer = this.player.player();

        final Region playerRegion = this.player.region();
        final Region currentRegion = VanillaAPI.getRegionFromLocation(worldName, position);
        final @NotNull Region fromRegion = (packet.isRotation() && playerRegion != null) ? playerRegion : currentRegion;


        //teleport
        if (packet.getWrapper() instanceof WrapperPlayServerPlayerPositionAndLook wrap) {

            final Vector3d serverPosition = wrap.getPosition();
            final Region teleportRegion = VanillaAPI.getRegionFromLocation(worldName, serverPosition);

            final boolean cancel = !fromRegion.isFlagActive(FlagType.LEAVE, bukkitPlayer) || cannotJoin(teleportRegion, bukkitPlayer);

            if (cancel) {
                packet.cancel();
                return;
            }

            if (fromRegion.is(teleportRegion)) {
                if (serverPosition.distance(position) <= 0d) return;
                new RegionMoveEvent(bukkitPlayer, fromRegion, serverPosition, wrap.getYaw(), wrap.getPitch()).callEvent();
                return;
            }

            new RegionSwitchEvent(bukkitPlayer, teleportRegion, serverPosition, wrap.getYaw(), wrap.getPitch(), fromRegion).callEvent();
            this.player.region(teleportRegion);
            return;
        }

        if (playerRegion == null) {
            this.player.region(fromRegion);
            return;
        }

        final boolean cancel = !fromRegion.isFlagActive(FlagType.LEAVE, bukkitPlayer) || !currentRegion.isFlagActive(FlagType.JOIN, bukkitPlayer);
        if (cancel) {
            this.player.setBack();
            return;
        }

        if (fromRegion.is(currentRegion)) {
            new RegionMoveEvent(bukkitPlayer, fromRegion, position, this.positionProcessor.getYaw(), this.positionProcessor.getPitch()).callEvent();
            return;
        }

        new RegionSwitchEvent(bukkitPlayer, currentRegion, position, this.positionProcessor.getYaw(), this.positionProcessor.getPitch(), fromRegion).callEvent();
        this.player.region(currentRegion);
    }

    private boolean cannotJoin(Region region, final Player player) {
        return !region.isFlagActive(FlagType.JOIN, player);
        /*
        if (!region.isFlagActive(FlagType.JOIN, player)) return true;
        final boolean isCombatJoin = region.isFlagActive(FlagType.JOIN_COMBAT, player);
        final boolean inCombat = this.player.getCombatLogProcessor().inCombat();

        return !isCombatJoin || inCombat;
         */
    }

}
