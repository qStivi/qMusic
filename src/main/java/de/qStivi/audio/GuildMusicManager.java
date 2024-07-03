package de.qStivi.audio;

import de.qStivi.ChatMessage;
import de.qStivi.Lavalink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GuildMusicManager {
    private static final Map<Long, GuildMusicManager> INSTANCE_MAP = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildMusicManager.class);
    public final TrackScheduler scheduler = new TrackScheduler(this);
    public final long guildId;

    private GuildMusicManager(long guildId) {
        this.guildId = guildId;
        LOGGER.info("New GuildMusicManager initialized for guildId: {}", guildId);
    }

    public static GuildMusicManager getInstance(Long guildId) {
        return INSTANCE_MAP.computeIfAbsent(guildId, GuildMusicManager::new);
    }

    public void stop() {
        LOGGER.info("Stopping music for guildId: {}", guildId);
        scheduler.queue.clear();
        LOGGER.info("Queue cleared.");
        Lavalink.getCachedPlayer(guildId).setPaused(false).setTrack(null).subscribe();
        LOGGER.info("Music stopped and track set to null.");
    }

    public void pause() {
        LOGGER.info("Pausing music for guildId: {}", guildId);
        Lavalink.getCachedPlayer(guildId).setPaused(true).subscribe();
        LOGGER.info("Music paused.");
    }

    public void skip() {
        LOGGER.info("Skipping track for guildId: {}", guildId);
        var track = scheduler.queue.poll();
        if (track == null) {
            ChatMessage.getInstance().delete();
        }
        Lavalink.getCachedPlayer(guildId).setTrack(track).subscribe();
        LOGGER.info("Track skipped.");
    }

    public void continuePlaying() {
        LOGGER.info("Continuing playback for guildId: {}", guildId);
        Lavalink.getCachedPlayer(guildId).setPaused(false).subscribe();
        LOGGER.info("Playback continued.");
    }

    public void toggleLoop() {
        scheduler.loop = !scheduler.loop;
        LOGGER.info("Loop toggled for guildId: {}. New loop state: {}", guildId, scheduler.loop);
    }
}