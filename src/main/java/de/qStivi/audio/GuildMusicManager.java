package de.qStivi.audio;

import de.qStivi.Main;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GuildMusicManager {
    private static final Map<Long, GuildMusicManager> INSTANCE_MAP = new HashMap<>();
    public final TrackScheduler scheduler = new TrackScheduler(this);
    private final long guildId;

    private GuildMusicManager(long guildId) {
        this.guildId = guildId;
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
        this.scheduler.queue.clear();

        this.getPlayer().ifPresent(
                (player) -> player.setPaused(false)
                        .setTrack(null)
                        .subscribe()
        );
    }

    public Optional<Link> getLink() {
        return Optional.ofNullable(
                Main.LAVALINK.getLinkIfCached(this.guildId)
        );
    }

    public Optional<LavalinkPlayer> getPlayer() {
        return this.getLink().map(Link::getCachedPlayer);
    }

    public void pause() {
        this.getPlayer().ifPresent(
                (player) -> player.setPaused(true).subscribe()
        );
    }

    public void skip() {
        this.getPlayer().ifPresent(
                (player) -> player.setTrack(this.scheduler.queue.poll()).subscribe()
        );
    }

    public void continuePlaying() {
        this.getPlayer().ifPresent(
                (player) -> player.setPaused(false).subscribe()
        );
    }
}
