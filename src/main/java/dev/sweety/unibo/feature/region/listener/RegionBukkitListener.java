package dev.sweety.unibo.feature.region.listener;

import com.github.retrooper.packetevents.util.Vector3d;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.VanillaAPI;
import dev.sweety.unibo.api.flag.FlagType;
import dev.sweety.unibo.feature.region.Region;
import dev.sweety.unibo.player.PlayerManager;
import dev.sweety.unibo.player.VanillaPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

import static dev.sweety.unibo.api.flag.FlagType.*;
import static org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED;

public class RegionBukkitListener implements Listener {

    private final VanillaCore plugin;
    private final PlayerManager playerManager;

    public RegionBukkitListener(final VanillaCore plugin) {
        this.playerManager = (this.plugin = plugin).playerManager();
    }

    @EventHandler
    public void onDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        final VanillaPlayer player = this.player(p);

        if (player == null) return;
        final Region region = player.region();
        if (region == null) return;

        Double multiplier = region.getFlagStatus(DAMAGE);

        event.setDamage(event.getDamage() * multiplier);
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent event){
        final Player victim = event.getPlayer(), killer = victim.getKiller();
        if (killer == null) return;

        VanillaPlayer victimProfile = this.player(victim);

        plugin.matchHandler().handleMatchResult(killer, victim, victimProfile);
        plugin.matchHandler().handleDeathMessage(event::deathMessage, victim, victimProfile, victim.displayName(), victim.getName(), killer);

        //todo victimProfile.release();
    }


    @EventHandler
    private void onBlockPlace(final BlockPlaceEvent event) {
        final VanillaPlayer player = this.player(event.getPlayer());
        final Location location = event.getBlock().getLocation();

        this.block(event, location, player, BUILD);
    }

    @EventHandler
    private void onBlockBreak(final BlockBreakEvent event) {
        final VanillaPlayer player = this.player(event.getPlayer());
        final Location location = event.getBlock().getLocation();

        this.block(event, location, player, BREAK);
    }

    @EventHandler
    private void onInteract(final PlayerInteractEvent event) {
        final VanillaPlayer player = this.player(event.getPlayer());

        if (player == null) return;
        if (event.getInteractionPoint() == null && event.getClickedBlock() == null) return;

        final Location location = event.getInteractionPoint() == null ? Objects.requireNonNull(event.getClickedBlock()).getLocation() : event.getInteractionPoint();

        if (location == null) return;

        this.block(event, location, player, INTERACT_BLOCK);
    }

    @EventHandler
    public void onSaturation(final FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        final VanillaPlayer player = this.player(p);

        if (player == null) return;
        final Region region = player.region();
        if (region == null) return;

        this.cancel(event, player, region, SATURATION_CHANGE);
    }

    @EventHandler
    public void onHealth(final EntityRegainHealthEvent event) {
        if (!event.getRegainReason().equals(SATIATED)) return;
        if (!(event.getEntity() instanceof Player p)) return;
        final VanillaPlayer player = this.player(p);

        if (player == null) return;
        final Region region = player.region();
        if (region == null) return;


        this.cancel(event, player, region, REGEN_HEALTH);
    }

    /**
     * Global
     */

    private void cancel(final Event event, final VanillaPlayer player, final Region rg, final FlagType flag) {
        if (!(event instanceof Cancellable e)) return;
        final boolean can = rg.isFlagActive(flag, player.player());
        final boolean exempt = player.exempt(rg, flag.getName());

        if (!can && !exempt) e.setCancelled(true);
    }

    /**
     * RegionPlayer
     */

    private VanillaPlayer player(final Player player) {
        return this.playerManager.profile(player);
    }

    /**
     * Block Events Common
     */

    private void block(final Event event, final Location block, final VanillaPlayer player, final FlagType flagName) {
        final Region region = VanillaAPI.getRegionFromLocation(player.worldName(), new Vector3d(block.getX(), block.getY(), block.getZ()));
        this.cancel(event, player, region, flagName);
    }
}
