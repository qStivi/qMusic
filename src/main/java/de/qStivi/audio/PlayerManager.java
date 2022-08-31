package de.qStivi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    private static final Logger logger = LoggerFactory.getLogger(PlayerManager.class);
    private static final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
    private static final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

    static {
        audioPlayerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        audioPlayerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);

        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);

        logger.info("Player manager loaded.");
    }

    private static GuildMusicManager getMusicManager(Guild guild) {
        return musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            var guildMusicManager = new GuildMusicManager(audioPlayerManager);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getAudioSendHandler());

            return guildMusicManager;
        });
    }

    // TODO test this with playlists
    public static void loadAndPlay(Guild guild, String trackURL) {
        var musicManager = getMusicManager(guild);

        audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.trackScheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                musicManager.trackScheduler.queue(playlist.getTracks().get(0));
            }

            @Override
            public void noMatches() {
                logger.error("No Matches!");
            }

            @Override
            public void loadFailed(FriendlyException e) {
                logger.error(e.getMessage(), e);
            }
        });
    }


    /**
     * Starts playing the next track in the queue.<br><br>
     * If the queue is empty the playback is going to be stopped.
     *
     * @param guild is the guild where the music should be skipped.
     * @see TrackScheduler
     */
    public static void skip(Guild guild) {
        getMusicManager(guild).trackScheduler.skip();
    }

    /**
     * Pauses the currently playing track.
     *
     * @param guild is the guild where the music should be paused.
     * @see TrackScheduler
     */
    public static void pause(Guild guild) {
        getMusicManager(guild).trackScheduler.pause();
    }

    /**
     * Continues to play a track if one is paused.
     *
     * @param guild is the guild where the music should be continued.
     * @see TrackScheduler
     */
    public static void unpause(Guild guild) {
        getMusicManager(guild).trackScheduler.unpause();
    }

    /**
     * Sets the value of isRepeating
     *
     * @param guild  is the guild where the music should be repeated.
     * @param repeat is the new value.
     * @see TrackScheduler
     */
    public static void setRepeat(Guild guild, boolean repeat) {
        getMusicManager(guild).trackScheduler.setRepeating(repeat);
    }

    /**
     * Returns weather the current track is being repeated or not.
     *
     * @param guild is the guild where the music is playing.
     * @return Boolean of the current repeating state.
     * @see TrackScheduler
     */
    public static boolean isRepeating(Guild guild) {
        return getMusicManager(guild).trackScheduler.isRepeating();
    }

    /**
     * Clears the current track queue.
     *
     * @param guild is the guild where the music is playing.
     * @see TrackScheduler
     */
    public static void stop(Guild guild) {
        getMusicManager(guild).trackScheduler.stop();
    }

    /**
     * Returns the currently playing track or null if no track is playing.
     *
     * @return {@link AudioTrack} or null
     * @see TrackScheduler
     */
    @Nullable
    public static AudioTrack getCurrentTrack(Guild guild) {
        return getMusicManager(guild).trackScheduler.getCurrentTrack();
    }
}
