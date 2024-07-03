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

    private static final List<ICommand> commands = new ArrayList<>();

    static {
        LOGGER.info("Registering commands.");
        registerCommands();
    }

    private static void registerCommands() {
        Reflections reflections = new Reflections("de.qStivi.commands");
        Set<Class<? extends ICommand>> commandClasses = reflections.getSubTypesOf(ICommand.class);
        for (Class<? extends ICommand> commandClass : commandClasses) {
            try {
                ICommand command = commandClass.getDeclaredConstructor().newInstance();
                commands.add(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateCommands() {
        List<CommandData> commandData = new ArrayList<>();
        for (ICommand command : commands) {
            commandData.add(command.getCommand());
            LOGGER.info("Registered command: {}", command.getName());
        }
        Main.JDA.updateCommands().addCommands(commandData).complete();
    }

    @Override
    public void onGenericCommandInteraction(GenericCommandInteractionEvent event) {
        for (ICommand command : commands) {
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
}