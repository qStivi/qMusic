package de.qStivi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.qStivi.Main;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class LavaPlayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LavaPlayer.class);

    private static final Map<Guild, AudioPlayer> AUDIO_PLAYER_MAP = new HashMap<>();
    private static final DefaultAudioPlayerManager PLAYER_MANAGER = new DefaultAudioPlayerManager();
    private static final Map<Guild, AudioLoadHandler> AUDIO_LOAD_HANDLER_MAP = new HashMap<>();

    static {
        AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);

//        var config = PLAYER_MANAGER.getConfiguration();
//        config.setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
//        config.setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
//        PLAYER_MANAGER.setFrameBufferDuration(5 * 1000);
        PLAYER_MANAGER.setTrackStuckThreshold(10 * 1000);
//        PLAYER_MANAGER.setPlayerCleanupThreshold(30 * 1000);
        PLAYER_MANAGER.enableGcMonitoring();

        Main.JDA.getGuilds().forEach(guild -> {
            var player = PLAYER_MANAGER.createPlayer();
            AUDIO_PLAYER_MAP.put(guild, player);

            guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));

            AUDIO_LOAD_HANDLER_MAP.put(guild, new AudioLoadHandler(player, guild));

//            AUDIO_PLAYER_MAP.forEach((s, audioPlayer) -> TRACK_SCHEDULER_MAP.put(guild, audioLoadHandler.getTrackScheduler()));
            AUDIO_PLAYER_MAP.forEach((audioPlayerGuild, audioPlayer) -> audioPlayer.addListener(AUDIO_LOAD_HANDLER_MAP.get(audioPlayerGuild).getTrackScheduler()));
        });


    }

    public static void play(String identifier, Guild guild) {
        PLAYER_MANAGER.loadItem(identifier, AUDIO_LOAD_HANDLER_MAP.get(guild));
    }

    public static void playOrdered(String identifier, Guild guild) {
        var audioLoadHandler = AUDIO_LOAD_HANDLER_MAP.get(guild);
        PLAYER_MANAGER.loadItemOrdered(guild, identifier, audioLoadHandler);
        audioLoadHandler.setLoading(true);
    }

    public static void resume(Guild guild) {
        AUDIO_PLAYER_MAP.get(guild).setPaused(false);
    }

    public static boolean isPlaying(Guild guild) {
        return AUDIO_PLAYER_MAP.get(guild).getPlayingTrack() != null;
    }

    public static void pause(Guild guild) {
        AUDIO_PLAYER_MAP.get(guild).setPaused(true);
    }

    public static Queue<AudioTrack> getQueue(Guild guild) {
        return AUDIO_LOAD_HANDLER_MAP.get(guild).getTrackScheduler().getQueue();
    }

    public static boolean toggleRepeat(Guild guild) {
        return AUDIO_LOAD_HANDLER_MAP.get(guild).getTrackScheduler().toggleRepeat();
    }

    public static void skip(Guild guild) {
        AUDIO_LOAD_HANDLER_MAP.get(guild).getTrackScheduler().skip();
    }

    public static void stop(Guild guild) {
        AUDIO_LOAD_HANDLER_MAP.get(guild).getTrackScheduler().stop();
    }

    public static boolean isRepeating(Guild guild) {
        return AUDIO_LOAD_HANDLER_MAP.get(guild).getTrackScheduler().isRepeating();
    }

    public static AudioTrack getPlayingTrack(Guild guild) {
        return AUDIO_PLAYER_MAP.get(guild).getPlayingTrack();
    }

    public static boolean trackIsLoading(Guild guild) {
        return AUDIO_LOAD_HANDLER_MAP.get(guild).isLoading();
    }

    public static boolean loadFailed(Guild guild) {
        return AUDIO_LOAD_HANDLER_MAP.get(guild).loadFailed();
    }
}
