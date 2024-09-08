package de.qStivi.commands.context;

import de.qStivi.ChatMessage;
import de.qStivi.Main;
import de.qStivi.audio.AudioLoader;
import de.qStivi.commands.CommandHandler;
import de.qStivi.commands.ICommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Shutdown implements ICommand<UserContextInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Shutdown.class);
    private static final String ID = "219108246143631364";

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.user(getName());
    }

    @Override
    public void handle(UserContextInteractionEvent event) {
        try {
            LOGGER.info("Shutdown command received from user: {}", event.getUser().getId());
            event.reply("Shutting down...").setEphemeral(true).complete();
            if (event.getJDA().getSelfUser().getId().equals(event.getTarget().getId()) && event.getUser().getId().equals(ID)) {

                LOGGER.info("Shutdown command validated. Initiating shutdown sequence.");
                CommandHandler.setShuttingDown(true);

                // Get all guilds
                event.getJDA().getGuilds().forEach(guild -> {
                    LOGGER.info("Stopping audio player and disconnecting from voice channel in guild: {}", guild.getId());
                    // Stop all audio players
                    AudioLoader.getInstance(guild.getIdLong()).mngr.stop();
                    // Disconnect from all voice channels
                    event.getJDA().getDirectAudioController().disconnect(guild);
                    // Delete all messages from the bot
                    ChatMessage.getInstance(event).delete();
                });

                // Wait for 10 seconds
                try {
                    LOGGER.info("Waiting for 10 seconds before proceeding with shutdown.");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    LOGGER.error("Error while waiting for shutdown.", e);
                }

                // Interrupt all still running threads
                Thread.getAllStackTraces().keySet().forEach(thread -> {
                    if (thread.getName().startsWith("CommandThread")) { // Identify threads properly
                        LOGGER.info("Interrupting thread: {}", thread.getName());
                        thread.interrupt();
                    }
                });

                // Delete all remaining bot messages from all channels
                LOGGER.info("Deleting all remaining bot messages from all channels.");
                deleteBotMessagesFromAllChannelsBlocking(event.getJDA());

                // Shutdown the bot
                LOGGER.info("Shutting down JDA.");
                Main.JDA.shutdown();
                // Allow at most 30 seconds for remaining requests to finish
                try {
                    if (!Main.JDA.awaitShutdown(30, TimeUnit.SECONDS)) {
                        LOGGER.warn("JDA did not shutdown within 30 seconds. Forcing shutdown now.");
                        Main.JDA.shutdownNow(); // Cancel all remaining requests

                        // Give the bot another 30 seconds to shut down
                        if (!Main.JDA.awaitShutdown(30, TimeUnit.SECONDS)) {
                            LOGGER.error("JDA did not shutdown properly.");
                        }
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Error while shutting down.", e);
                }

                // Exit the program
                LOGGER.info("Exiting...");
                System.exit(0);
            } else {
                LOGGER.warn("Shutdown command validation failed. User: {}, Target: {}", event.getUser().getId(), event.getTarget().getId());
            }

        } catch (Exception e) {
            LOGGER.error("There was an error while shutting down.", e);
            System.exit(1);
        }
    }

    public void deleteBotMessagesFromAllChannelsBlocking(JDA jda) {
        for (Guild guild : jda.getGuilds()) {
            LOGGER.info("Deleting bot messages from guild: {}", guild.getId());

            for (TextChannel channel : guild.getTextChannels()) {
                LOGGER.info("Deleting bot messages from channel: {}", channel.getId());
                Message lastMessage = null;

                while (true) {
                    try {
                        // Retrieve up to 100 messages starting from the last message fetched
                        List<Message> messages = (lastMessage == null)
                                ? channel.getHistory().retrievePast(100).complete()
                                : channel.getHistoryBefore(lastMessage.getId(), 100).complete().getRetrievedHistory();

                        if (messages.isEmpty()) break; // Exit if there are no more messages to process

                        // Filter messages sent by the bot
                        List<Message> botMessages = messages.stream()
                                .filter(message -> message.getAuthor().getId().equals(jda.getSelfUser().getId()))
                                .toList();

                        LOGGER.info("Retrieved {} bot messages from channel: {}", botMessages.size(), channel.getId());

                        if (!botMessages.isEmpty()) {
                            try {
                                channel.purgeMessages(botMessages);
                                LOGGER.info("Deleted {} bot messages from channel: {}", botMessages.size(), channel.getId());
                            } catch (IllegalArgumentException e) {
                                LOGGER.error("Failed to delete some messages older than 14 days in channel: {}", channel.getId(), e);
                            }
                        }

                        // Update the last message for pagination
                        lastMessage = messages.getLast();

                    } catch (Exception e) {
                        LOGGER.error("Error while deleting bot messages from channel: {}", channel.getId(), e);
                        break;
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "Shutdown";
    }
}
