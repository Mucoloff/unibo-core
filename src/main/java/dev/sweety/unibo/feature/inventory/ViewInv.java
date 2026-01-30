package dev.sweety.unibo.feature.inventory;


import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.file.language.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

@CommandWrapper.Info(name = "viewinv", permission = "unibo.default.viewinv")
public class ViewInv extends CommandWrapper {

    private final Map<UUID, Views> viewInventory;

    public ViewInv(final VanillaCore plugin) {
        super(plugin);
        this.viewInventory = plugin.matchHandler().getViewInventory();
    }

    @Override
    public void execute(final Player player, final String[] args) {
        if (args.length != 1) {
            player.sendMessage("Usage: /viewinv <player>");
            return;
        }
        final Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Language.PLAYER__NOT__FOUND.component("%player%", args[0]));
            return;
        }

        final Views view = this.viewInventory.get(target.getUniqueId());

        if (view == null) {
            player.sendRichMessage("<red>Inventory expired");
            return;
        }

        view.open(player);
    }
}