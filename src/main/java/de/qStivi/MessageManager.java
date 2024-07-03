package de.qStivi;

import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MessageManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageManager.class);
    private static final ConcurrentHashMap<Message, Long> messages = new ConcurrentHashMap<>();

    public static void addMessage(Message message, long duration, TimeUnit unit) {
        long expiryTime = System.currentTimeMillis() + unit.toMillis(duration);
        messages.put(message, expiryTime);
    }

    public static void deleteAllMessages() {
        for (Message message : messages.keySet()) {
            try {
                message.delete().complete();
            } catch (Exception e) {
                LOGGER.error("Failed to delete message: {}", message.getId(), e);
            }
        }
        messages.clear();
    }

    public static void deleteExpiredMessages() {
        long currentTime = System.currentTimeMillis();
        messages.entrySet().removeIf(entry -> {
            if (entry.getValue() <= currentTime) {
                try {
                    entry.getKey().delete().complete();
                } catch (Exception e) {
                    LOGGER.error("Failed to delete message: {}", entry.getKey().getId(), e);
                }
                return true;
            }
            return false;
        });
    }

    public static void deleteMessage(Message message) {
        messages.remove(message);
        try {
            message.delete().complete();
        } catch (Exception e) {
            LOGGER.error("Failed to delete message: {}", message.getId(), e);
        }
    }
}