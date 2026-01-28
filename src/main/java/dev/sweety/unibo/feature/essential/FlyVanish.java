package dev.sweety.unibo.feature.essential;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import static dev.sweety.unibo.file.Files.LANGUAGE;

@UtilityClass
public class FlyVanish {

    public static final Set<Player> fly = new HashSet<>();
    public static final Set<Player> vanish = new HashSet<>();

    public void register(final VanillaCore plugin) {

        BiConsumer<Player, Boolean> toggleFly = (player, active) -> {
            player.sendMessage(LANGUAGE.getString("fly." + (active ? "enable" : "disable")));
            player.setAllowFlight(active);
            player.setFlying(active);
        };

        BiConsumer<Player, Boolean> toggleVanish = (player, active) -> {
            String messageKey = active ? "vanish.enable" : "vanish.disable";
            player.sendMessage(LANGUAGE.getComponent(messageKey));

            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                if (active) {
                    if (!onlinePlayer.equals(player)) {
                        onlinePlayer.hidePlayer(plugin.instance(), player);
                    }
                } else {
                    onlinePlayer.showPlayer(plugin.instance(), player);
                }
            });
        };

        CommandWrapper.action(plugin, "fly", (player, args) -> execute(player, fly, toggleFly))
                .permission("unibo.staff.fly")
                .register();
        CommandWrapper.action(plugin, "vanish", (player, args) -> execute(player, vanish, toggleVanish))
                .permission("unibo.staff.vanish")
                .register();

    }

    private void execute(Player p, Set<Player> set, BiConsumer<Player, Boolean> toggle) {
        if (set.contains(p)) {
            toggle.accept(p, false);
            set.remove(p);
            return;
        }
        toggle.accept(p, true);
        set.add(p);
    }


}
