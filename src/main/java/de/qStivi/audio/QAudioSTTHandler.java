package de.qStivi.audio;

import de.qStivi.apis.stt.SpeechToText;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.OpusPacket;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QAudioSTTHandler implements AudioReceiveHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(QAudioEchoHandler.class);
    private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();
    private final SpeechToText speechToText;

    public QAudioSTTHandler() {
        this.speechToText = new SpeechToText();
    }

    public Queue<byte[]> getQueue() {
        return queue;
    }

    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    @Override
    public boolean canReceiveUser() {
        return false;
    }

    @Override
    public boolean canReceiveEncoded() {
        return false;
    }

    @Override
    public void handleEncodedAudio(@NotNull OpusPacket packet) {
        handleAudio(packet.getOpusAudio());
    }

    @Override
    public void handleCombinedAudio(@NotNull CombinedAudio combinedAudio) {
        handleAudio(combinedAudio.getAudioData(1));
    }

    @Override
    public void handleUserAudio(@NotNull UserAudio userAudio) {
        handleAudio(userAudio.getAudioData(1));
    }

    @Override
    public boolean includeUserInCombinedAudio(@NotNull User user) {
        return !user.isBot();
    }

    private void handleAudio(byte[] byteArray) {
//        if (isZeroArray(byteArray)) {
//            return;
//        }
//        queue.offer(byteArray);
        speechToText.sendRequest(byteArray);
//        LOGGER.debug("handleAudio() - " + Arrays.toString(byteArray));
    }

    private boolean isZeroArray(byte[] array) {
        for (byte b : array) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }
}
