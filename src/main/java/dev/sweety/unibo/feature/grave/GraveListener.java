package dev.sweety.unibo.feature.grave;

import dev.sweety.unibo.api.VanillaAPI;
import dev.sweety.unibo.utils.ColorUtils;
import dev.sweety.unibo.utils.McUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.bukkit.Material.AIR;

public class GraveListener implements Listener {

    public static HashMap<Block, Grave> graves = new HashMap<>();

    static NamespacedKey key;

    public GraveListener() {
        key = new NamespacedKey(VanillaAPI.instance(), "grave");
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent born) {
        Player p = born.getPlayer();
        if (graves.isEmpty()) return;
        graves.forEach((block, grave) -> {
            if (!p.getUniqueId().equals(grave.uuid())) return;

            ItemStack paper = grave.item();

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
            Hologram h = holo(grave.holoName());
            if (h != null) h.delete();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        final Player victim = e.getPlayer();

        final List<ItemStack> drops = e.getDrops();

        Predicate<ItemStack> removeCondition = i -> {
            if (!i.hasItemMeta()) return false;
            return i.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
        };

        drops.removeIf(removeCondition);

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
            org.bukkit.block.BlockFace[] faces = new org.bukkit.block.BlockFace[]{
                    org.bukkit.block.BlockFace.SOUTH,
                    org.bukkit.block.BlockFace.WEST,
                    org.bukkit.block.BlockFace.NORTH,
                    org.bukkit.block.BlockFace.EAST
            };
            Rotatable rot = (Rotatable) block.getBlockData();
            rot.setRotation(faces[idx]);
            block.setBlockData(rot, true);
        } catch (Throwable ignored) {
        }

        Inventory inv = Bukkit.createInventory(null, 4 * 9, Component.text(victim.getName() + "' grave"));

        drops.forEach(item -> {
            if (item != null) {
                inv.addItem(item);
            }
        });

        drops.clear();
        Grave grave = new Grave(victim.getUniqueId(), victim.getName(), inv, gayLocation, System.currentTimeMillis());
        create(grave.holoName(), block.getLocation().add(.5, 1, .5), lines);
        graves.put(block, grave);
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
        Hologram h = holo(grave.holoName());
        if (h != null) h.delete();
        graves.remove(block);
    }

    private static Hologram holo(String name) {
        return DHAPI.getHologram(name);
    }

    private static void create(String name, Location location, List<String> lines) {
        DHAPI.createHologram(name, location, true, lines);
    }

}
