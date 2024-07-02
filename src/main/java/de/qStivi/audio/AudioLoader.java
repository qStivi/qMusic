package de.qStivi.audio;

import de.qStivi.ChatMessage;
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;

public class AudioLoader extends AbstractAudioLoadResultHandler {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AudioLoader.class);
    private static final HashMap<Long, AudioLoader> INSTANCE_MAP = new HashMap<>();
    public final GuildMusicManager mngr;
    private final Long guildID;
    public boolean shouldSkipQueue = false;
    public boolean shouldSkipCurrent = false;
    public boolean shuffle = false;

    private AudioLoader(Long guildID) {
        this.mngr = new GuildMusicManager(guildID);
        this.guildID = guildID;
        LOGGER.info("New AudioLoader initialized.");
    }

    public static AudioLoader getInstance(Long guildID) {
        if (INSTANCE_MAP.containsKey(guildID)) {
            return INSTANCE_MAP.get(guildID);
        } else {
            var instance = new AudioLoader(guildID);
            INSTANCE_MAP.put(guildID, instance);
            return instance;
        }
    }

    public void shouldSkipQueue(boolean shouldSkipQueue) {
        this.shouldSkipQueue = shouldSkipQueue;
    }

    public void shouldSkipCurrent(boolean shouldSkipCurrent) {
        this.shouldSkipCurrent = shouldSkipCurrent;
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded result) {
        final Track track = result.getTrack();
        this.mngr.scheduler.enqueue(track, this.shouldSkipQueue, this.shouldSkipCurrent);
        LOGGER.info("Track loaded and enqueued: {}", track.getInfo().getTitle());
        if (this.shouldSkipCurrent) {
            mngr.skip();
            this.shouldSkipCurrent = false;
        }
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded result) {
        this.mngr.scheduler.enqueuePlaylist(result.getTracks(), this.shouldSkipQueue, this.shouldSkipCurrent, this.shuffle);
        if (this.shouldSkipCurrent) {
            mngr.skip();
            this.shouldSkipCurrent = false;
        }
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

        this.mngr.scheduler.enqueue(firstTrack, this.shouldSkipQueue, this.shouldSkipCurrent);

        LOGGER.info("Search result loaded and enqueued: {}", firstTrack.getInfo().getTitle());
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

    public void shuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }
}
