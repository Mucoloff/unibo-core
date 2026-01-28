package dev.sweety.unibo.feature.essential.teleport;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static dev.sweety.unibo.file.Files.LANGUAGE;

@CommandWrapper.Info(name = "teleportall", permission = "unibo.staff.tpall", aliases = "tpall", player = false)
public class TeleportAll extends CommandWrapper {

    public TeleportAll(VanillaCore plugin) {
        super(plugin);
    }

    @Override
    protected void tab(@NotNull CommandSender sender, @NotNull String[] args, List<String> suggestions) {
        // No tab completion needed for this command
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) return;

        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

        if (players.size() == 1) {
            // Only the sender is online
            p.sendMessage(LANGUAGE.getComponent("teleport.all.no-other-players"));
        } else if (players.size() > 1) {
            for (final Player player : players) {
                if (!player.equals(p)) {
                    player.teleportAsync(p.getLocation());
                }
            }
            // Send a message indicating how many players were teleported
            p.sendMessage(LANGUAGE.getComponent("teleport.all.success")
                    .replaceText(builder -> builder.matchLiteral("%player_count%").replacement(Component.text(players.size() - 1))));
        }
    }
}
