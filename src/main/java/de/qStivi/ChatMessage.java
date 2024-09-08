package de.qStivi;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

public class ChatMessage {

    public static ChatMessage instance;
    private final GenericCommandInteractionEvent interactionHook;
    private final Message message;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ChatMessage.class);

    private ChatMessage(GenericCommandInteractionEvent interactionHook) {
        this.interactionHook = interactionHook;
        this.message = interactionHook.getHook().retrieveOriginal().complete();
    }

    public static synchronized ChatMessage getInstance(GenericCommandInteractionEvent interactionHook) {
        if (instance == null) {
            instance = new ChatMessage(interactionHook);
        } else if (instance.interactionHook != interactionHook) {
            instance.delete();
            instance = new ChatMessage(interactionHook);
        }
        return instance;
    }

    @Nullable
    public static synchronized ChatMessage getInstance() {
        return instance;
    }

    public static synchronized ChatMessage getInstance(GenericCommandInteractionEvent interactionHook, boolean ephemeral) {
        instance = getInstance(interactionHook);
        instance.interactionHook.getHook().setEphemeral(ephemeral);
        return instance;
    }

    public synchronized void edit(String message) {
        if (instance == null) {
            throw new IllegalStateException("Instance is null.");
        }
        this.message.editMessage(message).complete();
    }

    public synchronized void delete() {
        if (instance == null) {
            throw new IllegalStateException("Instance is null.");
        }
        message.delete().complete();
        instance = null;
    }
}
