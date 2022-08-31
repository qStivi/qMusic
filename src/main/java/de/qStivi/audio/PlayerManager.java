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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    private PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.audioPlayerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        this.audioPlayerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

        logger.info("new PlayerManager()");
    }

    public static PlayerManager getINSTANCE() {

        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }

        return INSTANCE;
    }

    private GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            var guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getAudioSendHandler());

            return guildMusicManager;
        });
    }

    // TODO test this with playlists
    public void loadAndPlay(Guild guild, String trackURL) {
        var musicManager = this.getMusicManager(guild);

        this.audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
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
                logger.error(Arrays.deepToString(e.getStackTrace()));
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
    public void skip(Guild guild) {
        this.getMusicManager(guild).trackScheduler.skip();
    }

    /**
     * Pauses the currently playing track.
     *
     * @param guild is the guild where the music should be paused.
     * @see TrackScheduler
     */
    public void pause(Guild guild) {
        this.getMusicManager(guild).trackScheduler.pause();
    }

    /**
     * Continues to play a track if one is paused.
     *
     * @param guild is the guild where the music should be continued.
     * @see TrackScheduler
     */
    public void unpause(Guild guild) {
        this.getMusicManager(guild).trackScheduler.unpause();
    }

    /**
     * Sets the value of isRepeating
     *
     * @param guild  is the guild where the music should be repeated.
     * @param repeat is the new value.
     * @see TrackScheduler
     */
    public void setRepeat(Guild guild, boolean repeat) {
        this.getMusicManager(guild).trackScheduler.setRepeating(repeat);
    }

    /**
     * Returns weather the current track is being repeated or not.
     *
     * @param guild is the guild where the music is playing.
     * @return Boolean of the current repeating state.
     * @see TrackScheduler
     */
    public boolean isRepeating(Guild guild) {
        return this.getMusicManager(guild).trackScheduler.isRepeating();
    }

    /**
     * Clears the current track queue.
     *
     * @param guild is the guild where the music is playing.
     * @see TrackScheduler
     */
    public void stop(Guild guild) {
        this.getMusicManager(guild).trackScheduler.stop();
    }

    /**
     * Returns the currently playing track or null if no track is playing.
     *
     * @return {@link AudioTrack} or null
     * @see TrackScheduler
     */
    @Nullable
    public AudioTrack getCurrentTrack(Guild guild) {
        return this.getMusicManager(guild).trackScheduler.getCurrentTrack();
    }
}
