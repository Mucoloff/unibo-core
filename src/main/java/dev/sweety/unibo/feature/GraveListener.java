package dev.sweety.unibo.feature;

import dev.sweety.unibo.api.menu.SimpleMenu;
import dev.sweety.unibo.utils.ColorUtils;
import dev.sweety.unibo.utils.McUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.bukkit.Material.AIR;

public class GraveListener implements Listener {

    public static HashMap<Block, Grave> graves = new HashMap<>();

    @EventHandler
    public void onRespawn(PlayerRespawnEvent born) {
        Player p = born.getPlayer();
        if (graves.isEmpty()) return;
        graves.forEach((block, grave) -> {
            if (grave.player() != p) return;

            ItemStack paper = new ItemStack(Material.PAPER);
            String date = new SimpleDateFormat("dd/MM hh:mm:ss").format(new Date(grave.time()));
            ItemMeta meta = paper.getItemMeta();
            Location location = grave.deathLocation();
            String lore = String.format("%s\n&aDeath Time: &c%s", location(location, true), date);
            String name = String.format("&e%s's &7Grave", p.getName());
            meta.displayName(McUtils.component(name));
            meta.lore(McUtils.colorList(Arrays.stream(lore.split("\n")).toList()));
            paper.setItemMeta(meta);
            p.getInventory().addItem(paper);
        });
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        // drop grave items on break block
        Block block = e.getBlock();
        if (graves.containsKey(block)) {
            Grave grave = graves.get(block);
            e.setDropItems(false);
            grave.inventory().forEach(item -> {
                if (item != null) block.getWorld().dropItem(block.getLocation(), item);
            });
            graves.remove(block);
            Hologram h = DHAPI.getHologram(grave.player().getName() + grave.time());
            if (h != null) {
                h.delete();
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;

        final List<ItemStack> drops = e.getDrops();

        drops.removeIf(i -> i.displayName().contains(McUtils.component("'s &7Grave")));

        if (drops.isEmpty()) return;

        Location gayLocation = victim.getEyeLocation().add(0, -1, 0);
        Block block = gayLocation.getBlock();

        List<String> lines = new ArrayList<>(List.of("&c&lDEATH &f- &4" + victim.getName()));

        Player killer = victim.getKiller();
        if (killer != null) {
            lines.add("&7Killed by &f " + killer.getName());
        }

        block.setType(Material.PLAYER_HEAD);
        BlockState state = block.getState();
        Skull skull = (Skull) state;
        UUID uuid = victim.getUniqueId();

        skull.setProfile(ResolvableProfile.resolvableProfile(Bukkit.getServer().getOfflinePlayer(uuid).getPlayerProfile()));
        skull.update(true);

        try {
            float yaw = victim.getLocation().getYaw();
            yaw = (yaw % 360 + 360) % 360;
            int idx = Math.floorMod(Math.round(yaw / 90f), 4);
            org.bukkit.block.BlockFace[] faces = new org.bukkit.block.BlockFace[] {
                    org.bukkit.block.BlockFace.SOUTH,
                    org.bukkit.block.BlockFace.WEST,
                    org.bukkit.block.BlockFace.NORTH,
                    org.bukkit.block.BlockFace.EAST
            };
            Rotatable rot = (Rotatable) block.getBlockData();
            rot.setRotation(faces[idx]);
            block.setBlockData(rot, true);
        } catch (Throwable ignored) {}


        Inventory inv = Bukkit.createInventory(null, 4 * 9, Component.text(victim.getName() + "' grave"));

        drops.forEach(item -> {
            if (item != null) {
                inv.addItem(item);
            }
        });

        drops.clear();
        long t = System.currentTimeMillis();
        DHAPI.createHologram(victim.getName() + t, block.getLocation().add(.5, 1, .5), true, lines);
        graves.put(block, new Grave(victim, inv, gayLocation, t));
    }

    @EventHandler
    public void onGraveInventory(InventoryCloseEvent e) {
        Block block = e.getPlayer().getTargetBlock(null, 5);
        if (!graves.containsKey(block)) return;

        Grave grave = graves.get(block);

        Inventory inv = grave.inventory();
        if (!inv.equals(e.getInventory()) && !e.getView().title().contains(Component.text("'s grave"))) return;

        removeGraveBlock(block, grave, inv);
    }

    @EventHandler
    public void onGrave(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block block = e.getClickedBlock();

        if (block == null) return;

        if (!graves.containsKey(block)) return;

        if (!e.getAction().isRightClick()) return;

        Grave grave = graves.get(block);
        Inventory inv = grave.inventory();

        if (p.isSneaking()) {
            removeGraveBlock(block, grave, inv);
            return;
        }

        if (!inv.getViewers().isEmpty()) return;

        p.openInventory(inv);
    }

    private void removeGraveBlock(Block block, Grave grave, Inventory inv) {
        inv.forEach(item -> {
            if (item != null) block.getWorld().dropItem(block.getLocation(), item);
        });
        block.setType(AIR);
        Hologram h = DHAPI.getHologram(grave.player().getName() + grave.time());
        if (h != null) {
            h.delete();
        }
        graves.remove(block);
    }

    public static String location(Location location, boolean world) {

        return ColorUtils.color(world ? (String.format("&eWorld: &f%s\n&e[&6 %s / %s / %s &e]",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ())) : (String.format("&e[&6 %s / %s / %s &e]",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ())));
    }

    public record Grave(Player player, Inventory inventory, Location deathLocation, long time) {
    }
}
