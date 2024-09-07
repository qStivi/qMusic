package de.qStivi.commands.slash;

import de.qStivi.Lavalink;
import de.qStivi.audio.AudioLoader;
import de.qStivi.commands.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static de.qStivi.Util.joinHelper;
import static de.qStivi.Util.sendQueue;

public class PlayNow implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayNow.class);
    private static final String QUERY = "query";
    private static final String SHUFFLE = "shuffle";

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.STRING, QUERY, "The thing you want to play (search or link)", true)
                .addOption(OptionType.BOOLEAN, SHUFFLE, "Shuffle the queue", false);
    }

    private static void loadSongOrPlaylist(Guild guild, boolean shuffle, String query) {
        var al = AudioLoader.getInstance(guild.getIdLong());
        al.shouldSkipQueue(true);
        al.shouldSkipCurrent(true);
        al.shuffle(shuffle);

        Lavalink.get(guild.getIdLong()).loadItem(query).subscribe(al);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().complete();
        var query = Objects.requireNonNull(event.getOption(QUERY)).getAsString();

        // Get shuffle option, false by default
        var shuffle = event.getOption(SHUFFLE) != null && event.getOption(SHUFFLE).getAsBoolean();

        // Try parsing as URL and prepend "ytsearch:" if it fails
        try {
            new java.net.URL(query);
        } catch (java.net.MalformedURLException e) {
            query = "ytsearch:" + query;
        }

        var guild = event.getGuild();

        joinHelper(event);

        loadSongOrPlaylist(guild, shuffle, query);

        // Wait for the song to be loaded
        // TODO Do this in a better way
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        sendQueue(event);
    }

    @NotNull
    @Override
    public String getName() {
        return "playnow";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Puts your song in the front of the queue.";
    }
}