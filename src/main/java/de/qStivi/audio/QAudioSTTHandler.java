package de.qStivi.audio;

import com.google.cloud.texttospeech.v1.*;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import de.qStivi.apis.stt.SpeechToText;
import net.dv8tion.jda.api.audio.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class QAudioSTTHandler implements AudioReceiveHandler, AudioSendHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(QAudioEchoHandler.class);
    public final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();
    private final SpeechToText speechToText;
    private final AudioManager audioManager;

    private final QPlayer player;

    public QAudioSTTHandler(AudioManager audioManager, QPlayer instance) {
        this.player = instance;
        this.audioManager = audioManager;
        this.speechToText = new SpeechToText();
        OpenAiService service = new OpenAiService("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");
        var list = new ArrayList<ChatMessage>();
        list.add(new ChatMessage("system", "You are a friendly assistant for discord users."));
        new Thread(() -> {
            while (true) {
                if (!this.speechToText.responseObserver.queue.isEmpty()) {
                    var response = this.speechToText.responseObserver.queue.poll();
                    list.add(new ChatMessage("user", response));
                    ChatCompletionRequest request = ChatCompletionRequest.builder()
                            .model("gpt-3.5-turbo")
                            .messages(list)
                            .user("qBot")
                            .maxTokens(256)
                            .build();
                    var message = service.createChatCompletion(request).getChoices().get(0).getMessage();
                    var choice = message.getContent();
                    LOGGER.info("Choice: " + choice);


                    // Instantiates a client
                    try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
                        // Set the text input to be synthesized
                        SynthesisInput input = SynthesisInput.newBuilder().setText(choice).build();

                        // Build the voice request, select the language code ("en-US") and the ssml voice gender
                        // ("neutral")
                        VoiceSelectionParams voice =
                                VoiceSelectionParams.newBuilder()
                                        .setLanguageCode("en-US")
                                        .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                                        .build();

                        // Select the type of audio file you want returned
                        AudioConfig audioConfig =
                                AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16).setSampleRateHertz(48000).build();

                        // Perform the text-to-speech request on the text input with the selected voice parameters and
                        // audio file type
                        SynthesizeSpeechResponse ttsResponse =
                                textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

                        // Get the audio contents from the response
                        var audioContents = ttsResponse.getAudioContent().toByteArray();

                        var converted = convertLinear16ToBigEndianPCM(audioContents);

                        var audioList = splitAudioIntoChunks(converted, 48000);

//                        LOGGER.info("Audio contents: {}", list.size());

                        for (byte[] bytes : audioList) {
                            queue.offer(bytes);
                        }

//                        var audioContents = ttsResponse.getAudioContent();
//                        try (OutputStream out = new FileOutputStream("output.mp3")) {
//                            out.write(audioContents.toByteArray());
//                            System.out.println("Audio content written to file \"output.mp3\"");
//                        }
//
//                        player.play("output.mp3");

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    public static byte[] convertLinear16ToBigEndianPCM(byte[] audioBytes) {
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
                    .putInt((sample << 16) | ((short) sample & 0xFFFF));
        }

        return pcmBytes;
    }

    public SpeechToText getSpeechToText() {
        return speechToText;
    }

    @Override
    public boolean canReceiveCombined() {
        return audioManager.isConnected();
    }

    public List<byte[]> splitAudioIntoChunks(byte[] audioBytes, int sampleRate) {
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
        var littleEndianAudioMono = convertAudioDataDiscordToGoogle(bigEndianAudioStereo);
        speechToText.sendRequest(littleEndianAudioMono);
    }

    @Override
    public void handleUserAudio(@NotNull UserAudio userAudio) {
        handleAudio(userAudio.getAudioData(1));
    }

    private byte[] convertAudioDataDiscordToGoogle(byte[] audioBytes) {
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

    public byte[] convertAudioDataGoogleTotDiscord(byte[] audioBytes) {
        byte[] stereoBigEndianBytes = new byte[audioBytes.length * 2]; // Stereo data will be twice the size of mono data

        for (int i = 0; i < audioBytes.length; i += 2) {
            // Extract mono sample (little-endian) from input data
            int monoSample = ((audioBytes[i + 1] & 0xFF) << 8) | (audioBytes[i] & 0xFF);

            // Duplicate mono sample for both left and right channel
            byte lowByte = (byte) (monoSample & 0xFF);
            byte highByte = (byte) ((monoSample >> 8) & 0xFF);

            // Store left and right channel samples as big-endian in output byte array
            stereoBigEndianBytes[i * 2] = lowByte;
            stereoBigEndianBytes[(i * 2) + 1] = highByte;
            stereoBigEndianBytes[(i * 2) + 2] = lowByte;
            stereoBigEndianBytes[(i * 2) + 3] = highByte;
        }

        return stereoBigEndianBytes;
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

    @Override
    public boolean canProvide() {
        boolean isQueueEmpty = queue.isEmpty();
        return !isQueueEmpty;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(queue.poll());
        return byteBuffer;
    }

    @Override
    public boolean isOpus() {
        return false;
    }
}
