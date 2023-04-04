package de.qStivi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.qStivi.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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

    private static InteractionHook INTERACTION_HOOK = null;

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

    public static void play(String identifier, Guild guild, boolean random) {
        PLAYER_MANAGER.loadItem(identifier, AUDIO_LOAD_HANDLER_MAP.get(guild));
        var audioLoadHandler = AUDIO_LOAD_HANDLER_MAP.get(guild);
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

    public static void skip(GenericInteractionCreateEvent event) {
        AUDIO_LOAD_HANDLER_MAP.get(event.getGuild()).getTrackScheduler().skip();
        var member = event.getMember();
        if (member == null) {
            throw new NullPointerException("Member was null!");
        }
        LOGGER.info("Track skipped by " + member.getEffectiveName());
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

    public static void openAudioConnection(GenericCommandInteractionEvent event) {
        var guild = event.getGuild();
        if (guild == null) {
            event.getHook().editOriginal("Something ent wrong!").queue();
            return;
        }
        var member = event.getMember();
        if (member == null) {
            event.getHook().editOriginal("Something ent wrong!").queue();
            return;
        }
        var voiceState = member.getVoiceState();
        if (voiceState == null) {
            event.getHook().editOriginal("Something ent wrong!").queue();
            return;
        }
        guild.getAudioManager().openAudioConnection(voiceState.getChannel());
        INTERACTION_HOOK = event.getHook();
    }

    public static void updateTrackInfo() {
        var track = getPlayingTrack(INTERACTION_HOOK.getInteraction().getGuild());
        INTERACTION_HOOK.editOriginal("Playing: " + track.getInfo().author + " - " + track.getInfo().title + " (" + track.getIdentifier() + ")\n" + "https://youtu.be/" + track.getIdentifier()).queue();
        INTERACTION_HOOK.editOriginalComponents(ActionRow.of(Button.primary("play", Emoji.fromFormatted("<:play:929131671004012584>")), Button.primary("pause", Emoji.fromFormatted("<:pause:929131670957854721>")), Button.primary("stop", Emoji.fromFormatted("<:stop:929130911382007848>")), Button.primary("skip", Emoji.fromFormatted("<:skip:929131670660067370>")), Button.primary("repeat", Emoji.fromFormatted("<:repeat:929131670941089864>")))).queue();
    }
}
