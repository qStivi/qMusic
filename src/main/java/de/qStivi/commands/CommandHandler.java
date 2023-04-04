package de.qStivi.commands;

import de.qStivi.NoResultsException;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandHandler extends ListenerAdapter {

    public static final List<ICommand<SlashCommandInteractionEvent>> SLASH_COMMAND_LIST = new ArrayList<>();
    public static final List<ICommand<UserContextInteractionEvent>> USER_CONTEXT_INTERACTION_COMMAND_LIST = new ArrayList<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    static {
        LOGGER.info("Registering commands.");
        SLASH_COMMAND_LIST.add(new PlaySlashCommand());
//        SLASH_COMMAND_LIST.add(new PlaySpotifySlashCommand());
        USER_CONTEXT_INTERACTION_COMMAND_LIST.add(new ShutdownUserContextCommand());
    }

    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent event) {
        for (var command : SLASH_COMMAND_LIST) {
            if (command.getCommand().getName().equals(event.getName())) {
                event.deferReply().queue();
                new Thread(() -> {
                    try {
                        command.handle((SlashCommandInteractionEvent) event);
                    } catch (NoResultsException | IOException e) {
                        LOGGER.error(e.getMessage());
                        event.getHook().editOriginal(e.getMessage()).queue();
                    }
                }).start();
            }
        }

        for (var command : USER_CONTEXT_INTERACTION_COMMAND_LIST) {
            if (command.getCommand().getName().equals(event.getName())) {
                try {
                    command.handle((UserContextInteractionEvent) event);
                } catch (NoResultsException | IOException e) {
                    event.reply(e.getMessage()).queue();
                }
            }
        }
    }
}
