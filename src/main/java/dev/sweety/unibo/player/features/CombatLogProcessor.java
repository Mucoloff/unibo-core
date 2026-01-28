package dev.sweety.unibo.player.features;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommandUnsigned;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDamageEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeathCombatEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import dev.sweety.core.thread.ProfileThread;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.packet.Packet;
import dev.sweety.unibo.api.processor.Processor;
import dev.sweety.unibo.file.language.Language;
import dev.sweety.unibo.player.PlayerManager;

import dev.sweety.unibo.player.VanillaPlayer;
import dev.sweety.unibo.player.processors.CombatProcessor;
import dev.sweety.unibo.player.processors.DamageProcessor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class CombatLogProcessor extends Processor {

    private final CombatProcessor combatProcessor;

    private final int maxCooldown;
    private final AtomicLong cooldown = new AtomicLong(-1);
    private final AtomicBoolean combat = new AtomicBoolean(false);
    private final Runnable runnable;

    private final ProfileThread profileThread;
    private final PlayerManager playerManager;
    private final DamageProcessor damageProcessor;

    private CompletableFuture<?> task = null;

    public CombatLogProcessor(final VanillaPlayer player, final VanillaCore plugin) {
        super(player, plugin);
        this.playerManager = plugin.playerManager();
        this.combatProcessor = player.combatProcessor();
        this.damageProcessor = player.damageProcessor();
        this.profileThread = player.profileThread();
        this.maxCooldown = plugin.config().getInt("combat.cooldown", 15) * 1000;
        this.runnable = () -> {
            if (notCombat()) {
                this.cancel();
                return;
            }

            final Player p = this.player.player();
            final long remaining = this.maxCooldown - (System.currentTimeMillis() - this.cooldown.get());
            if (remaining <= 0) {
                this.clear();
                return;
            }

            p.sendActionBar(Language.COMBAT_TIMER.component("%time%", String.format("%.1f", remaining / 1000d)));

        };
    }

    @Override
    public void handle(final Packet packet) {
        if (packet.isCancelled()) return;
        final Player lastPlayerHit = this.combatProcessor.getLastPlayerHit();
        final Player p = this.player.player();

        final Entity eAttacker = this.damageProcessor.getAttacker();
        switch (packet.getWrapper()) {
            case WrapperPlayClientInteractEntity wrap -> {
                //if (combatProcessor.isAttack()) this.biTag(lastPlayerHit);
            }

            case WrapperPlayServerDamageEvent wrap -> {
                if (!this.damageProcessor.isPlayer()) return;
                if (!(eAttacker instanceof Player attacker)) return;
                this.biTag(attacker);
            }

            case WrapperPlayClientChatCommandUnsigned wrap -> {
                String command = wrap.getCommand();

                if (notCombat()) return;

                boolean blacklist = plugin.config().getBoolean("combat.blacklist", true);
                boolean inList = plugin.config().getStringList("combat.commands").contains(command);

                if (inList && !blacklist) return;

                if (p.hasPermission("unibo.staff.combatlog.bypass")) {
                    p.sendRichMessage("<red>combat bypass: \"" + command + "\"");
                    return;
                }

                p.sendRichMessage("<red>Yᴏᴜ ᴄᴀɴɴᴏᴛ ᴇxᴇᴄᴜᴛᴇ ᴄᴏᴍᴍᴀɴᴅs ᴡʜɪʟᴇ ɪɴ ᴄᴏᴍʙᴀᴛ!");
                packet.cancel();
            }

            case WrapperPlayServerRespawn wrap -> {
                this.clear();
            }

            case WrapperPlayServerDeathCombatEvent wrap -> {
                this.clear();
                if (lastPlayerHit != null) {
                    VanillaPlayer profile = this.playerManager.getProfile(lastPlayerHit);
                    if (profile == null) return;
                    profile.clear();
                }
                if (eAttacker instanceof Player attacker){
                    VanillaPlayer profile = this.playerManager.getProfile(attacker);
                    if (profile == null) return;
                    profile.clear();
                }
            }

            //case WrapperPlayServerDisconnect wrap -> quit();

            case null, default -> {
            }
        }
    }

    public void quit() {
        final Player p = this.player.player();
        Player lastPlayerHit = this.combatProcessor.getLastPlayerHit();
        if (lastPlayerHit == null) {
            final DamageProcessor damageProcessor = this.damageProcessor;
            if (damageProcessor.getAttacker() instanceof Player attacker) lastPlayerHit = attacker;
            if (damageProcessor.getCause() instanceof Player cause) lastPlayerHit = cause;
        }
        if (notCombat()) return;
        final TextComponent reason = Component.text(Language.COMBAT_LOG__OUT.get("%player%", this.player.name()));
        Bukkit.broadcast(reason);
        this.clear();

        if (lastPlayerHit == null) {
            p.damage(p.getHealth(), p);
            return;
        }

        final VanillaPlayer victim = this.playerManager.getProfile(lastPlayerHit);
        if (victim.combatProcessor().getLastPlayerHit().equals(this.getPlayer().player())) {
            victim.clear();
            p.damage(p.getHealth(), lastPlayerHit);
        }
    }

    public void tag(final VanillaPlayer victim) {
        this.tag(victim.player());
        victim.tag(this.player.player());
    }

    private void biTag(final Player player) {
        this.tag(player);
        final VanillaPlayer profile = this.playerManager.getProfile(player);
        if (profile == null) return;
        profile.tag(this.player.player());
    }

    public void tag(final Player victim) {
        final Player p = this.player.player();

        if (notCombat()) {
            final Component start = Language.COMBAT_START.component("%enemy%", victim.getName());
            p.sendMessage(start);
            p.sendActionBar(start);
        }

        this.combat.set(true);
        final long now = System.currentTimeMillis();
        this.cooldown.set(now);

        this.cancel();
        this.task = this.profileThread.scheduleAtFixedRate(runnable, 50, 50, TimeUnit.MILLISECONDS);
    }

    public void clear() {
        if (notCombat()) return;

        final Player p = this.player.player();
        final Component end = Language.COMBAT_END.component();
        p.sendMessage(end);
        p.sendActionBar(end);

        this.combat.set(false);
        this.cooldown.set(-1);
        this.cancel();
    }

    public void cancel() {
        if (task != null && !task.isCancelled()) this.task.cancel(true);
        this.task = null;
    }

    public boolean notCombat() {
        return !this.combat.get();
    }

    public boolean inCombat() {
        return this.combat.get();
    }

}