package de.qStivi;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nullable;

public class ChatMessage {

    public static ChatMessage instance;
    private final GenericCommandInteractionEvent event;
    private final Message message;

    private ChatMessage(GenericCommandInteractionEvent event) {
        this.event = event;
        this.message = event.getHook().retrieveOriginal().complete();
    }

    public static ChatMessage getInstance(GenericCommandInteractionEvent interactionHook) {
        if (instance == null) {
            instance = new ChatMessage(interactionHook);
        }
        return instance;
    }

    @Nullable
    public static ChatMessage getInstance() {
        return instance;
    }

    public static ChatMessage getInstance(GenericCommandInteractionEvent hook, boolean ephemeral) {
        if (instance == null) {
            instance = new ChatMessage(hook);
        }
        instance.event.getHook().setEphemeral(ephemeral);
        return instance;
    }

    public void edit(String message) {
        if (instance == null) {
            throw new IllegalStateException("Instance is null.");
        }
        this.message.editMessage(message).complete();
    }

    public void delete() {
        if (instance == null) {
            throw new IllegalStateException("Instance is null.");
        }
        message.delete().complete();
        instance = null;
    }
}
