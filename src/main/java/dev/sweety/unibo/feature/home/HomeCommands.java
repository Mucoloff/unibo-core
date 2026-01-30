package dev.sweety.unibo.feature.home;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.api.serializable.Position;
import dev.sweety.unibo.player.VanillaPlayer;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class HomeCommands {

    public void register(final VanillaCore plugin) {

        CommandWrapper.action(plugin, "sethome", (player, args) -> {
            if (args.length != 1) {
                player.sendRichMessage("<red>Usage: /sethome <name>");
                return;
            }

            String name = args[0];

            final VanillaPlayer profile = plugin.playerManager()
                    .profile(player);

            boolean success = profile
                    .homes()
                    .setHome(name, Position.fromPELocation(profile.worldName(), profile.positionProcessor().location()));

            if (success) {
                player.sendRichMessage("<green>Home '" + name + "' set!");
            } else {
                player.sendRichMessage("<red>You have reached the maximum number of homes.");
            }
        }).permission("unibo.default.home.set").register();

        CommandWrapper.Suggestion listHome = (sender, args, suggestions) -> {
            if (!(sender instanceof Player player)) return;
            if (args.length == 1) {
                final VanillaPlayer profile = plugin.playerManager()
                        .profile(player);
                suggestions.addAll(profile.homes().getHomes().keySet());
            }
        };

        CommandWrapper.action(plugin, "home", (player, args) -> {
            final VanillaPlayer profile = plugin.playerManager()
                    .profile(player);
            if (args.length != 1) {
                player.sendRichMessage("<red>Usage: /home <name>");
                player.sendRichMessage("<red>Available homes: " + String.join(", ", profile
                        .homes()
                        .getHomes()
                        .keySet()));
                return;
            }

            String name = args[0];
            final Position homePosition = profile
                    .homes()
                    .home(name);

            if (homePosition == null) {
                player.sendRichMessage("<red>Home '" + name + "' does not exist.");
                return;
            }

            homePosition.teleport(player);
            player.sendRichMessage("<green>Teleported to home '" + name + "'.");
        }).permission("unibo.default.home.use").suggestion(listHome).register();

        CommandWrapper.action(plugin, "delhome", (player, args) -> {
            if (args.length != 1) {
                player.sendRichMessage("<red>Usage: /delhome <name>");
                return;
            }

            String name = args[0];

            final VanillaPlayer profile = plugin.playerManager()
                    .profile(player);

            final Position removed = profile
                    .homes()
                    .delHome(name);

            if (removed == null) {
                player.sendRichMessage("<red>Home '" + name + "' does not exist.");
                return;
            }

            player.sendRichMessage("<green>Home '" + name + "' deleted.");
        }).permission("unibo.default.home.delete").suggestion(listHome).register();

    }

}
