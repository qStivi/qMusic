package de.qStivi;

import net.dv8tion.jda.api.interactions.InteractionHook;

public class ChatMessage {

    private final InteractionHook interactionHook;
    public static ChatMessage instance;

    private ChatMessage(InteractionHook interactionHook) {
        this.interactionHook = interactionHook;
    }

    public static ChatMessage getInstance(InteractionHook interactionHook) {
        if (instance == null) {
            instance = new ChatMessage(interactionHook);
        }
        return instance;
    }

    public static ChatMessage getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Instance is null.");
        }
        return instance;
    }

    public void edit(String message) {
        interactionHook.editOriginal(message).complete();
    }
}
