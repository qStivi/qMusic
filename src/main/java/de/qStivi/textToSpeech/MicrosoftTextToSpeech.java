package de.qStivi.textToSpeech;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import de.qStivi.Properties;

import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class MicrosoftTextToSpeech extends TextToSpeech {
    private final SpeechSynthesizer speechSynthesizer;

    public MicrosoftTextToSpeech() {
        var speechConfig = SpeechConfig.fromSubscription(Properties.TTS_MICROSOFT_API_KEY, Properties.TTS_MICROSOFT_API_REGION);

        speechConfig.setSpeechSynthesisVoiceName("en-GB-SoniaNeural");
        speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Raw48Khz16BitMonoPcm);

        this.speechSynthesizer = new SpeechSynthesizer(speechConfig, null);
        this.speechSynthesizer.Synthesizing.addEventListener((o, synthesizingEventArgs) -> {
            var audio = synthesizingEventArgs.getResult().getAudioData();
            var converted = convertLinear16AudioToDiscordAudio(audio);
            var audioList = splitAudioInto20msChunks(converted, 48000);
            for (byte[] bytes : audioList) {
                addAudio(bytes);
            }
        });

        Connection connection = Connection.fromSpeechSynthesizer(this.speechSynthesizer);
        connection.openConnection(true);

        this.thread = new Thread(() -> {
            while (true) {
                if (hasText()) {
                    var text = getText();
                    this.speechSynthesizer.SpeakTextAsync(text);
                }
            }
        });
    }
}
