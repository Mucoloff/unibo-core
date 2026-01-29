package dev.sweety.unibo.feature.essential;

import com.google.common.base.Joiner;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.feature.info.StatsCommand;
import dev.sweety.unibo.feature.info.leaderboard.Leaderboard;
import dev.sweety.unibo.feature.inventory.ViewInv;
import dev.sweety.unibo.feature.region.command.RegionCommand;
import dev.sweety.unibo.feature.teleport.*;
import dev.sweety.unibo.file.Files;
import dev.sweety.unibo.player.features.CombatLogProcessor;
import dev.sweety.unibo.player.features.CombatStatus;
import dev.sweety.unibo.utils.McUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class CommandRegistry {

    private final List<CommandWrapper> commands = new ArrayList<CommandWrapper>();
    private final VanillaCore plugin;
    public static CommandWrapper.Suggestion allPlayers = (player, args, suggestions) -> {
        if (args.length == 1) {
            suggestions.add("*");
            suggestions.addAll(McUtils.onlineNames());
        }
    };

    public CommandRegistry(VanillaCore plugin) {
        this.plugin = plugin;
        this.commands.add(CommandWrapper.action(plugin, "hat", (player, args) -> {
            PlayerInventory inventory = player.getInventory();
            ItemStack hat = inventory.getHelmet();
            ItemStack hand = inventory.getItemInMainHand();
            inventory.setItemInMainHand(hat);
            try {
                inventory.setHelmet(hand);
            }
            catch (Exception e) {
                inventory.setHelmet(hat);
                inventory.setItemInMainHand(hand);
            }
        }).build());
        this.commands.add(CommandWrapper.action(plugin, "reload-" + plugin.name(), (player, args) -> {
            if (!player.hasPermission("unibo.reload")) {
                player.sendRichMessage("<red>You don't have permission to use this command.");
                return;
            }
            plugin.instance().reloadConfig();
            Files.LANGUAGE.reload();
            Files.PLAYER_ELO.reload();
            plugin.playerManager().foreachProfile(profile -> profile.reloadStats(Files.PLAYER_ELO::load));
        }).build());
        this.commands.add(CommandWrapper.action(plugin, "broadcast", (sender, args) -> McUtils.broadcast(plugin.config().getString("broadcast.format", "&#F45454ʙ&#F5694Aʀ&#F67F3Fᴏ&#F79435ᴀ&#F8AA2Aᴅ&#F9BF20ᴄ&#FAD415ᴀ&#FBEA0Bs&#FCFF00ᴛ &7%message%").replace("%message%", Joiner.on(" ").join(args)))).description("broadcast message").permission("unibo.broadcast").alias("bc").build());
        this.commands.add(CommandWrapper.action(plugin, "leaderboard", (player, args) -> Leaderboard.Menu.INSTANCE.open(player)).build());
        this.commands.add(CommandWrapper.action(plugin, "enderchest", (player, args) -> player.openInventory(player.getEnderChest())).description("opens your enderchest").permission("unibo.enderchest").alias("ec").build());
        this.commands.add(CommandWrapper.action(plugin, "combat", (player, args) -> {
            if (args.length == 0) {
                CombatStatus status = plugin.playerManager().profile(player.getUniqueId()).combatStatus();
                String color = switch (status) {
                    case CombatStatus.IDLE -> "<green>";
                    case CombatStatus.ENGAGED -> "<red>";
                    case CombatStatus.DISABLED -> "<gray>";
                };
                player.sendRichMessage("<yellow>Combat: " + color + status);
            } else {

                CombatLogProcessor processor = plugin.playerManager().profile(player.getUniqueId()).combatLogProcessor();
                boolean status = args[0].equalsIgnoreCase("toggle") ? !processor.isEnabled() : Boolean.parseBoolean(args[0]);
                if (processor.inCombat()) {
                    player.sendRichMessage("<red>You cannot change combat status while in combat!");
                    return;
                }
                processor.setEnabled(status);
                player.sendRichMessage("<yellow>Combat " + (status ? "<green>enabled" : "<red>disabled"));
            }
        }).alias("ct").suggestion((player, args, suggestions) -> {
            if (args.length == 1) {
                suggestions.add("true");
                suggestions.add("false");
                suggestions.add("toggle");
            }
        }).build());
    }

    public void register() {
        Gamemode.register(this.plugin);
        Spawn.register(this.plugin);
        FeedHeal.register(this.plugin);
        FlyVanish.register(this.plugin);
        this.commands.addAll(List.of(new RegionCommand(this.plugin), new ViewInv(this.plugin), new StatsCommand(this.plugin), new Teleport(this.plugin), new TeleportAll(this.plugin), new TeleportHere(this.plugin), new Speed(this.plugin), new Sudo(this.plugin)));
        this.commands.forEach(CommandWrapper::register);
    }
}
