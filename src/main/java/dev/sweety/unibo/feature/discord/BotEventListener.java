package dev.sweety.unibo.feature.discord;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BotEventListener extends ListenerAdapter {

    private final BiConsumer<String, String> sendMessage;
    private final Predicate<MessageChannelUnion> isChannel;
    private final Consumer<ReadyEvent> onReady;

    public BotEventListener(final BiConsumer<String, String> sendMessage, final Predicate<MessageChannelUnion> isChannel, final Consumer<ReadyEvent> onReady) {
        this.sendMessage = sendMessage;
        this.isChannel = isChannel;
        this.onReady = onReady;
    }

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        final User author = event.getAuthor();
        if (author.isBot() || author.isSystem() || event.isWebhookMessage() || !isChannel.test(event.getChannel())) return;

        final String message = event.getMessage().getContentDisplay();

        this.sendMessage.accept(author.getName(), message);
    }

    @Override
    public void onReady(@NonNull ReadyEvent event) {
        this.onReady.accept(event);
    }

}
