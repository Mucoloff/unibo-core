package dev.sweety.unibo.player.features;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import dev.sweety.core.color.AnsiColor;
import dev.sweety.core.util.ObjectUtils;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.VanillaAPI;
import dev.sweety.unibo.api.packet.Packet;
import dev.sweety.unibo.api.processor.Processor;
import dev.sweety.unibo.player.VanillaPlayer;
import dev.sweety.unibo.utils.ColorUtils;
import dev.sweety.unibo.utils.McUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public class ChatProcessor extends Processor {

    private final FileConfiguration config;
    private final BiConsumer<String, String> forwardtoDiscord;

    public ChatProcessor(VanillaPlayer player, VanillaCore plugin) {
        super(player, plugin);
        this.config = plugin.config();
        this.forwardtoDiscord = plugin.discordBot()::sendDiscordMessage;
    }

    private final WrapperPlayServerSystemChatMessage chatWrap = new WrapperPlayServerSystemChatMessage(false, Component.newline());

    @Override
    public void handle(final Packet packet) {
        if (packet.isCancelled()) return;
        if (!(packet.getWrapper() instanceof WrapperPlayClientChatMessage wrap)) return;

        packet.cancel();

        final Player source = player.player();
        final String message = wrap.getMessage();

        String text = source.hasPermission("unibo.chat.colors") ? ColorUtils.color(message) : message;

        ItemStack item = source.getInventory().getItemInMainHand();
        boolean hasItem = !item.getType().isAir();

        this.forwardtoDiscord.accept(
                AnsiColor.BLUE_BOLD.getColor() +
                player.name() + AnsiColor.RESET.getColor(),
                AnsiColor.WHITE_BRIGHT.getColor() +
                message.replace("[i]", hasItem ? LegacyComponentSerializer.legacySection().serialize(item.displayName().style(Style.style())) : "")
                        + AnsiColor.RESET.getColor()
        );

        final CachedMetaData metaData = VanillaAPI.luckperms().getPlayerAdapter(Player.class).getMetaData(source);
        @NotNull final String group = ObjectUtils.nullOption(metaData.getPrimaryGroup(), "default");

        @NotNull String format = ObjectUtils.nullOption(config.getString("chat.groups." + group + ".format"), config.getString("chat.fallback.format"));
        @NotNull String firstColor = ObjectUtils.nullOption(config.getString("chat.groups." + group + ".first-color"), config.getString("chat.fallback.first-color"));
        @NotNull String secondColor = ObjectUtils.nullOption(config.getString("chat.groups." + group + ".second-color"), config.getString("chat.fallback.second-color"));

        format = applyPlaceholders(source, format, metaData, firstColor, secondColor);

        final TextComponent formatComponent = McUtils.component(format);

        boolean hover = config.getBoolean("chat.hover.enabled", true);

        final Component displayedName;
        if (hover) {
            String hoverText = ColorUtils.color(applyPlaceholders(source, config.getString("chat.hover.format", "%lang%"), metaData, firstColor, secondColor));
            HoverEvent<Component> hoverEvent = HoverEvent.showText(McUtils.component(hoverText));
            displayedName = source.displayName().hoverEvent(hoverEvent);
        } else displayedName = source.displayName();

        final Component component = formatComponent
                .replaceText(TextReplacementConfig.builder().matchLiteral("%player%").replacement(displayedName).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%name%").replacement(displayedName).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%displayname%").replacement(displayedName).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%message%").replacement(text).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("[i]").replacement(hasItem ? item.displayName().hoverEvent(item.asHoverEvent()) : Component.empty()).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%").replacement("%%").build());

        chatWrap.setMessage(component);

        plugin.playerManager().writePacket(chatWrap);
    }

    private @NotNull String applyPlaceholders(final Player source, final String replace, final CachedMetaData metaData, final String firstColor, final String secondColor) {
        String r = replace
                .replace("%prefix%", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("%suffix%", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("%prefixes%", metaData.getPrefixes().keySet().stream().map(key -> metaData.getPrefixes().get(key)).collect(Collectors.joining()))
                .replace("%suffixes%", metaData.getSuffixes().keySet().stream().map(key -> metaData.getSuffixes().get(key)).collect(Collectors.joining()))
                .replace("%world%", source.getWorld().getName())
                .replace("%first-color%", firstColor)
                .replace("%second-color%", secondColor)
                .replace("%ping%", String.valueOf(source.getPing()))
                .replace("%username-color%", ObjectUtils.nullOption(metaData.getMetaValue("username-color"), ""))
                .replace("%message-color%", ObjectUtils.nullOption(metaData.getMetaValue("message-color"), ""));
        return ColorUtils.color(PlaceholderAPI.setPlaceholders(source, r));
    }
}