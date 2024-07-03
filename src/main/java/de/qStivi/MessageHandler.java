package de.qStivi;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);
    private static final ConcurrentHashMap<Message, Long> messages = new ConcurrentHashMap<>();

    /**
     * Create and track a message.
     *
     * @param event    the interaction event
     * @param content  the message content
     * @param duration the duration after which the message should be deleted
     * @param unit     the time unit of the duration
     * @return the created message
     */
    public static Message createMessage(GenericCommandInteractionEvent event, String content, long duration, TimeUnit unit) {
        try {
            Message message = event.getHook().sendMessage(content).complete();
            addMessage(message, duration, unit);
            return message;
        } catch (Exception e) {
            LOGGER.error("Failed to create message", e);
            throw new RuntimeException("Failed to create message", e);
        }
    }

    /**
     * Edit a message.
     *
     * @param message the message to edit
     * @param content the new content
     */
    public static void editMessage(Message message, String content) {
        try {
            message.editMessage(content).complete();
        } catch (Exception e) {
            LOGGER.error("Failed to edit message: {}", message.getId(), e);
            throw new RuntimeException("Failed to edit message", e);
        }
    }

    /**
     * Delete a message immediately.
     *
     * @param message the message to delete
     */
    public static void deleteMessage(Message message) {
        try {
            messages.remove(message);
            message.delete().complete();
        } catch (Exception e) {
            LOGGER.error("Failed to delete message: {}", message.getId(), e);
            throw new RuntimeException("Failed to delete message", e);
        }
    }

    /**
     * Delete all tracked messages.
     */
    public static void deleteAllMessages() {
        for (Message message : messages.keySet()) {
            deleteMessage(message);
        }
    }

    /**
     * Add a message to be tracked and deleted after a certain duration.
     *
     * @param message  the message to track
     * @param duration the duration after which the message should be deleted
     * @param unit     the time unit of the duration
     */
    private static void addMessage(Message message, long duration, TimeUnit unit) {
        long expiryTime = System.currentTimeMillis() + unit.toMillis(duration);
        messages.put(message, expiryTime);
    }

    /**
     * Delete expired messages.
     */
    public static void deleteExpiredMessages() {
        long currentTime = System.currentTimeMillis();
        messages.entrySet().removeIf(entry -> {
            if (entry.getValue() <= currentTime) {
                deleteMessage(entry.getKey());
                return true;
            }
            return false;
        });
    }
}