package de.qStivi.audio;

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
    private final long guildId;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GuildMusicManager.class);

    private GuildMusicManager(long guildId) {
        this.guildId = guildId;

        LOGGER.info("New GuildMusicManager initialized.");
    }

    public static GuildMusicManager getInstance(Long guildID) {
        if (INSTANCE_MAP.containsKey(guildID)) {
            return INSTANCE_MAP.get(guildID);
        } else {
            var instance = new GuildMusicManager(guildID);
            INSTANCE_MAP.put(guildID, instance);
            return instance;
        }
    }

    public void stop() {
        LOGGER.info("Stopping...");
        this.scheduler.queue.clear();
        LOGGER.info("Queue cleared.");

        this.getPlayer().ifPresent(
                (player) -> player.setPaused(false)
                        .setTrack(null)
                        .subscribe()
        );
        LOGGER.info("Set paused to false and track set to null.");
    }

    public Optional<Link> getLink() {
        LOGGER.info("Getting link for guild: {}", this.guildId);
        return Optional.ofNullable(
                Main.LAVALINK.getLinkIfCached(this.guildId)
        );
    }

    public Optional<LavalinkPlayer> getPlayer() {
        LOGGER.info("Getting player for guild: {}", this.guildId);
        return this.getLink().map(Link::getCachedPlayer);
    }

    public void pause() {
        LOGGER.info("Pausing...");
        this.getPlayer().ifPresent(
                (player) -> player.setPaused(true).subscribe()
        );
        LOGGER.info("Paused.");
    }

    public void skip() {
        LOGGER.info("Skipping...");
        this.getPlayer().ifPresent(
                (player) -> player.setTrack(this.scheduler.queue.poll()).subscribe()
        );
        LOGGER.info("Skipped.");
    }

    public void continuePlaying() {
        LOGGER.info("Continuing to play...");
        this.getPlayer().ifPresent(
                (player) -> player.setPaused(false).subscribe()
        );
        LOGGER.info("Continued to play.");
    }

    public void loop() {
        LOGGER.info("Toggling loop...");
        this.scheduler.loop = !this.scheduler.loop;
        // TODO: Send a message to the user that the loop state has changed
        LOGGER.info("Loop toggled.");
    }
}
