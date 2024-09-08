package de.qStivi.commands;

import de.qStivi.ChatMessage;
import de.qStivi.Main;
import de.qStivi.NoResultsException;
import de.qStivi.commands.context.Shutdown;
import de.qStivi.commands.slash.*;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler extends ListenerAdapter {
    private static volatile boolean shuttingDown = false;

    public static final List<ICommand<SlashCommandInteractionEvent>> SLASH_COMMAND_LIST = new ArrayList<>();
    public static final List<ICommand<UserContextInteractionEvent>> USER_CONTEXT_INTERACTION_COMMAND_LIST = new ArrayList<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    static {
        LOGGER.info("Registering commands.");
        registerSlashCommands(new Play(), new PlayYoutube(), new Pause(), new Skip(), new Continue(), new Stop(), new Loop(), new Shuffle(), new Next(), new GetQueue(), new PlayNow());
        registerUserContextCommands(new Shutdown());
    }

    public static void updateCommands() {
        List<CommandData> commandDataList = new ArrayList<>();
        for (ICommand<SlashCommandInteractionEvent> command : CommandHandler.SLASH_COMMAND_LIST) {
            commandDataList.add(command.getCommand());
        }
        for (ICommand<UserContextInteractionEvent> command : CommandHandler.USER_CONTEXT_INTERACTION_COMMAND_LIST) {
            commandDataList.add(command.getCommand());
        }
        Main.JDA.updateCommands().addCommands(commandDataList).complete();
    }

    /**
     * Registers the given commands.
     *
     * @param commands The commands to register.
     */
    private static void registerSlashCommands(ICommand<SlashCommandInteractionEvent>... commands) {
        SLASH_COMMAND_LIST.addAll(Arrays.asList(commands));
    }

    /**
     * Registers the given commands.
     *
     * @param commands The commands to register.
     */
    private static void registerUserContextCommands(ICommand<UserContextInteractionEvent>... commands) {
        USER_CONTEXT_INTERACTION_COMMAND_LIST.addAll(Arrays.asList(commands));
    }

    public static void setShuttingDown(boolean shuttingDown) {
        CommandHandler.shuttingDown = shuttingDown;
    }

    private static void createThread(@NotNull UserContextInteractionEvent event, ICommand<UserContextInteractionEvent> command, String threadName) {
        new Thread(() -> {
            try {
                LOGGER.info("{} issued the {} command.", event.getUser().getName(), command.getName());
                command.handle(event);
            } catch (NoResultsException | IOException e) {
                LOGGER.error(e.getMessage());
                ChatMessage.getInstance(event, true).edit(e.getMessage());
            }
        }, threadName).start();
    }

    private static void createThread(@NotNull SlashCommandInteractionEvent event, ICommand<SlashCommandInteractionEvent> command, String threadName) {
        new Thread(() -> {
            try {
                LOGGER.info("{} issued the {} command.", event.getUser().getName(), command.getName());
                command.handle(event);
            } catch (NoResultsException | IOException e) {
                LOGGER.error(e.getMessage());
                ChatMessage.getInstance(event, false).edit(e.getMessage());
            }
        }, threadName).start();
    }

    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent event) {
        // Check if the bot is shutting down
        if (shuttingDown) {
            LOGGER.info("Command execution skipped: bot is shutting down.");
            return; // Skip execution if shutting down
        }

        // Slash commands
        for (var command : SLASH_COMMAND_LIST) {
            if (command.getCommand().getName().equals(event.getName())) {
                var threadName = "CommandThread-" + command.getName();
                createThread((SlashCommandInteractionEvent) event, command, threadName);
            }
        }

        // User context commands
        for (var command : USER_CONTEXT_INTERACTION_COMMAND_LIST) {
            if (command.getCommand().getName().equals(event.getName())) {
                var threadName = "CommandThread-" + command.getName();
                if (event.getName().equals("Shutdown")) {
                    threadName = "ShutdownThread-" + command.getName();
                    createThread((UserContextInteractionEvent) event, command, threadName);
                } else {
                    createThread((UserContextInteractionEvent) event, command, threadName);
                }
            }
        }
    }
}
