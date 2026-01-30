package dev.sweety.unibo.feature.teleport.tpa;

import dev.sweety.core.math.function.TriFunction;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.file.language.Language;
import dev.sweety.unibo.player.PlayerManager;
import dev.sweety.unibo.player.features.teleport.TpaProcessor;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

@UtilityClass
public class TpaCommands {

    public void register(VanillaCore plugin) {

        final PlayerManager playerManager = plugin.playerManager();
        final String permission = "unibo.default.tpa";

        TriConsumer<Player, TpaType, String[]> action = (player, type, args) -> {
            String name = type.name().toLowerCase();
            if (args.length != 1) {
                player.sendMessage(Language.TELEPORT_TPA_USAGE.component("%command%", name));
                return;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null || target == player) {
                player.sendMessage(Language.TELEPORT_TPA_INVALID__PLAYER.component());
                return;
            }

            TpaResult result = playerManager.profile(player)
                    .tpaProcessor()
                    .send(target.getUniqueId(), type);

            switch (result) {
                case SUCCESS -> {
                    player.sendMessage(Language.TELEPORT_TPA_SENT.component("%player%", target.getName()));

                    switch (type){
                        case TPA -> target.sendMessage(Language.TELEPORT_TPA_RECEIVED_TPA.component("%player%", player.getName()));
                        case TPAHERE -> target.sendMessage(Language.TELEPORT_TPA_RECEIVED_TPAHERE.component("%player%", player.getName()));
                    }

                    target.sendMessage(
                            Language.TELEPORT_TPA_RECEIVED_INFO__ACCEPT.component("%player%", player.getName())
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to accept").color(NamedTextColor.GREEN)))
                                    .clickEvent(ClickEvent.runCommand("/tpaccept %player%".replace("%player%", player.getName())))
                    );
                    target.sendMessage(
                            Language.TELEPORT_TPA_RECEIVED_INFO__DENY.component("%player%", player.getName())
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to deny").color(NamedTextColor.RED)))
                                    .clickEvent(ClickEvent.runCommand("/tpdeny %player%".replace("%player%", player.getName())))
                    );

                }
                case ALREADY_IN_TELEPORT ->
                        player.sendMessage(Language.TELEPORT_TPA_ERROR_ALREADY__IN__TELEPORT.component());
                case ALREADY_REQUESTED ->
                        player.sendMessage(Language.TELEPORT_TPA_ERROR_ALREADY__REQUESTED.component());
                case TARGET_NOT_FOUND ->
                        player.sendMessage(Language.TELEPORT_TPA_ERROR_TARGET__NOT__FOUND.component());
                default -> player.sendMessage(Language.TELEPORT_TPA_ERROR_UNKNOWN.component("%result%", result.name()));
            }
        };

        for (TpaType type : TpaType.values()) {
            CommandWrapper.action(plugin, type.name().toLowerCase(), (player, args) -> action.accept(player, type, args))
                    .suggestion((sender, args, suggestions) -> {
                        if (args.length > 1) return;
                        if (!(sender instanceof Player player)) return;
                        Set<UUID> out = playerManager.profile(player).tpaProcessor().outgoing();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.equals(sender)) continue;
                            if (out.contains(p.getUniqueId())) continue;
                            suggestions.add(p.getName());
                        }
                    })
                    .permission(permission)
                    .register();
        }


        CommandWrapper.action(plugin, "tpacancel", (player, args) -> {
            TpaResult result = playerManager.profile(player)
                    .tpaProcessor()
                    .cancelOutgoingRequests(CancelReasons.CANCEL);

            switch (result) {
                case CANCELLED -> player.sendMessage(Language.TELEPORT_TPA_CANCEL_SUCCESS.component());
                case NOTHING_TO_CANCEL -> player.sendMessage(Language.TELEPORT_TPA_CANCEL_NOTHING.component());
                default -> {
                }
            }
        }).permission(permission).register();

        final CommandWrapper.Suggestion suggestion = (sender, args, suggestions) -> {
            if (args.length > 1) return;
            if (!(sender instanceof Player p)) return;

            TpaProcessor tpa = playerManager.profile(p).tpaProcessor();
            suggestions.addAll(
                    tpa.incomingNames()
            );
        };


        TriFunction<UUID, Player, TpaProcessor, String[]> handleRequest = (player, tpa, args) -> {
            final UUID requester;
            if (args.length == 0) {
                requester = tpa.getSingleIncomingOrNull();
                if (requester == null) {
                    player.sendMessage(Language.TELEPORT_TPA_NO__REQUEST.component());
                    return null;
                }
            } else {
                Player p = Bukkit.getPlayerExact(args[0]);
                if (p == null) {
                    player.sendMessage(Language.TELEPORT_TPA_INVALID__PLAYER.component());
                    return null;
                }
                requester = p.getUniqueId();
            }

            return requester;
        };

        CommandWrapper.action(plugin, "tpaccept", (player, args) -> {
            TpaProcessor tpa = playerManager.profile(player).tpaProcessor();

            UUID requesterId = handleRequest.apply(player, tpa, args);
            if (requesterId == null) return;

            TpaResult result = tpa.accept(requesterId);
            Player requester = Bukkit.getPlayer(requesterId);

            switch (result) {
                case SUCCESS -> {
                    player.sendMessage(Language.TELEPORT_TPA_ACCEPT_SUCCESS.component());
                    if (requester != null) requester.sendMessage(Language.TELEPORT_TPA_ACCEPT_ACCEPTED.component());
                }
                case NO_REQUEST -> player.sendMessage(Language.TELEPORT_TPA_NO__REQUEST.component());
                case ALREADY_IN_TELEPORT -> player.sendMessage(Language.TELEPORT_TPA_ERROR_ALREADY__IN__TELEPORT.component());
                case TARGET_NOT_FOUND -> player.sendMessage(Language.TELEPORT_TPA_ACCEPT_TARGET__OFFLINE.component());
                default -> player.sendMessage(Language.TELEPORT_TPA_ERROR_UNKNOWN.component("%result%", result.name()));
            }
        }).alias("tpyes").suggestion(suggestion).permission(permission).register();

        CommandWrapper.action(plugin, "tpdeny", (player, args) -> {
            TpaProcessor tpa = playerManager.profile(player).tpaProcessor();

            if (args.length == 0) {
                TpaResult result = tpa.denyAll();
                if (result == TpaResult.SUCCESS) {
                    player.sendMessage(Language.TELEPORT_TPA_DENY_ALL__SUCCESS.component());
                } else {
                    player.sendMessage(Language.TELEPORT_TPA_DENY_NOTHING.component());
                }
                return;
            }

            UUID requesterId = handleRequest.apply(player, tpa, args);
            if (requesterId == null) return;

            TpaResult result = tpa.deny(requesterId);
            switch (result) {
                case SUCCESS -> player.sendMessage(Language.TELEPORT_TPA_DENY_SUCCESS.component());
                case NO_REQUEST -> player.sendMessage(Language.TELEPORT_TPA_NO__REQUEST.component());
                default -> player.sendMessage(Language.TELEPORT_TPA_ERROR_UNKNOWN.component("%result%", result.name()));
            }
        }).alias("tpno").suggestion(suggestion).permission(permission).register();


    }

}
