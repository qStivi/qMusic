package de.qStivi.audio;

import de.qStivi.Lavalink;
import de.qStivi.Main;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GuildMusicManager {
    private static final Map<Long, GuildMusicManager> INSTANCE_MAP = new HashMap<>();
    public final TrackScheduler scheduler = new TrackScheduler(this);
    public final long guildId;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GuildMusicManager.class);

    public GuildMusicManager(long guildId) {
        this.guildId = guildId;

        LOGGER.info("New GuildMusicManager initialized.");
    }

//    public static GuildMusicManager getInstance(Long guildID) {
//        if (INSTANCE_MAP.containsKey(guildID)) {
//            return INSTANCE_MAP.get(guildID);
//        } else {
//            var instance = new GuildMusicManager(guildID);
//            INSTANCE_MAP.put(guildID, instance);
//            return instance;
//        }
//    }

    public void stop() {
        LOGGER.info("Stopping...");
        this.scheduler.queue.clear();
        LOGGER.info("Queue cleared.");
        Lavalink.getCachedPlayer(guildId).setPaused(false)
                        .setTrack(null)
                        .subscribe();
        LOGGER.info("Set paused to false and track set to null.");
    }

    public void pause() {
        LOGGER.info("Pausing...");
        Lavalink.getCachedPlayer(guildId).setPaused(true).subscribe();
        LOGGER.info("Paused.");
    }

    public void skip() {
        LOGGER.info("Skipping...");
        Lavalink.getCachedPlayer(guildId).setTrack(this.scheduler.queue.poll()).subscribe();
        LOGGER.info("Skipped.");
    }

    public void continuePlaying() {
        LOGGER.info("Continuing to play...");
        Lavalink.getCachedPlayer(guildId).setPaused(false).subscribe();
        LOGGER.info("Continued to play.");
    }

    public void loop() {
        LOGGER.info("Toggling loop...");
        this.scheduler.loop = !this.scheduler.loop;
        // TODO: Send a message to the user that the loop state has changed
        LOGGER.info("Loop toggled.");
    }
}
