package dev.sweety.unibo.player;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.feature.info.Stats;
import dev.sweety.unibo.feature.inventory.Views;
import dev.sweety.unibo.file.Files;
import dev.sweety.unibo.file.language.Language;
import dev.sweety.unibo.player.processors.DamageProcessor;
import dev.sweety.unibo.utils.EloUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
public class MatchHandler {

    private final VanillaCore plugin;
    private final PlayerManager playerManager;

    private final Map<UUID, Views> viewInventory = new HashMap<>();

    public MatchHandler(final VanillaCore plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.playerManager();
    }

    public void handleMatchResult(final Player winner, final VanillaPlayer loserProfile) {
        final VanillaPlayer winnerProfile = this.playerManager.getProfile(winner);
        final Player loser = loserProfile.player();
        loserProfile.clear();
        if (winnerProfile == null) return;
        winnerProfile.clear();

        this.viewInventory.put(winner.getUniqueId(), new Views(winner));
        this.viewInventory.put(loser.getUniqueId(), new Views(loser));


        final Stats winnerStats = winnerProfile.stats();
        final Stats loserStats = loserProfile.stats();

        final double constant = this.plugin.config().getDouble("elo.const", 32);
        final double win_rate = this.plugin.config().getDouble("elo.win-rate", 0.05);
        final double lose_rate = this.plugin.config().getDouble("elo.lose-rate", 0.03);
        final double divisionFactor = this.plugin.config().getDouble("elo.division-factor", 400);

        winnerStats.addWin();
        loserStats.addLoss();

        double baseEloChange = EloUtils.getChange(constant, winnerStats.getElo(), loserStats.getElo(), divisionFactor);
        final int winStreak = winnerStats.getWinStreak();
        final int loseStreak = loserStats.getLoseStreak();

        final double winnerEloChange = baseEloChange * (1 + (winStreak * win_rate));
        final double loserEloChange = baseEloChange * (1 - (loseStreak * lose_rate));

        winnerStats.updateElo(winnerEloChange);
        loserStats.updateElo(-loserEloChange);

        Files.PLAYER_ELO.save(winnerProfile, loserProfile);
    }

    public void handleDeathMessage(Consumer<Component> consumer, VanillaPlayer victim, Component victimDisplay, String victimName, Player killer) {
        final DamageProcessor damageProcessor = victim.damageProcessor();
        final double health = killer.getHealth();

        final Entity attacker = damageProcessor.getAttacker();
        final Entity cause = damageProcessor.getCause();

        if (attacker == null) {
            final Language lang = switch (damageProcessor.getSourceType().getMessageId()) {
                case "fall" -> Language.DEATH__MESSAGES_FALL_DEATH;
                case "drown" -> Language.DEATH__MESSAGES_DROWNING__DEATH;
                case null, default -> Language.DEATH__MESSAGES_UNKNOWN__DEATH;
            };
            consumer.accept(lang.component("{victim}", victimName));
            return;
        }

        final Language msg = cause instanceof EnderCrystal ? Language.DEATH__MESSAGES_END__CRYSTAL__KILLED : Language.DEATH__MESSAGES_PLAYER__KILLED;
        final Language show_inv = Language.DEATH__MESSAGES_SHOW__INV;
        final Component deathMessage = msg.component("{killer_health}", String.format("%.2f", health / 2.0) + "❤");
        consumer.accept(deathMessage
                .replaceText(TextReplacementConfig.builder()
                        .matchLiteral("{victim}")
                        .replacement(victimDisplay
                                .hoverEvent(HoverEvent.showText(show_inv.component("%player%", victimName + "'s")))
                                .clickEvent(ClickEvent.runCommand("/viewinv " + victimName)))
                        .build())
                .replaceText(TextReplacementConfig.builder()
                        .matchLiteral("{killer}")
                        .replacement(killer.displayName()
                                .hoverEvent(HoverEvent.showText(show_inv.component("%player%", killer.getName() + "'s")))
                                .clickEvent(ClickEvent.runCommand("/viewinv " + killer.getName())))
                        .build())
        );
        victim.player().sendMessage(show_inv.component("%player%", "players"));
    }

}
