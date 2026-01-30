package dev.sweety.unibo.feature.info.leaderboard;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.menu.SimpleMenu;
import dev.sweety.unibo.feature.info.Stats;
import dev.sweety.unibo.file.Files;
import dev.sweety.unibo.utils.McUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class Leaderboard {

    private final VanillaCore plugin;

    private final AtomicReference<Snapshot> snapshot =
            new AtomicReference<>(Snapshot.empty());

    public Leaderboard(VanillaCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin.instance(), this::updateLeaderboards, 0L, 20 * 60L);
    }

    private void updateLeaderboards() {
        Map<String, Stats> statsMap = new HashMap<>();

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            String name = player.getName();
            if (name == null) continue;

            Stats stats = Files.PLAYER_STATS.load(player.getUniqueId());
            statsMap.put(name, stats);
        }

        List<Entry> kills = new ArrayList<>();
        List<Entry> deaths = new ArrayList<>();
        List<Entry> kd = new ArrayList<>();
        List<Entry> streak = new ArrayList<>();
        List<Entry> elo = new ArrayList<>();

        statsMap.forEach((name, stats) -> {
            kills.add(new Entry(name, stats.getWins()));
            deaths.add(new Entry(name, stats.getLosses()));
            kd.add(new Entry(name, stats.getKd()));
            streak.add(new Entry(name, stats.getWinStreak()));
            elo.add(new Entry(name, stats.getElo()));
        });

        kills.sort(null);
        deaths.sort(null);
        kd.sort(null);
        streak.sort(null);
        elo.sort(null);

        snapshot.set(new Snapshot(
                List.copyOf(kills),
                List.copyOf(deaths),
                List.copyOf(kd),
                List.copyOf(streak),
                List.copyOf(elo)
        ));
    }


    public List<Entry> getTopKills() {
        return snapshot.get().kills().subList(0, Math.min(5, snapshot.get().kills().size()));
    }

    public List<Entry> getTopDeaths() {
        return snapshot.get().deaths().subList(0, Math.min(5, snapshot.get().deaths().size()));
    }

    public List<Entry> getTopKD() {
        return snapshot.get().kd().subList(0, Math.min(5, snapshot.get().kd().size()));
    }

    public List<Entry> getTopStreak() {
        return snapshot.get().streak().subList(0, Math.min(5, snapshot.get().streak().size()));
    }

    public List<Entry> getTopElo() {
        return snapshot.get().elo().subList(0, Math.min(5, snapshot.get().elo().size()));
    }

    private record Snapshot(
            List<Entry> kills,
            List<Entry> deaths,
            List<Entry> kd,
            List<Entry> streak,
            List<Entry> elo
    ) {
        static Snapshot empty() {
            return new Snapshot(
                    List.of(), List.of(), List.of(), List.of(), List.of()
            );
        }
    }

    public record Entry(String name, double score) implements Comparable<Entry> {
        @Override
        public int compareTo(Entry other) {
            return Double.compare(other.score(), this.score());
        }
    }

    public static class Menu extends SimpleMenu {

        public static final Menu INSTANCE = new Menu();

        private static final ItemStack BACKGROUND = McUtils.createGlass(Material.BLACK_STAINED_GLASS_PANE);

        public Menu() {
            super(Rows.THREE, Component.text("Leaderboard").color(NamedTextColor.LIGHT_PURPLE));
        }

        @Override
        public void setup() {
            for (int i = 0; i < 27; i++) setItem(i, BACKGROUND);
            for (Board value : Board.values()) setItem(value.getSlot(), value.getItem());


        }

    }
}