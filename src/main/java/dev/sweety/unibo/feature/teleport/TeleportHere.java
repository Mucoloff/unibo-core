package dev.sweety.unibo.feature.teleport;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.utils.McUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.sweety.unibo.file.Files.LANGUAGE;


@CommandWrapper.Info(name = "teleporthere", permission = "unibo.staff.tphere", aliases = {"tph", "tphere"}, player = false)
public class TeleportHere extends CommandWrapper {

    public TeleportHere(VanillaCore plugin) {
        super(plugin);
    }

    @Override
    protected void tab(@NotNull CommandSender sender, @NotNull String[] args, List<String> suggestions) {
        if (args.length == 1) {
            suggestions.addAll(McUtils.onlineNames());
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) return;

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                target.teleportAsync(p.getLocation());
                p.sendMessage(LANGUAGE.getComponent("teleport.here.success")
                        .replaceText(builder -> builder.matchLiteral("%target%").replacement(target.name())));
            } else {
                p.sendMessage(LANGUAGE.getComponent("teleport.player-not-found")
                        .replaceText(builder -> builder.matchLiteral("%player%").replacement(Component.text(args[0]))));
            }
        } else {
            p.sendMessage(LANGUAGE.getComponent("teleport.here.usage"));
        }
    }
}
