package de.qStivi.audio;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GuildMusicManager {
    public final TrackScheduler scheduler = new TrackScheduler(this);
    private final long guildId;
    private final LavalinkClient lavalink;


    private static final Map<Long, GuildMusicManager> INSTANCE_MAP = new HashMap<>();

    public static GuildMusicManager getInstance(Long guildID, LavalinkClient lavalink) {
        if (INSTANCE_MAP.containsKey(guildID)) {
            return INSTANCE_MAP.get(guildID);
        } else {
            var instance = new GuildMusicManager(guildID, lavalink);
            INSTANCE_MAP.put(guildID, instance);
            return instance;
        }
    }

    private GuildMusicManager(long guildId, LavalinkClient lavalink) {
        this.lavalink = lavalink;
        this.guildId = guildId;
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
                this.lavalink.getLinkIfCached(this.guildId)
        );
    }

    public Optional<LavalinkPlayer> getPlayer() {
        return this.getLink().map(Link::getCachedPlayer);
    }
}
