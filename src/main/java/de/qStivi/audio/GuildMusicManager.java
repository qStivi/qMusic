package de.qStivi.audio;

import de.qStivi.ChatMessage;
import de.qStivi.Lavalink;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GuildMusicManager {
    private static final Map<Long, GuildMusicManager> INSTANCE_MAP = new HashMap<>();
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GuildMusicManager.class);
    public final TrackScheduler scheduler = new TrackScheduler(this);
    public final long guildId;

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
        var track = this.scheduler.queue.poll();
        if (track == null) {
            ChatMessage.getInstance().delete();
        }
        Lavalink.getCachedPlayer(guildId).setTrack(track).subscribe();
        LOGGER.info("Skipped.");
    }

    public void continuePlaying() {
        LOGGER.info("Continuing to play...");
        Lavalink.getCachedPlayer(guildId).setPaused(false).subscribe();
        LOGGER.info("Continued to play.");
    }

    public void toggleLoop(SlashCommandInteractionEvent event) {
        LOGGER.info("Toggling loop...");
        this.scheduler.loop = !this.scheduler.loop;


        if (scheduler.loop) {
            event.getHook().editOriginal("Looping enabled.").queue((m) -> m.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
        } else {
            event.getHook().editOriginal("Looping disabled.").queue((m) -> m.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
        }

        LOGGER.info("Loop toggled.");
    }
}
