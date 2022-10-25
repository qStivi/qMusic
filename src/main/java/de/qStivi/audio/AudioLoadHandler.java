package de.qStivi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioLoadHandler implements AudioLoadResultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AudioLoadHandler.class);
    private final TrackScheduler trackScheduler;
    private boolean isLoading = false;

    private boolean loadFailed = false;

    public AudioLoadHandler(AudioPlayer player, Guild guild) {
        LOGGER.info("Initializing new AudioLoadHandler.");
        trackScheduler = new TrackScheduler(player, guild);
        LOGGER.info("new AudioLoadHandler successfully initialized.");
    }

    boolean loadFailed() {
        return loadFailed;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        LOGGER.info("Track loaded! - " + track.getInfo().author + " - " + track.getInfo().title + " (" + track.getIdentifier() + ")");
        trackScheduler.queue(track);
        setLoading(false);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        LOGGER.info("Playlist loaded! - " + playlist.getName());
        for (AudioTrack track : playlist.getTracks()) {
            trackScheduler.queue(track);
            setLoading(false);
        }
    }

    @Override
    public void noMatches() {
        loadFailed = true;
        setLoading(false);
        LOGGER.warn("No Matches!");
    }

    @Override
    public void loadFailed(FriendlyException throwable) {
        loadFailed = true;
        setLoading(false);
        LOGGER.error("Load failed!");
    }
}
