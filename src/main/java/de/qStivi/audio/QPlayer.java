package de.qStivi.audio;

import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.qStivi.NoResultsException;
import de.qStivi.apis.YouTubeAPI;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class QPlayer {

    private final Logger LOGGER = LoggerFactory.getLogger(QPlayer.class);

    private final QAudioPlayerManager playerManager;
    private final AudioPlayer audioPlayerManager;
    private final QAudioLoadHandler QAudioLoadHandler;

    private final QAudioEventAdapter QAudioEventAdapter;

    private static final Map<Guild, QPlayer> INSTANCE_MAP = new HashMap<>();

    public static QPlayer getInstance(Guild guild) {
        if (INSTANCE_MAP.containsKey(guild)) {
            return INSTANCE_MAP.get(guild);
        } else {
            var instance = new QPlayer();
            INSTANCE_MAP.put(guild, instance);
            return instance;
        }
    }

    private QPlayer() {
        this.playerManager = new QAudioPlayerManager();
        this.audioPlayerManager = playerManager.createPlayer();
        this.QAudioEventAdapter = new QAudioEventAdapter(audioPlayerManager);
        this.QAudioLoadHandler = new QAudioLoadHandler(this.QAudioEventAdapter);

        this.audioPlayerManager.addListener(this.QAudioEventAdapter);

        AudioSourceManagers.registerRemoteSources(playerManager);
        playerManager.setTrackStuckThreshold(10 * 1000);
        playerManager.enableGcMonitoring();
    }

    public void play(String identifier) {
        playerManager.loadItem(identifier, QAudioLoadHandler);
    }

    public void resume() {
        audioPlayerManager.setPaused(false);
    }

    public boolean isPlaying() {
        return audioPlayerManager.getPlayingTrack() != null;
    }

    public void pause() {
        audioPlayerManager.setPaused(true);
    }

    public Queue<AudioTrack> getQueue() {
        return QAudioLoadHandler.getAudioEventHandler().getQueue();
    }

    public boolean toggleRepeat() {
        return QAudioLoadHandler.getAudioEventHandler().toggleRepeat();
    }

    public void skip(GenericInteractionCreateEvent event) {
        QAudioLoadHandler.getAudioEventHandler().skip();
        var member = event.getMember();
        if (member == null) {
            throw new NullPointerException("Member was null!");
        }
        LOGGER.info("Track skipped by " + member.getEffectiveName());
    }

    public void stop() {
        QAudioLoadHandler.getAudioEventHandler().stop();
    }

    public boolean isRepeating() {
        return QAudioLoadHandler.getAudioEventHandler().isRepeating();
    }

    public void openAudioConnection(GenericCommandInteractionEvent event) {
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
        if (!voiceState.inAudioChannel()) {
            event.getHook().editOriginal("Please join a channel first.").queue();
            return;
        }
        var audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(voiceState.getChannel());
        audioManager.setSendingHandler(new QAudioSendHandler(audioPlayerManager));
    }

    public void setMessage(Message message) {
        QAudioEventAdapter.setMessage(message);
    }

    public void playQuery(String query) {
        try {
            var identifier = YouTubeAPI.getSearchResults(query).get(0).getId().getVideoId();
            playerManager.loadItem(identifier, QAudioLoadHandler);
        } catch (IOException | NoResultsException e) {
            throw new RuntimeException(e);
        }
    }
}
