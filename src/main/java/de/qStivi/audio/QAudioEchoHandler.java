package de.qStivi.audio;

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class QAudioEchoHandler implements AudioReceiveHandler, AudioSendHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AudioSendHandler.class);
    private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

    public QAudioEchoHandler() {

        LOGGER.info("new AudioPlayerSendHandler");
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
        handleAudio(packet.getAudioData(1));
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
        if (isZeroArray(byteArray)) {
            return;
        }
        queue.offer(byteArray);
        LOGGER.debug("handleAudio() - " + Arrays.toString(byteArray));
    }

    private boolean isZeroArray(byte[] array) {
        for (byte b : array) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canProvide() {
        boolean isQueueEmpty = queue.isEmpty();
        LOGGER.debug("canProvide() - " + Arrays.toString(queue.peek()));
        LOGGER.debug("canProvide() - " + !isQueueEmpty);
        return !isQueueEmpty;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(queue.poll());
        LOGGER.debug("provide20MsAudio() - " + queue.size());
        LOGGER.debug("provide20MsAudio() - " + Arrays.toString(byteBuffer.array()));
        return byteBuffer;
    }

    @Override
    public boolean isOpus() {
        return false;
    }
}
