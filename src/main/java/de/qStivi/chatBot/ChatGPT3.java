package de.qStivi.chatBot;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import de.qStivi.Properties;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ChatGPT3 extends ChatBot {

    private final OpenAiService service;

    public ChatGPT3(String systemMessage) {
        this.service = new OpenAiService(Properties.CHAT_API_KEY, Duration.of(30, TimeUnit.SECONDS.toChronoUnit()));
        this.getChatHistory().add(new Message("system", systemMessage));
    }

    public String sendMessage(Message message) {
        this.getChatHistory().add(message);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(Message.toChatMessages(this.getChatHistory()))
                .user("qBot")
                .maxTokens(256)
                .build();

        var choice = service.createChatCompletion(request).getChoices().get(0).getMessage().getContent();

        this.getChatHistory().add(new Message("assistant", choice));

        return choice;
    }
}
