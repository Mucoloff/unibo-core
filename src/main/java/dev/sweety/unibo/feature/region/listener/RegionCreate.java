package dev.sweety.unibo.feature.region.listener;


import dev.sweety.core.math.Pair;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.feature.region.command.RegionCommand;
import dev.sweety.unibo.file.language.Language;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class RegionCreate implements Listener {

    private final VanillaCore plugin;
    public RegionCreate(final VanillaCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRegionCreate(final PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        final Block block = e.getClickedBlock();

        if (!player.isOp()) return;

        final ItemStack item = e.getItem();
        final Location location = block != null ? block.getLocation() : e.getInteractionPoint();

        if (location == null || item == null || !item.getType().equals(Material.GOLDEN_HOE)) return;

        final Action action = e.getAction();
        final Pair<Location> locations = RegionCommand.LOCATIONS.getOrDefault(player.getUniqueId(), new Pair<>());

        if (action.equals(Action.LEFT_CLICK_BLOCK)) {
            locations.setFirst(location);
            player.sendMessage(Language.REGIONS_POS_FIRST.component("%x%", String.valueOf(location.getX()), "%y%", String.valueOf(location.getY()), "%z%", String.valueOf(location.getZ())));
        }

        if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            locations.setSecond(location);
            player.sendMessage(Language.REGIONS_POS_SECOND.component("%x%", String.valueOf(location.getX()), "%y%", String.valueOf(location.getY()), "%z%", String.valueOf(location.getZ())));
        }

        RegionCommand.LOCATIONS.put(player.getUniqueId(), locations);

        e.setCancelled(true);
    }

}
