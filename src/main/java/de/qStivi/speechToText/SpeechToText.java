package de.qStivi.speechToText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class SpeechToText {


    public Thread thread;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentLinkedQueue<String> responseQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<byte[]> audioQueue = new ConcurrentLinkedQueue<>();

    public static byte[] convertDiscordAudioToLinear16Audio(byte[] audioBytes) {
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

    public String getResponse() {
        return responseQueue.poll();
    }

    public boolean hasResponse() {
        return !responseQueue.isEmpty();
    }

    public void addResponse(String response) {
        LOGGER.info("Response: " + response);
        responseQueue.offer(response);
    }

    public void queueAudio(byte[] data) {
        audioQueue.offer(data);
    }

    public boolean hasAudio() {
        return !audioQueue.isEmpty();
    }

    public byte[] getAudio() {
        return audioQueue.poll();
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        thread.interrupt();
    }
}
