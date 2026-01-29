package dev.sweety.unibo.feature.discord;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.VanillaCoreAccessors;
import dev.sweety.unibo.utils.McUtils;
import dev.sweety.unibo.utils.ColorUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.time.Duration;
import java.util.EnumSet;

public class DiscordBot {

    private final VanillaCore plugin;

    private TextChannel textChannel;
    private String discordFormat;
    private String chatFormat;
    private JDA jda;

    public DiscordBot(final VanillaCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        final EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT
        );
        this.jda = JDABuilder.createLight(plugin.config().getString("bot.token"), intents)
                .addEventListeners(new BotEventListener(this::sendChatMessage, this::isChannel, this::onReady))
                .build();


        this.discordFormat = plugin.config().getString("bot.discordFormat", "**%s** -> %s");
        this.chatFormat = ColorUtils.color(plugin.config().getString("bot.chatFormat", "[Discord] %s -> %s"));
    }

    public void sendDiscordMessage(String player, String message) {
        if (textChannel != null) textChannel.sendMessage(this.discordFormat.formatted(player, message)).queue();
    }

    public void onReady(ReadyEvent event) {
        this.textChannel = event.getJDA().getTextChannelById(plugin.config().getLong("bot.channelId"));
    }

    public void sendChatMessage(String author, String message) {
        McUtils.broadcast(this.chatFormat.formatted(author, message));
    }

    public boolean isChannel(final MessageChannelUnion channel) {
        if (this.textChannel == null) return false;
        return channel.getIdLong() == this.textChannel.getIdLong();
    }

    public void shutdown() {
        if (this.jda == null) return;
        this.jda.shutdown();
        try {
            if (!this.jda.awaitShutdown(Duration.ofSeconds(5))) {
                this.jda.shutdownNow();
                this.jda.awaitShutdown();
            }
        } catch (Exception e){
            VanillaCoreAccessors.logger().warn("Error while shutting down Discord JDA", e);
        }
    }

}