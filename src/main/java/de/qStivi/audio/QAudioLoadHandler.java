package de.qStivi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QAudioLoadHandler implements AudioLoadResultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(QAudioLoadHandler.class);
    private final QAudioEventAdapter qAudioEventAdapter;

    public QAudioLoadHandler(QAudioEventAdapter audioEventAdapter) {
        LOGGER.info("Initializing new AudioLoadHandler.");
        this.qAudioEventAdapter = audioEventAdapter;
        LOGGER.info("new AudioLoadHandler successfully initialized.");
    }

    QAudioEventAdapter getAudioEventHandler() {
        return qAudioEventAdapter;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        LOGGER.info("Track loaded! - " + track.getInfo().author + " - " + track.getInfo().title + " (" + track.getIdentifier() + ")");
        qAudioEventAdapter.queue(track);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        LOGGER.info("Playlist loaded! - " + playlist.getName());
        for (AudioTrack track : playlist.getTracks()) {
            qAudioEventAdapter.queue(track);
        }
    }

    @Override
    public void noMatches() {
        LOGGER.warn("No Matches!");
    }

    @Override
    public void loadFailed(FriendlyException throwable) {
        LOGGER.error("Load failed!");
    }
}
