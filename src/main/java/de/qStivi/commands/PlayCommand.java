package de.qStivi.commands;

import de.qStivi.Util;
import de.qStivi.apis.Spotify;
import de.qStivi.apis.YouTube;
import de.qStivi.audio.PlayerManager;
import de.qStivi.listener.ControlsManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static de.qStivi.commands.JoinCommand.join;
import static org.slf4j.LoggerFactory.getLogger;

public class PlayCommand implements ICommand {

    private static final Logger logger = getLogger(PlayCommand.class);


    public String playSong(OptionMapping optionMapping, boolean shuffle, TextChannel channel, Guild guild, SlashCommandEvent event) throws IOException, ParseException, SpotifyWebApiException, InterruptedException {

        var song = optionMapping.getAsString().trim();

        RequestType requestType = getRequestType(song);

        if (requestType == RequestType.YOUTUBE) {
            YoutubeType youtubeType = getYouTubeType(song);
            if (youtubeType != null) {
                switch (youtubeType) {
                    case TRACK -> song = playYoutubeTrack(song, guild);
                    case PLAYLIST -> song = playYoutubePlaylist(song, shuffle, channel);
                }
            }
        } else if (requestType == RequestType.SPOTIFY) {
            SpotifyType spotifyType = getSpotifyType(song);
            if (spotifyType != null) {
                switch (spotifyType) {
                    case TRACK -> song = playSpotifyTrack(song, channel);
                    case PLAYLIST -> song = playSpotifyPlaylist(song, shuffle, channel);
                    case ALBUM -> song = playSpotifyAlbum(song, shuffle);
                    case ARTIST -> song = playSpotifyArtist(song, shuffle);
                }
            }
        } else if (requestType == RequestType.SEARCH) {
            song = searchPlay(song, channel);
        }
        if (song != null) ControlsManager.getINSTANCE().sendMessage(event, guild);

        return song;
    }

    private String playSpotifyArtist(String arg0, Boolean shuffle) {

        logger.info(arg0 + shuffle);
        logger.error("NOT YET IMPLEMENTED!");
        return null;
    }

    private String playSpotifyAlbum(String arg0, Boolean shuffle) {

        logger.info(arg0 + shuffle);
        logger.error("NOT YET IMPLEMENTED!");
        return null;
    }

    private String playSpotifyPlaylist(String arg0, Boolean shuffle, TextChannel channel) throws IOException, ParseException, SpotifyWebApiException, InterruptedException {

        String id = null;
        if (arg0.startsWith("spotify:playlist:")) {
            id = arg0.split(":")[2];
        }
        if (arg0.startsWith("open.spotify.com/playlist/")) {
            var temp = arg0.split("/")[2];
            id = temp.split("\\?")[0];
        }
        if (arg0.startsWith("https://open.spotify.com/playlist/")) {
            var temp = arg0.split("/")[4];
            id = temp.split("\\?")[0];
        }

        var playlist = new Spotify().getFormattedPlaylist(id);
        if (shuffle) Collections.shuffle(Collections.singletonList(playlist));
        for (String link : playlist) {
            searchPlay(link, channel);
        }
        return arg0;
    }

    private String playSpotifyTrack(String link, TextChannel channel) throws IOException, ParseException, SpotifyWebApiException {

        if (link.contains("open.spotify.com/track/")) {
            link = link.replace("https://", "");
            link = link.split("/")[2];
            link = link.split("\\?")[0];
        } else if (link.startsWith("spotify:track:")) {
            link = link.split(":")[2];
        }

        Spotify spotify = new Spotify();
        String search = spotify.getTrackArtists(link) + " " + spotify.getTrackName(link);
        search = searchPlay(search, channel);
        return search;
    }

    private SpotifyType getSpotifyType(String link) {
        if (link.contains("track")) {
            return SpotifyType.TRACK;
        } else if (link.contains("playlist")) {
            return SpotifyType.PLAYLIST;
        } else if (link.contains("album")) {
            return SpotifyType.ALBUM;
        } else if (link.contains("artist") || link.contains("\uD83E\uDDD1\u200D\uD83C\uDFA8")) {
            return SpotifyType.ARTIST;
        }
        return null;
    }

    private String playYoutubePlaylist(String link, Boolean randomizeOrder, TextChannel channel) throws IOException {

        List<String> ids = YouTube.getPlaylistItemsByLink(link);
        if (randomizeOrder) Collections.shuffle(ids);
        for (String id : ids) {
            PlayerManager.getINSTANCE().loadAndPlay(channel.getGuild(), "https://youtu.be/" + id);
        }
        channel.sendMessage("Added " + ids.size() + " songs to the queue.").queue();
        return link;
    }

    private String playYoutubeTrack(String url, Guild guild) {
        PlayerManager.getINSTANCE().loadAndPlay(guild, url);
        return url;
    }

    private YoutubeType getYouTubeType(String link) {
        if (link.contains("youtube.com/watch?v=") || link.contains("youtu.be/")) {
            return YoutubeType.TRACK;
        } else if (link.contains("youtube.com/playlist?list=")) {
            return YoutubeType.PLAYLIST;
        }
        logger.error("Something went wrong!");
        return null;
    }

    private RequestType getRequestType(String arg0) {
        if (Util.isValidLink(arg0)) {
            if (arg0.contains("youtube") || arg0.contains("youtu.be")) {
                return RequestType.YOUTUBE;
            } else if (arg0.contains("spotify")) {
                return RequestType.SPOTIFY;
            }
        } else {
            return RequestType.SEARCH;
        }
        logger.error("Something went wrong!");
        return null;
    }

    private String searchPlay(String search, TextChannel channel) throws IOException {
        search = Util.cleanForURL(search);
        String id = YouTube.getVideoIdBySearchQuery(search);
        String link = "https://youtu.be/" + id;
        PlayerManager.getINSTANCE().loadAndPlay(channel.getGuild(), link);
        return link;
    }

    @NotNull
    @Override
    public CommandData getCommand() {
        return new CommandData(getName(), getDescription())
                .addOptions(
                        new OptionData(OptionType.STRING, "query", "Link or search query to some music.")
                                .setRequired(true),
                        new OptionData(OptionType.BOOLEAN, "shuffle", "Do you want the playlist to be shuffled?"));
    }

    @Override
    public void handle(SlashCommandEvent event) {
        var hook = event.getHook();
        if (!join(event.getGuild(), event.getMember())) {
            hook.editOriginal("Please join a channel, so I can play your request.").queue();
            return;
        }
        try {
//            if (event.getOption("shuffle") != null) {
//                if (event.getOption("shuffle").getAsBoolean()) {
//                    hook.sendMessage(playSong(event.getOption("query").getAsString(), true, event.getTextChannel(), event.getGuild())).queue();
//                }
//            } else {
            var msg = playSong(event.getOptions().get(0), true, event.getTextChannel(), event.getGuild(), event);
            if (msg != null) {
                hook.editOriginal(msg).queue();
            } else {

                hook.editOriginal("Something went wrong!").queue();
            }
//            }
        } catch (Exception e) {
            e.printStackTrace();
            hook.editOriginal("Something went wrong :(").queue();
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "play";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Plays music.";
    }

    private enum RequestType {
        YOUTUBE,
        SPOTIFY,
        SEARCH
    }

    private enum YoutubeType {
        TRACK,
        PLAYLIST
    }

    private enum SpotifyType {
        TRACK,
        PLAYLIST,
        ALBUM,
        ARTIST
    }
}
