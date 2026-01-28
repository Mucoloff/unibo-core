package dev.sweety.unibo.feature.inventory;

import dev.sweety.unibo.utils.McUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.sweety.unibo.utils.McUtils.createGlass;

@Getter
public class Views implements InventoryHolder {

    private final Inventory inventory;
    private static final ItemStack BACKGROUND = createGlass(Material.BLACK_STAINED_GLASS_PANE);

    public Views(final Player player) {
        this.inventory = Bukkit.createInventory(this, 5 * 9, player.displayName().append(Component.text(" inventory")).color(NamedTextColor.YELLOW));

        final PlayerInventory inv = player.getInventory();

        for (int i = 0; i < 5 * 9; i++) {
            this.inventory.setItem(i, BACKGROUND);
        }

        final @Nullable ItemStack @NotNull [] contents = inv.getStorageContents();
        final @Nullable ItemStack @NotNull [] armorContents = inv.getArmorContents();

        final ItemStack off = inv.getItemInOffHand();
        if (off.getType() != Material.AIR) {
            this.inventory.setItem(7, off);
        }

        for (int i = 0; i < armorContents.length; i++) {
            ItemStack armor = armorContents[armorContents.length - 1 - i];
            this.inventory.setItem(i, armor);
        }

        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> {
            meta.setOwningPlayer(player);
            meta.displayName(player.displayName().color(NamedTextColor.YELLOW));
            meta.lore(McUtils.colorList(List.of( String.format("&b%.2f", player.getHealth() / 2.0d) + "❤"))); //todo match infos
        });

        this.inventory.setItem(8, head);
        for (int i = 0; i < contents.length && i + 9 < this.inventory.getSize(); i++) {
            ItemStack content = contents[i];
            this.inventory.setItem(i + 9, content);
        }
    }

    public void open(final Player player) {
        player.openInventory(this.inventory);
    }

    public static class Handler implements Listener {
        @EventHandler
        public void onClick(final InventoryClickEvent event) {
            if (!(event.getView().getTopInventory().getHolder() instanceof Views editor)) return;
            event.setCancelled(true);
        }

    }
}
