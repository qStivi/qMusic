package de.qStivi;

import net.dv8tion.jda.api.interactions.InteractionHook;

public class ChatMessage {

    private final InteractionHook interactionHook;

    public ChatMessage(InteractionHook interactionHook) {

        this.interactionHook = interactionHook;
    }

    public void edit(String message) {
        interactionHook.editOriginal(message).complete();
    }
}
