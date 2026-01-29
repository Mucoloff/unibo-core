package dev.sweety.unibo.feature.teleport.tpa;

import dev.sweety.core.math.function.TriFunction;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.player.PlayerManager;
import dev.sweety.unibo.player.features.teleport.TpaProcessor;
import dev.sweety.unibo.player.features.teleport.TpaResult;
import dev.sweety.unibo.player.features.teleport.TpaType;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

@UtilityClass
public class TPA {

    public void register(VanillaCore plugin) {

        final PlayerManager playerManager = plugin.playerManager();

        TriConsumer<Player, TpaType, String[]> action = (player, type, args) -> {
            String name = type.name().toLowerCase();
            if (args.length != 1) {
                player.sendRichMessage("<red>Utilizzo corretto: /" + name + " <giocatore>");
                return;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null || target == player) {
                player.sendRichMessage("<red>Player non valido.");
                return;
            }

            TpaResult result = playerManager.profile(player)
                    .tpaProcessor()
                    .send(target.getUniqueId(), type);

            switch (result) {
                case SUCCESS -> {
                    player.sendRichMessage("<gray>Richiesta inviata a <yellow>" + target.getName() + "<gray>.");

                    switch (type){
                        case TPA -> target.sendRichMessage("<yellow>" + player.getName() + " <gray>ti ha inviato una richiesta di teleport verso di te.");
                        case TPAHERE -> target.sendRichMessage("<yellow>" + player.getName() + " <gray>ti ha inviato una richiesta di teleport verso di lui.");
                    }

                    target.sendRichMessage("<gray>Digita <yellow>/tpaccept " + player.getName() + " <gray>per accettare o <yellow>/tpdeny " + player.getName() + " <gray>per rifiutare.");

                }
                case ALREADY_IN_TELEPORT ->
                        player.sendRichMessage("<red>Sei già in teleport o il giocatore è occupato.");
                case ALREADY_REQUESTED ->
                        player.sendRichMessage("<red>Hai già inviato una richiesta a questo giocatore.");
                case TARGET_NOT_FOUND ->
                        player.sendRichMessage("<red>Giocatore non trovato (potrebbe essere offline).");
                default -> player.sendRichMessage("<red>Errore nell'invio della richiesta: " + result.name());
            }
        };

        for (TpaType type : TpaType.values()) {
            CommandWrapper.action(plugin, type.name().toLowerCase(), (player, args) -> action.accept(player, type, args))
                    .suggestion((sender, args, suggestions) -> {
                        if (args.length != 0) return;
                        if (!(sender instanceof Player player)) return;
                        Set<UUID> out = playerManager.profile(player).tpaProcessor().outgoing();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.equals(sender)) continue;
                            if (out.contains(p.getUniqueId())) continue;
                            suggestions.add(p.getName());
                        }
                    })
                    .register();
        }


        CommandWrapper.action(plugin, "tpacancel", (player, args) -> {
            TpaResult result = playerManager.profile(player)
                    .tpaProcessor()
                    .cancelOutgoingRequests("cancel");

            switch (result) {
                case CANCELLED -> player.sendRichMessage("<gray>Richieste annullate.");
                case NOTHING_TO_CANCEL -> player.sendRichMessage("<red>Nessuna richiesta da annullare.");
                default -> {
                }
            }
        }).register();

        final CommandWrapper.Suggestion suggestion = (sender, args, suggestions) -> {
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
                    player.sendRichMessage("<red>Nessuna richiesta in arrivo.");
                    return null;
                }
            } else {
                Player p = Bukkit.getPlayerExact(args[0]);
                if (p == null) {
                    player.sendRichMessage("<red>Player non valido.");
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
                    player.sendRichMessage("<gray>Hai accettato la richiesta.");
                    if (requester != null) requester.sendRichMessage("<gray>La tua richiesta è stata accettata.");
                }
                case NO_REQUEST -> player.sendRichMessage("<red>Nessuna richiesta trovata.");
                case ALREADY_IN_TELEPORT -> player.sendRichMessage("<red>Un teleport è già in corso.");
                case TARGET_NOT_FOUND -> player.sendRichMessage("<red>Il richiedente non è più online.");
                default -> player.sendRichMessage("<red>Errore: " + result.name());
            }
        }).alias("tpyes").suggestion(suggestion).register();

        CommandWrapper.action(plugin, "tpdeny", (player, args) -> {
            TpaProcessor tpa = playerManager.profile(player).tpaProcessor();

            if (args.length == 0) {
                TpaResult result = tpa.denyAll();
                if (result == TpaResult.SUCCESS) {
                    player.sendRichMessage("<gray>Tutte le richieste sono state rifiutate.");
                } else {
                    player.sendRichMessage("<red>Nessuna richiesta da rifiutare.");
                }
                return;
            }

            UUID requesterId = handleRequest.apply(player, tpa, args);
            if (requesterId == null) return;

            TpaResult result = tpa.deny(requesterId);
            switch (result) {
                case SUCCESS -> player.sendRichMessage("<gray>Hai rifiutato la richiesta.");
                case NO_REQUEST -> player.sendRichMessage("<red>Nessuna richiesta trovata.");
                default -> player.sendRichMessage("<red>Errore: " + result.name());
            }
        }).alias("tpno").suggestion(suggestion).register();


    }

}
