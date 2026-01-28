package dev.sweety.unibo.feature.info.leaderboard;

import dev.sweety.unibo.api.VanillaAPI;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public enum Board {
    KILLS(10, Material.DIAMOND_SWORD, Component.text("🏹 Top Kills").color(NamedTextColor.GREEN), VanillaAPI.leaderboard()::getTopKills),
    DEATHS(11, Material.SKELETON_SKULL, Component.text("💀 Top Deaths").color(NamedTextColor.RED), VanillaAPI.leaderboard()::getTopDeaths),
    ELO(13, Material.DIAMOND, Component.text("🏆 Top ELO").color(NamedTextColor.AQUA), VanillaAPI.leaderboard()::getTopElo),
    KD(15, Material.WITHER_SKELETON_SKULL, Component.text("📈 Miglior K/D").color(NamedTextColor.YELLOW), VanillaAPI.leaderboard()::getTopKD),
    STREAK(16, Material.NETHERITE_SWORD, Component.text("🔥 Top Killstreak").color(NamedTextColor.LIGHT_PURPLE), VanillaAPI.leaderboard()::getTopStreak),
    ;

    @Getter
    private final int slot;

    private final ItemStack item;
    private final Supplier<List<Leaderboard.Entry>> fetch;

    Board(int slot, Material material, @NotNull TextComponent name, Supplier<List<Leaderboard.Entry>> fetch) {
        this.slot = slot;
        this.fetch = fetch;
        (this.item = new ItemStack(material)).editMeta(meta -> {
            //meta.setHideTooltip(true);
            meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        });
    }

    public ItemStack getItem() {
        this.item.editMeta(meta -> {
            meta.addItemFlags(ItemFlag.values());
            List<Component> lore = new ArrayList<>();

            List<Leaderboard.Entry> entries = fetch.get();
            for (int i = 0; i < entries.size(); i++) {
                Leaderboard.Entry entry = entries.get(i);
                String value = bool() ? String.format("%.2f", entry.score()) : String.valueOf(((int) entry.score()));
                TextComponent text = (Component.text("#" + (i + 1) + " ").color(NamedTextColor.GRAY).append((Component.text(entry.name() + " - " + value)).color(NamedTextColor.YELLOW))).decoration(TextDecoration.ITALIC, false);
                lore.add(text);
            }

            lore.add(Component.empty());
            lore.add(Component.text("⚠ Aggiornato ogni 60s").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
        });
        return this.item;
    }

    private boolean bool() {
        return this.equals(ELO) || this.equals(KD);
    }

}