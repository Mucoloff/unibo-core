package dev.sweety.unibo.feature.essential;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.feature.CommandRegistry;
import dev.sweety.unibo.utils.PlayerUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static dev.sweety.unibo.file.Files.LANGUAGE;

@UtilityClass
public class FeedHeal {

    public void register(final VanillaCore plugin) {

        Consumer<Player> feed = t -> {
            PlayerUtils.feed(t);
            t.sendMessage(LANGUAGE.getString("feed"));
        };

        Consumer<Player> heal = t -> {
            PlayerUtils.setMaxHealth(t);
            t.sendMessage(LANGUAGE.getString("heal"));
            t.clearActivePotionEffects();
        };

        CommandWrapper.builder(plugin, "heal", ((sender, args) -> execute(sender, args, heal, "heal"))).suggestion(CommandRegistry.allPlayers).register();
        CommandWrapper.builder(plugin, "feed", ((sender, args) -> execute(sender, args, feed, "feed"))).suggestion(CommandRegistry.allPlayers).register();

    }

    private void execute(CommandSender sender, String[] args, Consumer<Player> action, String name) {
        if (args.length >= 1 && sender.hasPermission("unibo.staff.other."+name)) {
            String c = args[0];
            if (c.equalsIgnoreCase("*")) {
                Bukkit.getOnlinePlayers().forEach(t -> {
                    action.accept(t);

                    sender.sendMessage(LANGUAGE.getConfig().getString(name+"-other").replace("%player%", t.getName()));
                });
                return;
            }
            Player t = Bukkit.getPlayer(c);
            if (t != null) {
                action.accept(t);
                sender.sendMessage(LANGUAGE.getConfig().getString(name+"-other").replace("%player%", t.getName()));
                return;
            }
            sender.sendMessage(LANGUAGE.getConfig().getString("player-not-online").replace("%player%", c));
            return;
        }
        if (!(sender instanceof Player t)) return;
        action.accept(t);
    }

}
