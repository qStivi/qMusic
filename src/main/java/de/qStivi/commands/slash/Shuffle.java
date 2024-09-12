package de.qStivi.commands.slash;

import de.qStivi.ChatMessage;
import de.qStivi.NoResultsException;
import de.qStivi.audio.AudioLoader;
import de.qStivi.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static de.qStivi.Util.sendQueue;

public class Shuffle implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Shuffle.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) throws NoResultsException, IOException {
        event.deferReply().complete();
        var guildMusicManager = AudioLoader.getInstance(event.getGuild().getIdLong()).mngr;
        var scheduler = guildMusicManager.scheduler;
        var queue = scheduler.queue;

        if (queue.isEmpty()) {
            ChatMessage.getInstance(event).edit("Queue is empty.");
            return;
        }

        // get all items in queue, shuffle them and put them back in queue
        var queueCopy = new ArrayList<>(queue);
        Collections.shuffle(queueCopy);
        queue.clear();
        queue.addAll(queueCopy);

        event.getChannel().sendMessage("Queue shuffled.").queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));

        sendQueue(event);
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