package dev.sweety.unibo.utils;

import dev.sweety.core.math.MathUtils;
import dev.sweety.unibo.api.VanillaAPI;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@UtilityClass
public class McUtils {

    public static @NotNull Component component(Object o) {
        return Component.text(ColorUtils.color(o));
    }

    public static List<Component> colorList(@NotNull List<String> list) {
        return list.stream().map(McUtils::component).toList();
    }

    @NotNull
    public List<String> onlineNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    public void safeGive(Player p, ItemStack giveItem) {
        int amount = giveItem.getAmount();
        giveItem.setAmount(1);
        for (int i = 0; i < amount; i++) {
            p.getInventory().addItem(giveItem);
        }
    }

    public int freeSlots(Player p) {
        return (int) Arrays.stream(p.getInventory().getStorageContents()).filter(Objects::isNull).count();
    }

    public void addPermission(UUID userUuid, String permission) {
        VanillaAPI.luckperms().getUserManager().modifyUser(userUuid, user -> user.data().add(Node.builder(permission).build()));
    }

    public void broadcast(final String message) {
        broadcast(McUtils.component(message));
    }

    public void broadcast(final Component message) {

        if (VanillaAPI.config().getBoolean("broadcast.console", false))
            Bukkit.getConsoleSender().sendMessage(message);
        MathUtils.parallel(Bukkit.getOnlinePlayers()).forEach(p -> p.sendMessage(message));
    }

    @UtilityClass
    public class Command {

        public void execute(String command) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        public void execute(CommandSender sender, String command) {
            Bukkit.dispatchCommand(sender, command);
        }

    }

    public static ItemStack createGlass(Material mat) {
        ItemStack item = new ItemStack(mat);
        item.editMeta(meta -> {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setHideTooltip(true);
            meta.displayName(Component.empty());
        });
        return item;
    }

}
