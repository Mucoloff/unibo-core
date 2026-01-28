package dev.sweety.unibo.feature.essential;

import com.google.common.base.Joiner;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.feature.essential.teleport.*;
import dev.sweety.unibo.feature.info.StatsCommand;
import dev.sweety.unibo.feature.info.leaderboard.Leaderboard;
import dev.sweety.unibo.feature.inventory.ViewInv;
import dev.sweety.unibo.feature.region.command.RegionCommand;
import dev.sweety.unibo.file.Files;
import dev.sweety.unibo.utils.McUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class CommandRegistry {

    private final List<CommandWrapper> commands = new ArrayList<>();
    private final VanillaCore plugin;

    public static CommandWrapper.Suggestion allPlayers = (player, args, suggestions) -> {
        if (args.length == 1) {
            suggestions.add("*");
            suggestions.addAll(McUtils.onlineNames());
        }
    };

    public CommandRegistry(final VanillaCore plugin) {
        this.plugin = plugin;


        this.commands.add(CommandWrapper.action(plugin, "hat", ((player, args) -> {
            PlayerInventory inventory = player.getInventory();

            ItemStack hat = inventory.getHelmet();
            ItemStack hand = inventory.getItemInMainHand();

            inventory.setItemInMainHand(hat);
            try {
                inventory.setHelmet(hand);
            } catch (Exception e) {
                inventory.setHelmet(hat);
                inventory.setItemInMainHand(hand);
            }
        })).build());

        this.commands.add(CommandWrapper.action(plugin, "reload-" + plugin.name(), ((player, args) -> {
            if (!player.hasPermission("unibo.reload")) {
                player.sendRichMessage("<red>You don't have permission to use this command.");
                return;
            }
            plugin.instance().reloadConfig();

            Files.LANGUAGE.reload();


            Files.PLAYER_ELO.reload();
            plugin.playerManager().foreachProfile(profile -> profile.reloadStats(Files.PLAYER_ELO::load));

        })).build());


        this.commands.add(
                CommandWrapper.action(plugin, "broadcast", (sender, args) -> McUtils.broadcast(plugin.config().getString("broadcast.format", "&#F45454ʙ&#F5694Aʀ&#F67F3Fᴏ&#F79435ᴀ&#F8AA2Aᴅ&#F9BF20ᴄ&#FAD415ᴀ&#FBEA0Bs&#FCFF00ᴛ &7%message%").replace("%message%", Joiner.on(" ").join(args))))
                        .description("broadcast message")
                        .permission("unibo.broadcast")
                        .alias("bc")
                        .build()
        );

        this.commands.add(CommandWrapper.action(plugin, "leaderboard", (player, args) -> Leaderboard.Menu.INSTANCE.open(player)).build());

        this.commands.add(
                CommandWrapper.action(plugin, "enderchest", (player, args) -> player.openInventory(player.getEnderChest()))
                        .description("opens your enderchest")
                        .permission("unibo.enderchest")
                        .alias("ec")
                        .build()
        );
    }

    public void register() {
        Gamemode.register(plugin);
        Spawn.register(plugin);
        FeedHeal.register(plugin);
        FlyVanish.register(plugin);
        this.commands.addAll(List.of(
                //features
                new RegionCommand(plugin),
                new ViewInv(plugin),
                new StatsCommand(plugin),
                // - teleport
                new Teleport(plugin),
                new TeleportAll(plugin),
                new TeleportHere(plugin),
                // - player
                new Speed(plugin),
                new Sudo(plugin)
        ));
        this.commands.forEach(CommandWrapper::register);
    }
}
