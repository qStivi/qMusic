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
    private final boolean shouldSkipQueue;

    private AudioLoader(Long guildID, boolean shouldSkipQueue) {
        this.shouldSkipQueue = shouldSkipQueue;
        this.mngr = new GuildMusicManager(guildID);
        this.guildID = guildID;

        LOGGER.info("New AudioLoader initialized.");
    }

    private AudioLoader(Long guildID) {
        this.shouldSkipQueue = false;
        this.mngr = new GuildMusicManager(guildID);
        this.guildID = guildID;
        LOGGER.info("New AudioLoader initialized.");
    }

    public static AudioLoader getInstance(Long guildID, boolean shouldSkipQueue) {
        if (INSTANCE_MAP.containsKey(guildID)) {
            return INSTANCE_MAP.get(guildID);
        } else {
            var instance = new AudioLoader(guildID, shouldSkipQueue);
            INSTANCE_MAP.put(guildID, instance);
            return instance;
        }
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
        ChatMessage.getInstance().setMessage("No matches found for your input!");
    }

    @Override
    public void loadFailed(@NotNull LoadFailed result) {
        LOGGER.error("Failed to load track: {}", result.getException().getMessage());
        ChatMessage.getInstance().setMessage(result.getException().getMessage());
    }
}
