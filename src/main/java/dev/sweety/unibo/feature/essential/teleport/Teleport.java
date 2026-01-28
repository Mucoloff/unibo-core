package dev.sweety.unibo.feature.essential.teleport;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.utils.McUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.sweety.unibo.file.Files.LANGUAGE;

@CommandWrapper.Info(name = "teleport", permission = "unibo.staff.teleport", aliases = "tp", player = false)
public class Teleport extends CommandWrapper {

    public Teleport(VanillaCore plugin) {
        super(plugin);
    }

    @Override
    protected void tab(@NotNull CommandSender sender, @NotNull String[] args, List<String> suggestions) {
        if (args.length == 1 || args.length == 2 || args.length == 5) {
            suggestions.addAll(McUtils.onlineNames());
            return;
        }
        if (args.length == 4) {
            suggestions.addAll(Bukkit.getWorlds().stream().map(World::getName).toList());
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {

            sender.sendMessage(LANGUAGE.getComponent("teleport.not-a-player"));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(LANGUAGE.getComponent("teleport.no-args"));
            player.sendMessage(LANGUAGE.getComponent("teleport.usage-self"));
            player.sendMessage(LANGUAGE.getComponent("teleport.usage-others"));
            return;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(LANGUAGE.getComponent("teleport.player-not-found").replaceText(b -> b.matchLiteral("%player%").replacement(args[0])));
                return;
            }
            player.teleportAsync(target.getLocation());
            player.sendMessage(LANGUAGE.getComponent("teleport.self-success").replaceText(b -> b.matchLiteral("%player%").replacement(target.getName())));
            return;
        }

        if (args.length == 2) {
            Player playerToSend = Bukkit.getPlayer(args[0]);
            Player target = Bukkit.getPlayer(args[1]);

            if (playerToSend == null) {
                player.sendMessage(LANGUAGE.getComponent("teleport.player-not-found").replaceText(b -> b.matchLiteral("%player%").replacement(args[0])));
                return;
            }

            if (target == null) {
                player.sendMessage(LANGUAGE.getComponent("teleport.player-not-found").replaceText(b -> b.matchLiteral("%player%").replacement(args[1])));
                return;
            }

            playerToSend.teleportAsync(target.getLocation());
            player.sendMessage(LANGUAGE.getComponent("teleport.others-success")
                    .replaceText(b -> b.matchLiteral("%player_to_send%").replacement(playerToSend.getName()))
                    .replaceText(b -> b.matchLiteral("%target%").replacement(target.getName())));
            return;
        }

        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        double z = Double.parseDouble(args[2]);
        Player target = args.length >= 5 ? Bukkit.getPlayer(args[4]) : player;
        if (target == null) {
            player.sendMessage(LANGUAGE.getComponent("teleport.player-not-found").replaceText(b -> b.matchLiteral("%player%").replacement(args[1])));
            return;
        }
        World world = args.length >= 4 ? Bukkit.getWorld(args[3]) : target.getWorld();
        Location loc = new Location(world, x, y, z);
        target.teleportAsync(loc);

        player.sendMessage(LANGUAGE.getComponent("teleport.coords-success")
                .replaceText(b -> b.matchLiteral("%player%").replacement(target.getName()))
                .replaceText(b -> b.matchLiteral("%target%").replacement(loc.serialize().toString())));
    }
}
