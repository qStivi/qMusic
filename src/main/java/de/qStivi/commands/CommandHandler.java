package de.qStivi.commands;

import de.qStivi.ChatMessage;
import de.qStivi.Main;
import de.qStivi.NoResultsException;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandHandler extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
    private static volatile boolean shuttingDown = false;

    public static final List<ICommand<SlashCommandInteractionEvent>> SLASH_COMMAND_LIST = new ArrayList<>();
    public static final List<ICommand<UserContextInteractionEvent>> USER_CONTEXT_INTERACTION_COMMAND_LIST = new ArrayList<>();

    static {
        LOGGER.info("Registering commands.");
        registerAllCommands();
    }

    public static void registerAllCommands() {
        Reflections reflections = new Reflections("de.qStivi.commands");

        // Get all classes that implement ICommand for Slash and User Context Commands
        Set<Class<? extends ICommand>> commandClasses = reflections.getSubTypesOf(ICommand.class);

        // Loop through found classes and register them
        for (Class<? extends ICommand> commandClass : commandClasses) {
            try {
                // Create an instance of the command
                ICommand<?> command = commandClass.getDeclaredConstructor().newInstance();

                // Check if it's a Slash Command or User Context Command and register accordingly
                if (command instanceof ICommand<SlashCommandInteractionEvent>) {
                    registerSlashCommands((ICommand<SlashCommandInteractionEvent>) command);
                } else if (command instanceof ICommand<UserContextInteractionEvent>) {
                    registerUserContextCommands((ICommand<UserContextInteractionEvent>) command);
                }

                LOGGER.info("Successfully registered command: {}", commandClass.getSimpleName());

            } catch (Exception e) {
                LOGGER.error("Failed to register command: {}", commandClass.getSimpleName(), e);
            }
        }
    }

    public static void updateCommands() {
        List<CommandData> commandDataList = new ArrayList<>();

        // Iterate through registered slash commands
        SLASH_COMMAND_LIST.forEach(command -> commandDataList.add(command.getCommand()));

        // Iterate through registered user context commands
        USER_CONTEXT_INTERACTION_COMMAND_LIST.forEach(command -> commandDataList.add(command.getCommand()));

        // Update commands in Discord
        if (!commandDataList.isEmpty()) {
            Main.JDA.updateCommands().addCommands(commandDataList).complete();
            LOGGER.info("Updated bot commands.");
        } else {
            LOGGER.warn("No commands were found to update.");
        }
    }

    private static void registerSlashCommands(ICommand<SlashCommandInteractionEvent>... commands) {
        SLASH_COMMAND_LIST.addAll(Arrays.asList(commands));
    }

    private static void registerUserContextCommands(ICommand<UserContextInteractionEvent>... commands) {
        USER_CONTEXT_INTERACTION_COMMAND_LIST.addAll(Arrays.asList(commands));
    }

    public static void setShuttingDown(boolean shuttingDown) {
        CommandHandler.shuttingDown = shuttingDown;
    }

    /**
     * Creates a new thread for handling the given command.
     *
     * @param event      The event to handle.
     * @param command    The command to execute.
     * @param threadName The name of the thread.
     */
    private static void createThread(@NotNull GenericCommandInteractionEvent event,
                                     ICommand<GenericCommandInteractionEvent> command
            , String threadName) {
        new Thread(() -> {
            try {
                LOGGER.info("{} issued the {} command.", event.getUser().getName(), command.getName());
                command.handle(event);
            } catch (NoResultsException | IOException e) {
                LOGGER.error("Error while handling command: ", e);
                ChatMessage.getInstance(event, true).edit(e.getMessage());
            }
        }, threadName).start();
    }

    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent event) {
        if (shuttingDown) {
            LOGGER.info("Command execution skipped: bot is shutting down.");
            return;
        }

        if (event instanceof SlashCommandInteractionEvent slashEvent) {
            handleSlashCommand(slashEvent);
        } else if (event instanceof UserContextInteractionEvent userEvent) {
            handleUserContextCommand(userEvent);
        }
    }

    private void handleSlashCommand(SlashCommandInteractionEvent event) {
        SLASH_COMMAND_LIST.stream()
                .filter(command -> command.getCommand().getName().equals(event.getName()))
                .findFirst()
                .ifPresent(command -> createThread(event, (ICommand<SlashCommandInteractionEvent>) command,
                        "CommandThread-" + command.getName()));
    }

    private void handleUserContextCommand(UserContextInteractionEvent event) {
        USER_CONTEXT_INTERACTION_COMMAND_LIST.stream()
                .filter(command -> command.getCommand().getName().equals(event.getName()))
                .findFirst()
                .ifPresent(command -> {
                    String threadName = command.getName().equals("Shutdown") ? "ShutdownThread-" + command.getName() : "CommandThread-" + command.getName();
                    createThread(event, (ICommand<GenericCommandInteractionEvent>) command, threadName);
                });
    }
}