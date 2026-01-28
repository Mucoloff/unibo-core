package dev.sweety.unibo.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface Menu extends InventoryHolder {

    void click(final Player player, final int slot);

    void setItem(final int slot, final ItemStack item);

    void setItem(final int slot, final ItemStack item, final Consumer<Player> action);

    void setup();

    default void open(final Player player) {
        final Inventory inv = getInventory();
        inv.clear();
        setup();
        player.openInventory(inv);
    }
}
