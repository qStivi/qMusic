package de.qStivi.chatBot;

import java.util.ArrayList;
import java.util.List;

public abstract class ChatBot {
    private final List<Message> chatHistory;

    public ChatBot() {
        this.chatHistory = new ArrayList<>();
    }

    public List<Message> getChatHistory() {
        return chatHistory;
    }
}
