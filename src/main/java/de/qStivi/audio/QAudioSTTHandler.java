package de.qStivi.audio;

import de.qStivi.apis.stt.SpeechToText;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.OpusPacket;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QAudioSTTHandler implements AudioReceiveHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(QAudioEchoHandler.class);
    private final SpeechToText speechToText;
    private final AudioManager audioManager;

    public QAudioSTTHandler(AudioManager audioManager) {
        this.audioManager = audioManager;
        this.speechToText = new SpeechToText();
    }

    public SpeechToText getSpeechToText() {
        return speechToText;
    }

    @Override
    public boolean canReceiveCombined() {
        return audioManager.isConnected();
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
        var bigEndianAudioStereo = combinedAudio.getAudioData(1);
        var littleEndianAudioMono = convertAudioData(bigEndianAudioStereo);
        speechToText.sendRequest(littleEndianAudioMono);
    }

    @Override
    public void handleUserAudio(@NotNull UserAudio userAudio) {
        handleAudio(userAudio.getAudioData(1));
    }

    private byte[] convertAudioData(byte[] audioBytes) {
        byte[] monoLittleEndianBytes = new byte[audioBytes.length / 2]; // Mono data will be half the size of stereo data

        for (int i = 0; i < audioBytes.length; i += 4) {
            // Extract left channel sample (big-endian) from stereo data
            int leftSample = ((audioBytes[i] & 0xFF) << 8) | (audioBytes[i + 1] & 0xFF);

            // Extract right channel sample (big-endian) from stereo data
            int rightSample = ((audioBytes[i + 2] & 0xFF) << 8) | (audioBytes[i + 3] & 0xFF);

            // Average left and right channel samples to convert to mono
            int monoSample = (leftSample + rightSample) / 2;

            // Convert mono sample to little-endian byte representation
            byte lowByte = (byte) (monoSample & 0xFF);
            byte highByte = (byte) ((monoSample >> 8) & 0xFF);

            // Store little-endian mono sample in output byte array
            monoLittleEndianBytes[i / 2] = lowByte;
            monoLittleEndianBytes[(i / 2) + 1] = highByte;
        }

        return monoLittleEndianBytes;
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
