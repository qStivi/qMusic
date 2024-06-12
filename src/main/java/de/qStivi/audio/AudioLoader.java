package de.qStivi.audio;

import de.qStivi.ChatMessage;
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public class AudioLoader extends AbstractAudioLoadResultHandler {
    private final GuildMusicManager mngr;
    private final ChatMessage message;
    private final boolean shouldSkipQueue;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AudioLoader.class);

    public AudioLoader(GuildMusicManager mngr, ChatMessage message, boolean shouldSkipQueue) {
        this.mngr = mngr;
        this.message = message;
        this.shouldSkipQueue = shouldSkipQueue;
        LOGGER.info("New AudioLoader initialized.");
    }

    public AudioLoader(GuildMusicManager mngr, ChatMessage message) {
        this.mngr = mngr;
        this.message = message;
        this.shouldSkipQueue = false;
        LOGGER.info("New AudioLoader initialized.");
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded result) {
        final Track track = result.getTrack();
        this.mngr.scheduler.enqueue(track, this.shouldSkipQueue);
        LOGGER.info("Track loaded and enqueued: {}", track.getInfo().getTitle());
    }



    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded result) {
        this.mngr.scheduler.enqueuePlaylist(result.getTracks());
        LOGGER.info("Playlist loaded and enqueued. Enqueued {} tracks.", result.getTracks().size());
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult result) {
        LOGGER.info("Search result loaded.");
        final List<Track> tracks = result.getTracks();

        if (tracks.isEmpty()) {
            LOGGER.info("No matches found for your input!");
            return;
        }

        final Track firstTrack = tracks.get(0);

        this.mngr.scheduler.enqueue(firstTrack, this.shouldSkipQueue);

        LOGGER.info("Search result loaded and enqueued: {}", firstTrack.getInfo().getTitle());
    }

    @Override
    public void noMatches() {
        LOGGER.info("No matches found for your input!");
        message.edit("No matches found for your input!");
    }

    @Override
    public void loadFailed(@NotNull LoadFailed result) {
        LOGGER.error("Failed to load track: {}", result.getException().getMessage());
        message.edit(result.getException().getMessage());
    }
}
