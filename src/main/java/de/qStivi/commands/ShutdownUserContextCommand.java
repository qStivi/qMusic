package de.qStivi.commands;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownUserContextCommand implements ICommand<UserContextInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownUserContextCommand.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.user(getName());
    }

    @Override
    public void handle(UserContextInteractionEvent event) {
        if (event.getJDA().getSelfUser().getId().equals(event.getTarget().getId()) && event.getUser().getId().equals("219108246143631364")) {
            event.getHook().editOriginal("Shutting down...").complete();
            event.getJDA().shutdown();
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "Shutdown";
    }
}
