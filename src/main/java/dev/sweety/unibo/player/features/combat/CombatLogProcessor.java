package dev.sweety.unibo.player.features.combat;

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
import dev.sweety.unibo.player.processors.AttackProcessor;
import dev.sweety.unibo.player.processors.DamageProcessor;
import dev.sweety.unibo.utils.McUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class CombatLogProcessor extends Processor {

    private final AttackProcessor attackProcessor;

    private final int maxCooldown;
    private final AtomicLong cooldown = new AtomicLong(-1);
    private final Runnable runnable;

    private final AtomicReference<CombatStatus> combatStatus = new AtomicReference<>(CombatStatus.IDLE);

    private final ProfileThread profileThread;
    private final PlayerManager playerManager;
    private final DamageProcessor damageProcessor;

    private CompletableFuture<?> task = null;

    public void setEnabled(boolean enabled) {
        this.combatStatus.set(enabled ? CombatStatus.IDLE : CombatStatus.DISABLED);
    }

    public CombatLogProcessor(final VanillaPlayer player, final VanillaCore plugin) {
        super(player, plugin);
        this.playerManager = plugin.playerManager();
        this.attackProcessor = player.attackProcessor();
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

        setEnabled(player.stats().isCombat());
    }

    @Override
    public void handle(final Packet packet) {
        if (packet.isCancelled()) return;
        final Player lastPlayerHit = this.attackProcessor.getLastPlayerHit();
        final Player self = this.player.player();

        final Entity damager = this.damageProcessor.getDamager();
        switch (packet.getWrapper()) {
            case WrapperPlayClientInteractEntity wrap -> {
                if (this.attackProcessor.isAttack()) {
                    if (isDisabled()) {
                        self.sendRichMessage("<red>You have disabled PvP!");
                        packet.cancel();
                        return;
                    }

                    final VanillaPlayer profile = this.playerManager.profile(lastPlayerHit);
                    if (profile == null) return;
                    if (profile.combatLogProcessor().isDisabled()) {
                        self.sendRichMessage("<red>" + lastPlayerHit.getName() + " has disabled PvP!");
                        packet.cancel();
                        return;
                    }
                    this.tag(lastPlayerHit);
                    profile.tag(this.player.player());
                }
            }

            case WrapperPlayServerDamageEvent wrap -> {
                if (!this.damageProcessor.isPlayer()) return;
                if (!(damager instanceof Player attacker)) return;
                if (isDisabled()) {
                    attacker.sendRichMessage("<red>" + self.getName() + " has disabled PvP!");
                    packet.cancel();
                    return;
                }
                this.tag(attacker);
                final VanillaPlayer profile = this.playerManager.profile(attacker);
                if (profile == null) return;
                profile.tag(this.player.player());
            }

            case WrapperPlayClientChatCommandUnsigned wrap -> {
                String command = wrap.getCommand();

                if (notCombat()) return;

                boolean blacklist = plugin.config().getBoolean("combat.blacklist", true);
                boolean inList = plugin.config().getStringList("combat.commands").contains(command);

                if (inList && !blacklist) return;

                if (self.hasPermission("unibo.staff.combatlog.bypass")) {
                    self.sendRichMessage("<red>combat bypass: \"" + command + "\"");
                    return;
                }

                self.sendRichMessage("<red>Yᴏᴜ ᴄᴀɴɴᴏᴛ ᴇxᴇᴄᴜᴛᴇ ᴄᴏᴍᴍᴀɴᴅs ᴡʜɪʟᴇ ɪɴ ᴄᴏᴍʙᴀᴛ!");
                packet.cancel();
            }

            case WrapperPlayServerRespawn wrap -> {
                this.clear();
            }

            case WrapperPlayServerDeathCombatEvent wrap -> {
                this.clear();
                if (lastPlayerHit != null) {
                    VanillaPlayer profile = this.playerManager.profile(lastPlayerHit);
                    if (profile == null) return;
                    profile.removeCombat();
                }
                if (damager instanceof Player attacker) {
                    VanillaPlayer profile = this.playerManager.profile(attacker);
                    if (profile == null) return;
                    profile.removeCombat();
                }
            }

            //case WrapperPlayServerDisconnect wrap -> quit();

            case null, default -> {
            }
        }
    }

    public void quit() {
        if (notCombat()) return;

        final Player p = this.player.player();
        Player lastPlayerHit = this.attackProcessor.getLastPlayerHit();
        if (lastPlayerHit == null) {
            final DamageProcessor damageProcessor = this.damageProcessor;
            if (damageProcessor.getDamager() instanceof Player attacker) lastPlayerHit = attacker;
            if (damageProcessor.getCause() instanceof Player cause) lastPlayerHit = cause;
        }

        final TextComponent reason = McUtils.component(Language.COMBAT_LOG__OUT.get("%player%", this.player.name()));
        Bukkit.broadcast(reason);
        this.clear();

        if (lastPlayerHit == null) {
            p.damage(p.getHealth(), p);
            return;
        }

        final VanillaPlayer killerProfile = this.playerManager.profile(lastPlayerHit);
        if (killerProfile != null) killerProfile.removeCombat();

        this.player.retain();

        final Player killer = lastPlayerHit;
        Bukkit.getScheduler().runTask(plugin.instance(), () -> {
            DamageSource source = DamageSource.
                    builder(DamageType.PLAYER_ATTACK)
                    .withDirectEntity(killer)
                    .withCausingEntity(killer)
                    .build();
            p.damage(p.getHealth(), source);
            this.player.release();
        });
    }

    public void tag(final VanillaPlayer victim) {
        this.tag(victim.player());
        victim.tag(this.player.player());
    }

    public void tag(final Player victim) {
        final Player p = this.player.player();

        if (notCombat()) {
            final Component start = Language.COMBAT_START.component("%enemy%", victim.getName());
            p.sendMessage(start);
            p.sendActionBar(start);
        }

        this.player.combatStatus(CombatStatus.ENGAGED);

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

        this.player.combatStatus(CombatStatus.IDLE);

        this.cooldown.set(-1);
        this.cancel();
    }

    public void cancel() {
        if (task != null && !task.isCancelled()) this.task.cancel(true);
        this.task = null;
    }

    public boolean isDisabled() {
        return this.combatStatus.get().equals(CombatStatus.DISABLED);
    }

    public boolean notCombat() {
        return this.combatStatus.get().equals(CombatStatus.IDLE);
    }

    public boolean inCombat() {
        return this.combatStatus.get().equals(CombatStatus.ENGAGED);
    }

}