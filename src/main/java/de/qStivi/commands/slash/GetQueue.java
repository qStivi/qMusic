package de.qStivi.commands.slash;

import de.qStivi.audio.AudioLoader;
import de.qStivi.commands.ICommand;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

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
        StringBuilder sb = new StringBuilder();

        var i = 0;
        for (Track audioTrack : AudioLoader.getInstance(event.getGuild().getIdLong()).mngr.scheduler.queue) {
            sb.append(i++).append(". ").append("`").append(audioTrack.getInfo().getTitle()).append("`").append(" by ").append("`").append(audioTrack.getInfo().getAuthor()).append("`").append("\n");
            if (i == 10) {
                sb.append("...");
                break;
            }
        }

        if (sb.isEmpty()) {
            sb.append("The queue is empty.");
            event.getHook().editOriginal(sb.toString()).queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        event.getHook().editOriginal(sb.toString()).queue((msg) -> msg.delete().queueAfter(15, TimeUnit.MINUTES));
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