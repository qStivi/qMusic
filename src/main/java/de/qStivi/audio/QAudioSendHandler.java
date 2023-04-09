package de.qStivi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;


public class QAudioSendHandler implements AudioSendHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AudioSendHandler.class);
    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;


    public QAudioSendHandler(AudioPlayer audioPlayer) {
        LOGGER.info("new AudioPlayerSendHandler");
        this.audioPlayer = audioPlayer;
    }

    @Override
    public boolean canProvide() {
//        LOGGER.info("canProvide");
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
//        LOGGER.info("provide20MsAudio");
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
//        LOGGER.info("isOpus");
        return true;
    }
}
