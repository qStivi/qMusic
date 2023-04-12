package de.qStivi.textToSpeech;

import com.google.cloud.texttospeech.v1.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GoogleTextToSpeech extends TextToSpeech {

    private final TextToSpeechClient client;
    private final VoiceSelectionParams voice;
    private final AudioConfig audioConfig;
    private final Thread thread;

    public GoogleTextToSpeech() {
        this.voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("en-US")
                .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                .build();

        this.audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.LINEAR16)
                .setSampleRateHertz(48000)
                .build();

        try {
            this.client = TextToSpeechClient.create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.thread = new Thread(() -> {
            while (true) {
                if (hasText()) {
                    var text = getText();
                    var input = SynthesisInput.newBuilder().setText(text).build();
                    var response = client.synthesizeSpeech(input, voice, audioConfig);
                    var audioContents = response.getAudioContent().toByteArray();
                    var converted = convertLinear16AudioToDiscordAudio(audioContents);
                    var audioList = splitAudioInto20msChunks(converted, 48000);
                    for (byte[] bytes : audioList) {
                        addAudio(bytes);
                    }
                }
            }
        });
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        thread.interrupt();
    }
}
