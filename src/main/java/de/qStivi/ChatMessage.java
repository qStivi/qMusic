package de.qStivi;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import javax.annotation.Nullable;

public class ChatMessage {

    private static volatile ChatMessage instance;
    private final GenericCommandInteractionEvent event;
    private final Message message;

    private ChatMessage(GenericCommandInteractionEvent event) {
        this.event = event;
        this.message = event.getHook().retrieveOriginal().complete();
    }

    public static ChatMessage getInstance(GenericCommandInteractionEvent interactionHook) {
        if (instance == null) {
            synchronized (ChatMessage.class) {
                if (instance == null) {
                    instance = new ChatMessage(interactionHook);
                }
            }
        }
        return instance;
    }

    @Nullable
    public static ChatMessage getInstance() {
        return instance;
    }

    public static ChatMessage getInstance(GenericCommandInteractionEvent hook, boolean ephemeral) {
        ChatMessage chatMessage = getInstance(hook);
        chatMessage.event.getHook().setEphemeral(ephemeral);
        return chatMessage;
    }

    public void edit(String message) {
        if (instance == null) {
            throw new IllegalStateException("Instance is null.");
        }
        try {
            this.message.editMessage(message).complete();
        } catch (Exception e) {
            throw new RuntimeException("Failed to edit message", e);
        }
    }

    public void delete() {
        if (instance == null) {
            throw new IllegalStateException("Instance is null.");
        }
        try {
            message.delete().complete();
            instance = null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete message", e);
        }
    }
}
