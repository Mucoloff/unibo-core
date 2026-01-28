package dev.sweety.unibo.feature.discord;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.function.BiConsumer;

public class ChatEventListener implements Listener {

    private final BiConsumer<String, String> sendMessage;

    public ChatEventListener(final BiConsumer<String, String> sendMessage) {
        this.sendMessage = sendMessage;
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        //todo move
        if (!(e.message() instanceof TextComponent messageComponent) || !(e.getPlayer().name() instanceof TextComponent nameComponent))
            return;

        final String message = messageComponent.content();
        final String name = nameComponent.content();

        System.out.println("Chat message from " + name + ": " + message);

        this.sendMessage.accept(name, message);
    }

}
