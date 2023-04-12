package de.qStivi.textToSpeech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class TextToSpeech {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final ConcurrentLinkedQueue<String> textQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<byte[]> audioQueue = new ConcurrentLinkedQueue<>();


    public boolean hasText() {
        return !textQueue.isEmpty();
    }

    public void addText(String text) {
        textQueue.offer(text);
    }

    public String getText() {
        return textQueue.poll();
    }

    public boolean hasAudio() {
        return !audioQueue.isEmpty();
    }

    public byte[] getAudio() {
        return audioQueue.poll();
    }

    public void addAudio(byte[] audio) {
        audioQueue.offer(audio);
    }

    public static byte[] convertLinear16AudioToDiscordAudio(byte[] audioBytes) {
        // Each sample in LINEAR16 format is 2 bytes
        int numSamples = audioBytes.length / 2;

        // Output byte array for PCM data, 4 bytes per sample (2 bytes per channel)
        byte[] pcmBytes = new byte[numSamples * 4];

        // Iterate over each sample in the input audio data
        for (int i = 0; i < numSamples; i++) {
            // Extract the 16-bit sample from the input audio data (little-endian)
            short sample = ByteBuffer.wrap(audioBytes, i * 2, 2)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getShort();

            // Convert the sample to big-endian format and store it in the output PCM data
            ByteBuffer.wrap(pcmBytes, i * 4, 4)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putInt((sample << 16) | (sample & 0xFFFF));
        }

        return pcmBytes;
    }

    public static List<byte[]> splitAudioInto20msChunks(byte[] audioBytes, int sampleRate) {
        int chunkSize = (int) ((sampleRate * 2 * 2 * 20) / 1000.0); // 2 bytes per sample, 2 channels, 20ms duration
        int numChunks = (audioBytes.length + chunkSize - 1) / chunkSize; // Round up division

        List<byte[]> audioChunks = new ArrayList<>();
        int offset = 0;
        for (int i = 0; i < numChunks; i++) {
            int chunkLength = Math.min(chunkSize, audioBytes.length - offset);
            byte[] chunk = new byte[chunkLength];
            System.arraycopy(audioBytes, offset, chunk, 0, chunkLength);
            audioChunks.add(chunk);
            offset += chunkLength;
        }

        return audioChunks;
    }
}
