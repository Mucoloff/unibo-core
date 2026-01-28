package dev.sweety.unibo.feature.info;


import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.file.language.Language;
import dev.sweety.unibo.utils.McUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@CommandWrapper.Info(name = "stats", description = "View your stats", permission = "unibo.stats")
public class StatsCommand extends CommandWrapper {

    private static final String EDIT = "unibo.stats.edit";

    public StatsCommand(final VanillaCore plugin) {
        super(plugin);
    }

    @Override
    public void execute(final Player player, String[] args) {
        if (args.length == 0) {
            showStats(player, player);
            return;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(Language.PLAYER__NOT__FOUND.get("%player%", args[0]));
                return;
            }
            showStats(player, target);
            return;
        }

        if (!player.hasPermission(EDIT)) {
            player.sendMessage(Language.NO__PERMISSION.component());
            return;
        }

        if (args.length == 4) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(Language.PLAYER__NOT__FOUND.get("%player%", args[1]));
                return;
            }

            Stats stats = plugin.playerManager().getProfile(target.getUniqueId()).stats();
            String value = args[3];

            switch (args[0].toLowerCase()) {
                case "set" -> {
                    switch (args[2].toLowerCase()) {
                        case "elo" -> stats.updateElo(-stats.getElo() + Double.parseDouble(value));
                        case "wins" -> stats.setWins(Integer.parseInt(value));
                        case "losses" -> stats.setLosses(Integer.parseInt(value));
                        default -> {
                            player.sendRichMessage("<red>Invalid stats!");
                            return;
                        }
                    }
                }
                case "add" -> {
                    switch (args[2].toLowerCase()) {
                        case "elo" -> stats.updateElo(Double.parseDouble(value));
                        case "wins" -> stats.setWins(stats.getWins() + Integer.parseInt(value));
                        case "losses" -> stats.setLosses(stats.getLosses() + Integer.parseInt(value));
                        default -> {
                            player.sendRichMessage("<red>Invalid stats!");
                            return;
                        }
                    }
                }
                case "remove" -> {
                    switch (args[2].toLowerCase()) {
                        case "elo" -> stats.updateElo(-Double.parseDouble(value));
                        case "wins" -> stats.setWins(stats.getWins() - Integer.parseInt(value));
                        case "losses" -> stats.setLosses(stats.getLosses() - Integer.parseInt(value));
                        default -> {
                            player.sendRichMessage("<red>Invalid stats!");
                            return;
                        }
                    }
                }
            }
            player.sendRichMessage("<red>Stat edited successfully!");
        }
    }

    @Override
    protected void tab(@NotNull CommandSender sender, @NotNull String[] args, List<String> suggestions) {
        if (args.length == 1) {
            if (sender.hasPermission(EDIT)) {
                suggestions.add("set");
                suggestions.add("add");
                suggestions.add("remove");
            }
            suggestions.addAll(McUtils.onlineNames());
        }

        if (args.length == 3 && sender.hasPermission(EDIT) && args[0].equalsIgnoreCase("edit")) {
            suggestions.addAll(Arrays.asList("elo", "wins", "losses"));
        }
    }

    private void showStats(Player viewer, Player target) {
        final Stats stats = plugin.playerManager().getProfile(target.getUniqueId()).stats();

        viewer.sendRichMessage("<gold>"+ target.getName() + "'s Stats");
        viewer.sendRichMessage("<gray>Elo: <white>" + String.format("%.2f",stats.getElo()));
        viewer.sendRichMessage("<gray>Wins: <white>" + stats.getWins());
        viewer.sendRichMessage("<gray>Losses: <white>" + stats.getLosses());
        viewer.sendRichMessage("<gray>K/D: <white>" + String.format("%.2f", stats.getKd()));
        viewer.sendRichMessage("<gray>Win Streak: <white>" + stats.getWinStreak());
        viewer.sendRichMessage("<gray>Lose Streak: <white>" + stats.getLoseStreak());
    }
}
