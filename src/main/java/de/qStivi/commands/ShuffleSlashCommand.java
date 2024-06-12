package de.qStivi.commands;

import de.qStivi.ChatMessage;
import de.qStivi.NoResultsException;
import de.qStivi.audio.GuildMusicManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ShuffleSlashCommand implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShuffleSlashCommand.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event, ChatMessage message) throws NoResultsException, IOException {
        var guildMusicManager = GuildMusicManager.getInstance(event.getGuild().getIdLong());
        var scheduler = guildMusicManager.scheduler;
        var queue = scheduler.queue;

        if (queue.isEmpty()) {
            message.edit("Queue is empty.");
            return;
        }

        // get all items in queue, shuffle them and put them back in queue
        var queueCopy = new ArrayList<>(queue);
        Collections.shuffle(queueCopy);
        queue.clear();
        queue.addAll(queueCopy);

        message.edit("Queue shuffled.");
    }

    @NotNull
    @Override
    public String getName() {
        return "shuffle";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Shuffles the current queue.";
    }
}