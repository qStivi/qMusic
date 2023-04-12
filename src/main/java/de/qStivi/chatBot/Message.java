package de.qStivi.chatBot;

import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public record Message(String actor, String message) {
    public ChatMessage toChatMessage() {
        return new ChatMessage(actor, message);
    }

    public static List<ChatMessage> toChatMessages(List<Message> messages) {
        var list = new ArrayList<ChatMessage>();
        for (var message : messages) {
            list.add(message.toChatMessage());
        }
        return list;
    }
}
