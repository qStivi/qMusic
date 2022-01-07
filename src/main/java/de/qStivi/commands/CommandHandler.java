package de.qStivi.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler extends ListenerAdapter {
    public static final List<ICommand> COMMAND_LIST = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CommandHandler() {
        logger.debug("Registering commands.");
        COMMAND_LIST.add(new StopCommand());
        COMMAND_LIST.add(new ContinueCommand());
        COMMAND_LIST.add(new PauseCommand());
        COMMAND_LIST.add(new RepeatCommand());
        COMMAND_LIST.add(new SkipCommand());
        COMMAND_LIST.add(new JoinCommand());
        COMMAND_LIST.add(new LeaveCommand());
        COMMAND_LIST.add(new PlayCommand());
        COMMAND_LIST.add(new TestCommand());
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        for (var command : COMMAND_LIST) {
            if (command.getCommand().getName().equals(event.getName())) {
                command.handle(event);
            }
        }
    }
}
