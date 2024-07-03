package de.qStivi;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

public class ChatMessage {

    private static volatile ChatMessage instance;
    private final GenericCommandInteractionEvent event;
    private final Message message;

    private ChatMessage(GenericCommandInteractionEvent event) {
        this.event = event;
        this.message = event.getHook().retrieveOriginal().complete();
    }

    public static ChatMessage getInstance(GenericCommandInteractionEvent event) {
        if (instance == null) {
            synchronized (ChatMessage.class) {
                if (instance == null) {
                    instance = new ChatMessage(event);
                }
            }
        }
        return instance;
    }

    public static ChatMessage getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Instance is not initialized.");
        }
        return instance;
    }

    public static boolean isInstanceNull() {
        return instance == null;
    }

    public void edit(String message) {
        try {
            this.message.editMessage(message).complete();
        } catch (Exception e) {
            throw new RuntimeException("Failed to edit message", e);
        }
    }

    public void delete() {
        try {
            this.message.delete().complete();
            instance = null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete message", e);
        }
    }

    public Message getMessage() {
        return this.message;
    }
}