package dev.sweety.unibo.feature.essential;

import com.google.common.base.Joiner;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.utils.McUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static dev.sweety.unibo.file.Files.LANGUAGE;

@CommandWrapper.Info(name = "sudo", permission = "unibo.staff.sudo", description = "Sudo a player", player = false)
public class Sudo extends CommandWrapper {

    public Sudo(VanillaCore plugin) {
        super(plugin);
    }

    @Override
    protected void tab(@NotNull CommandSender sender, @NotNull String[] args, List<String> suggestions) {
        suggestions.addAll(McUtils.onlineNames());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player t = Bukkit.getPlayer(args[0]);
        String[] mess = Arrays.copyOfRange(args, 1, args.length);
        String message = Joiner.on(" ").join(mess);
        if (t == null) {
            if (args[0].equalsIgnoreCase("*")) {
                Bukkit.getOnlinePlayers().forEach(player -> sudo(sender, message, player));
                return;
            }
            sender.sendMessage(LANGUAGE.getConfig().getString("player-not-online").replace("%player%", args[0]));
            return;
        }
        sudo(sender, message, t);
    }

    private void sudo(CommandSender sender, String message, Player t) {
        if (message.startsWith("c:")) {
            message = message.substring(2);
            sender.sendRichMessage("<yellow>[SUDO] %s: %s".formatted(t.getName(), message));
            t.chat(message);
            return;
        }
        sender.sendRichMessage("<yellow>[SUDO] %s: %s".formatted(t.getName(), message));
        McUtils.Command.execute(t, message);
    }
}
