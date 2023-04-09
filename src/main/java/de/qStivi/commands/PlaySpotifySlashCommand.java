package de.qStivi.commands;

import de.qStivi.NoResultsException;
import de.qStivi.apis.SpotifyAPI;
import de.qStivi.apis.YouTubeAPI;
import de.qStivi.audio.QPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PlaySpotifySlashCommand implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaySpotifySlashCommand.class);

    private static final String COMMAND_NAME = "link";

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription()).addOption(OptionType.STRING, COMMAND_NAME, getDescription(), true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) throws NoResultsException, IOException {
        var option = event.getOption(COMMAND_NAME);
        var guild = event.getGuild();
        if (option == null) {
            event.getHook().editOriginal("Something ent wrong!").queue();
            return;
        }

        var lavaPlayer = QPlayer.getInstance(guild);

        lavaPlayer.openAudioConnection(event);

        // https://open.spotify.com/playlist/6eLECLM3JbU5JSZfj5p59i?si=082dc01c7c5542d9
        var sID = option.getAsString().substring(34).split("\\?")[0];
        LOGGER.info(sID);
        var playlist = SpotifyAPI.getPlaylist(sID);
        var tracks = playlist.getTracks().getItems();

        for (PlaylistTrack x : tracks) {
            var otherTrack = SpotifyAPI.getTrack(x.getTrack().getId());
            var name = otherTrack.getName();
            var artists = Arrays.stream(otherTrack.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(" - "));

            var id = YouTubeAPI.getSearchResults(name + " - " + artists).get(0).getId().getVideoId();
            lavaPlayer.play(id);
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "playspotify";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Provide a link or search query to some video, music or playlist and I will try to play it for you.";
    }
}
