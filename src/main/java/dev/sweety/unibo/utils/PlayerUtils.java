package dev.sweety.unibo.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public final class PlayerUtils {

    public final double DEFAULT_MAX_HEALTH = 20.0D;
    private final float DEFAULT_EXHAUSTION = 0.0F;
    private final float DEFAULT_SATURATION = 5.0F;
    private final int DEFAULT_MAX_FOOD_LEVEL = 20;

    public double getMaxHealth(final Player player) {
        final AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) return DEFAULT_MAX_HEALTH;
        return attribute.getValue();
    }

    public void setMaxHealth(final Player player) {
        player.setHealth(getMaxHealth(player));
    }

    public void feed(final Player player){
        player.setExhaustion(DEFAULT_EXHAUSTION);
        player.setSaturation(DEFAULT_SATURATION);
        player.setFoodLevel(DEFAULT_MAX_FOOD_LEVEL);
    }

    public void reset(final Player player) {
        player.setFireTicks(0);
        //player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.clearActivePotionEffects();
        setMaxHealth(player);
        feed(player);
        player.setItemOnCursor(null);

        final Inventory top = player.getOpenInventory().getTopInventory();

        if (top.getType() == InventoryType.CRAFTING) {
            top.clear();
        }

        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().clear();
        player.updateInventory();
    }
}
