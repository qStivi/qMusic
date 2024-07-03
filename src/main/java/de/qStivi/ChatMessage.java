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
        if (instance == null) {
            throw new IllegalStateException("Instance is not initialized.");
        }
        return instance;
    }

    public static ChatMessage getInstance(GenericCommandInteractionEvent hook, boolean ephemeral) {
        ChatMessage chatMessage = getInstance(hook);
        chatMessage.event.getHook().setEphemeral(ephemeral);
        return chatMessage;
    }

    public void edit(String message) {
        if (this.message == null) {
            throw new IllegalStateException("Instance is null.");
        }
        try {
            var editAction = this.message.editMessage(message);
            if (editAction != null) {
                editAction.complete();
            } else {
                throw new RuntimeException("Edit action returned null");
            }
        } catch (NullPointerException e) {
            throw new RuntimeException("Failed to edit message", e);
        }
    }

    public void delete() {
        if (this.message == null) {
            throw new IllegalStateException("Instance is null.");
        }
        try {
            var deleteAction = message.delete();
            if (deleteAction != null) {
                deleteAction.complete();
            } else {
                throw new RuntimeException("Delete action returned null");
            }
            instance = null;
        } catch (NullPointerException e) {
            throw new RuntimeException("Failed to delete message", e);
        }
    }

    /**
     * This method is used to get if the instance is null.
     */
    public static boolean isInstanceNull() {
        return instance == null;
    }
}