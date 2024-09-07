package de.qStivi.commands.slash;

import de.qStivi.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.qStivi.Util.sendQueue;

public class GetQueue implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetQueue.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().complete();

        sendQueue(event);
    }

    @NotNull
    @Override
    public String getName() {
        return "queue";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Gets the next 10 songs in the queue.";
    }
}