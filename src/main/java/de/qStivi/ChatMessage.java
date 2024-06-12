package de.qStivi;

import net.dv8tion.jda.api.interactions.InteractionHook;

import javax.annotation.Nullable;

public class ChatMessage {

    public static ChatMessage instance;
    private final InteractionHook interactionHook;

    private ChatMessage(InteractionHook interactionHook) {
        this.interactionHook = interactionHook;
    }

    public static ChatMessage getInstance(InteractionHook interactionHook) {
        if (instance == null) {
            instance = new ChatMessage(interactionHook);
        }
        return instance;
    }

    @Nullable
    public static ChatMessage getInstance() {
        return instance;
    }

    public static ChatMessage getInstance(InteractionHook hook, boolean ephemeral) {
        if (instance == null) {
            instance = new ChatMessage(hook);
        }
        instance.interactionHook.setEphemeral(ephemeral);
        return instance;
    }

    public void edit(String message) {
        if (instance == null) {
            throw new IllegalStateException("Instance is null.");
        }
        interactionHook.editOriginal(message).complete();
    }

    public void delete() {
        if (instance == null) {
            throw new IllegalStateException("Instance is null.");
        }
        interactionHook.deleteOriginal().complete();
        instance = null;
    }
}
