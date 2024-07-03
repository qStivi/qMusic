package de.qStivi.audio;

import de.qStivi.ChatMessage;
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioLoader extends AbstractAudioLoadResultHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AudioLoader.class);
    private static final Map<Long, AudioLoader> INSTANCE_MAP = new HashMap<>();
    public final GuildMusicManager mngr;
    private final Long guildID;
    private boolean shouldSkipQueue = false;
    private boolean shouldSkipCurrent = false;
    private boolean shuffle = false;

    private AudioLoader(Long guildID) {
        this.mngr = GuildMusicManager.getInstance(guildID);
        this.guildID = guildID;
        LOGGER.info("New AudioLoader initialized for guildID: {}", guildID);
    }

    public static AudioLoader getInstance(Long guildID) {
        return INSTANCE_MAP.computeIfAbsent(guildID, AudioLoader::new);
    }

    public void setShouldSkipQueue(boolean shouldSkipQueue) {
        this.shouldSkipQueue = shouldSkipQueue;
    }

    public void setShouldSkipCurrent(boolean shouldSkipCurrent) {
        this.shouldSkipCurrent = shouldSkipCurrent;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded trackLoaded) {
        final Track track = trackLoaded.getTrack();
        enqueueTrack(track);
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded result) {
        this.mngr.scheduler.enqueuePlaylist(result.getTracks(), this.shouldSkipQueue, this.shouldSkipCurrent, this.shuffle);
        resetSkipFlags();
        LOGGER.info("Playlist loaded and enqueued. Enqueued {} tracks.", result.getTracks().size());
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult result) {
        LOGGER.info("Search result loaded.");
        final List<Track> tracks = result.getTracks();

        if (tracks.isEmpty()) {
            LOGGER.info("No matches found for your input!");
            ChatMessage.getInstance().edit("No matches found for your input!");
            return;
        }

        enqueueTrack(tracks.get(0));
    }

    @Override
    public void noMatches() {
        LOGGER.info("No matches found for your input!");
        ChatMessage.getInstance().edit("No matches found for your input!");
    }

    @Override
    public void loadFailed(@NotNull LoadFailed result) {
        LOGGER.error("Failed to load track: {}", result.getException().getMessage());
        ChatMessage.getInstance().edit(result.getException().getMessage());
    }

    private void enqueueTrack(Track track) {
        this.mngr.scheduler.enqueue(track, this.shouldSkipQueue, this.shouldSkipCurrent);
        LOGGER.info("Track loaded and enqueued: {}", track.getInfo().getTitle());
        if (this.shouldSkipCurrent) {
            mngr.skip();
            this.shouldSkipCurrent = false;
        }
    }

    private void resetSkipFlags() {
        if (this.shouldSkipCurrent) {
            mngr.skip();
            this.shouldSkipCurrent = false;
        }
    }
}