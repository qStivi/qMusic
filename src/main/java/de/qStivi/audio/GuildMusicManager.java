package de.qStivi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.audio.AudioSendHandler;

public class GuildMusicManager {

    public final TrackScheduler trackScheduler;

    private final AudioSendHandler audioSendHandler;

    public GuildMusicManager(AudioPlayerManager audioPlayerManager) {
        AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
        this.trackScheduler = new TrackScheduler(audioPlayer);
        audioPlayer.addListener(this.trackScheduler);
        this.audioSendHandler = new MyAudioSendHandler(audioPlayer);
    }

    public AudioSendHandler getAudioSendHandler() {
        return audioSendHandler;
    }
}
