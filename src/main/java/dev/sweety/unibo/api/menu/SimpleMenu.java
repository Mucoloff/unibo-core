package dev.sweety.unibo.api.menu;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public abstract class SimpleMenu implements Menu {
    private final Map<Integer, Consumer<Player>> actions = new HashMap<>();

    private final Inventory inventory;

    public SimpleMenu(Rows rows, Component title) {
        this.inventory = Bukkit.createInventory(this, rows.getSize(), title);
    }

    @Override
    public void click(final Player player, int slot) {
        final Consumer<Player> action = this.actions.get(slot);
        if (action != null) action.accept(player);
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        setItem(slot, item, p -> {
        });
    }

    public void setItem(Rows row, int position, ItemStack item) {
        setItem(row, position, item, p -> {
        });
    }

    @Override
    public void setItem(int slot, ItemStack item, Consumer<Player> action) {
        this.actions.put(slot, action);
        getInventory().setItem(slot, item);
    }

    public void setItem(Rows row, int position, ItemStack item, Consumer<Player> action) {
        setItem(row.getSize() - 9 + position, item, action);
    }

    @Override
    public abstract void setup();

    @Getter
    public enum Rows {
        ONE(9),
        TWO(18),
        THREE(27),
        FOUR(36),
        FIVE(45),
        SIX(54);

        private final int size;

        Rows(int size) {
            this.size = size;
        }

    }

}
