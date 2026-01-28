package dev.sweety.unibo.feature.essential;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.file.language.Language;
import dev.sweety.unibo.utils.McUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@UtilityClass
public class Gamemode {

    public void register(final VanillaCore plugin) {

        String permission = "unibo.staff.gamemode";

        CommandWrapper.action(plugin, "gamemode", (sender, args) -> {
                    if (args.length < 1) return;

                    GameMode gameMode = getGameMode(args[0].toLowerCase());
                    if (gameMode == null) {
                        sender.sendMessage(Language.GAMEMODE_ERROR.component());
                        return;
                    }

                    setGameMode(sender, args, gameMode);
                }).alias("gm")
                .permission(permission)
                .suggestion((player, args, suggestions) -> {
                    if (args.length == 1) {
                        suggestions.addAll(List.of("adventure", "creative", "spectator", "survival"));
                    } else if (args.length == 2) {
                        suggestions.add("*");
                        suggestions.addAll(McUtils.onlineNames());
                    }
                })
                .register();


        for (GameMode value : GameMode.values()) {
            String lower = value.name().toLowerCase();

            CommandWrapper.action(plugin, lower, (sender, args) -> setGameMode(sender, args, value)).alias("gm" + lower + (value == GameMode.SPECTATOR ? "p" : ""))
                    .permission(permission + "." + lower)
                    .suggestion(CommandRegistry.allPlayers)
                    .register();
        }
    }

    public void setGameMode(CommandSender sender, String[] args, GameMode gameMode) {
        if (args.length >= 1 && sender.hasPermission("unibo.staff.gamemode." + gameMode.name().toLowerCase() + ".other")) {
            handleGameModeChange(sender, gameMode, args[args.length - 1]);
        } else if (sender instanceof Player player) {
            sendMessage(sender, gameMode, "");
            player.setGameMode(gameMode);
        }
    }

    public GameMode getGameMode(final String mode) {
        return switch (mode) {
            case "adventure", "a", "2" -> GameMode.ADVENTURE;
            case "creative", "c", "1" -> GameMode.CREATIVE;
            case "spectator", "sp", "3" -> GameMode.SPECTATOR;
            case "survival", "s", "0" -> GameMode.SURVIVAL;
            case null, default -> null;
        };
    }

    public void handleGameModeChange(@NotNull CommandSender sender, GameMode gameMode, String target) {
        if (target.equalsIgnoreCase("*")) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                sendMessage(sender, gameMode, player.getName());
                player.setGameMode(gameMode);
            });
            return;
        }


        Player player = Bukkit.getPlayer(target);
        if (player == null) {
            if (getGameMode(target) == null) {

                sender.sendMessage(Language.PLAYER__NOT__FOUND.component("%player%", target));
                return;
            }
            if (!(sender instanceof Player p)) return;
            player = p;
        }

        player.setGameMode(gameMode);
        sendMessage(sender, gameMode, target);
    }

    private void sendMessage(@NotNull CommandSender sender, GameMode gameMode, String playerName) {
        sender.sendMessage((playerName.isEmpty() ? Language.GAMEMODE_SET : Language.GAMEMODE_OTHER).component("%gamemode%", gameMode.name().toLowerCase(), "%player%", playerName));
    }

}
