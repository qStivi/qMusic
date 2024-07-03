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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandHandler extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
    private static final List<ICommand<SlashCommandInteractionEvent>> SLASH_COMMAND_LIST = new ArrayList<>();
    private static final List<ICommand<UserContextInteractionEvent>> USER_CONTEXT_INTERACTION_COMMAND_LIST = new ArrayList<>();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    static {
        LOGGER.info("Registering commands.");
        registerCommands();
    }

    private static void registerCommands() {
        registerSlashCommands(new Play(), new PlayYoutube(), new Pause(), new Skip(), new Resume(), new Stop(), new Loop(), new Shuffle(), new Next(), new GetQueue(), new PlayNow());
        registerUserContextCommands(new Shutdown());
    }

    private static void registerSlashCommands(ICommand<SlashCommandInteractionEvent>... commands) {
        SLASH_COMMAND_LIST.addAll(Arrays.asList(commands));
    }

    private static void registerUserContextCommands(ICommand<UserContextInteractionEvent>... commands) {
        USER_CONTEXT_INTERACTION_COMMAND_LIST.addAll(Arrays.asList(commands));
    }

    public static void updateCommands() {
        List<CommandData> commandDataList = new ArrayList<>();
        SLASH_COMMAND_LIST.forEach(command -> commandDataList.add(command.getCommand()));
        USER_CONTEXT_INTERACTION_COMMAND_LIST.forEach(command -> commandDataList.add(command.getCommand()));
        Main.JDA.updateCommands().addCommands(commandDataList).complete();
    }

    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent event) {
        if (event instanceof SlashCommandInteractionEvent) {
            handleSlashCommand((SlashCommandInteractionEvent) event);
        } else if (event instanceof UserContextInteractionEvent) {
            handleUserContextCommand((UserContextInteractionEvent) event);
        }
    }

    private void handleSlashCommand(SlashCommandInteractionEvent event) {
        for (ICommand<SlashCommandInteractionEvent> command : SLASH_COMMAND_LIST) {
            if (command.getCommand().getName().equals(event.getName())) {
                EXECUTOR.submit(() -> {
                    try {
                        LOGGER.info("{} issued the {} command.", event.getUser().getName(), command.getName());
                        command.handle(event);
                    } catch (NoResultsException | IOException e) {
                        LOGGER.error(e.getMessage(), e);
                        ChatMessage.getInstance(event, false).edit(e.getMessage());
                    }
                });
                break;
            }
        }
    }

    private void handleUserContextCommand(UserContextInteractionEvent event) {
        for (ICommand<UserContextInteractionEvent> command : USER_CONTEXT_INTERACTION_COMMAND_LIST) {
            if (command.getCommand().getName().equals(event.getName())) {
                try {
                    command.handle(event);
                } catch (NoResultsException | IOException e) {
                    LOGGER.error(e.getMessage(), e);
                    ChatMessage.getInstance(event, true).edit(e.getMessage());
                }
                break;
            }
        }
    }
}