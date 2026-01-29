package dev.sweety.unibo.api.papi;

import dev.sweety.unibo.feature.info.Stats;
import dev.sweety.unibo.player.PlayerManager;
import dev.sweety.unibo.player.VanillaPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatsExpansion extends PlaceholderExpansion {

    private final PlayerManager playerManager;

    public StatsExpansion(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "unibo";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SweetyDreams_";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(final Player player, final @NotNull String params) {
        if (player == null) return null;
        final VanillaPlayer profile = this.playerManager.profile(player.getUniqueId());
        final Stats stats = profile.stats();

        return switch (params.toLowerCase()) {
            case "elo" -> String.format("%.2f", stats.getElo());
            case "kd" -> String.format("%.2f", stats.getKd());
            case "wins" -> String.valueOf(stats.getWins());
            case "losses" -> String.valueOf(stats.getLosses());
            case "winstreak" -> String.valueOf(stats.getWinStreak());
            case "losestreak" -> String.valueOf(stats.getLoseStreak());
            case "region" -> profile.regionName();
            case "last-region" -> profile.lastRegionName();
            default -> "";
        };
    }
}