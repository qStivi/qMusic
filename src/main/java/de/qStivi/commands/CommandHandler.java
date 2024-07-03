package de.qStivi.commands;

import de.qStivi.ChatMessage;
import de.qStivi.Main;
import de.qStivi.NoResultsException;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandHandler extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static final List<ICommand<GenericCommandInteractionEvent>> commands = new ArrayList<>();

    static {
        LOGGER.info("Registering commands.");
        registerCommands();
    }

    /**
     * Registers all commands implementing the ICommand interface.
     */
    private static void registerCommands() {
        Reflections reflections = new Reflections("de.qStivi.commands");
        Set<Class<? extends ICommand>> commandClasses = reflections.getSubTypesOf(ICommand.class);
        for (Class<? extends ICommand> commandClass : commandClasses) {
            try {
                @SuppressWarnings("unchecked")
                ICommand<GenericCommandInteractionEvent> command = (ICommand<GenericCommandInteractionEvent>) commandClass.getDeclaredConstructor().newInstance();
                commands.add(command);
                LOGGER.info("Registered command: {}", command.getName());
            } catch (Exception e) {
                LOGGER.error("Failed to register command: {}", commandClass.getName(), e);
            }
        }
    }

    /**
     * Updates the commands on the server.
     */
    public static void updateCommands() {
        List<CommandData> commandData = new ArrayList<>();
        for (ICommand<GenericCommandInteractionEvent> command : commands) {
            commandData.add(command.getCommand());
        }
        try {
            Main.JDA.updateCommands().addCommands(commandData).complete();
            LOGGER.info("Commands updated successfully.");
        } catch (Exception e) {
            LOGGER.error("Failed to update commands", e);
        }
    }

    /**
     * Shutdown the executor service gracefully.
     */
    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
                if (!EXECUTOR.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    LOGGER.error("Executor did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onGenericCommandInteraction(GenericCommandInteractionEvent event) {
        for (ICommand<GenericCommandInteractionEvent> command : commands) {
            if (command.getCommand().getName().equals(event.getName())) {
                EXECUTOR.submit(() -> {
                    try {
                        LOGGER.info("{} issued the {} command.", event.getUser().getName(), command.getName());
                        command.handle(event);
                    } catch (NoResultsException | IOException e) {
                        LOGGER.error("Error handling command {}: {}", command.getName(), e.getMessage(), e);
                        ChatMessage.getInstance(event).edit(e.getMessage());
                    } catch (Exception e) {
                        LOGGER.error("Unexpected error handling command {}: {}", command.getName(), e.getMessage(), e);
                        ChatMessage.getInstance(event).edit("An unexpected error occurred.");
                    }
                });
                break;
            }
        }
    }
}