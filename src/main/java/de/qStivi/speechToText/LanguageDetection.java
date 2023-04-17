package de.qStivi.speechToText;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;
import com.microsoft.cognitiveservices.speech.audio.PushAudioInputStream;
import de.qStivi.Properties;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class LanguageDetection extends SpeechToText {

    private final PushAudioInputStream audio;

    public LanguageDetection() {
        this.audio = AudioInputStream.createPushStream();
        this.thread = new Thread(() -> {
            while (true) {
                if (hasAudio()) {
                    var audio = getAudio();

                    this.audio.write(convertAudioFormat(audio));
                }
            }
        });

        // Continuous language detection with speech recognition requires the application to set a V2 endpoint URL.
        // Replace the service (Azure) region with your own service region (e.g. "westus").
        String v2EndpointUrl = "wss://" + Properties.TTS_MICROSOFT_API_REGION + ".stt.speech.microsoft.com/speech/universal/v2";

        // Creates an instance of a speech config with specified endpoint URL and subscription key. Replace with your own subscription key.
        SpeechConfig speechConfig = SpeechConfig.fromEndpoint(URI.create(v2EndpointUrl), Properties.TTS_MICROSOFT_API_KEY);

        // Change the default from at-start language detection to continuous language detection, since the spoken language in the audio
        // may change.
        speechConfig.setProperty(PropertyId.SpeechServiceConnection_LanguageIdMode, "Continuous");

        // Define a set of expected spoken languages in the audio, with an optional custom model endpoint ID associated with each.
        // Update the below with your own languages. Please see https://docs.microsoft.com/azure/cognitive-services/speech-service/language-support
        // for all supported languages.
        // Update the below with your own custom model endpoint IDs, or omit it if you want to use the standard model.
        List<SourceLanguageConfig> sourceLanguageConfigs = new ArrayList<SourceLanguageConfig>();
        sourceLanguageConfigs.add(SourceLanguageConfig.fromLanguage("en-US"));
        sourceLanguageConfigs.add(SourceLanguageConfig.fromLanguage("de-DE"));
        sourceLanguageConfigs.add(SourceLanguageConfig.fromLanguage("pt-BR"));

        // Creates an instance of AutoDetectSourceLanguageConfig with the above 3 source language configurations.
        AutoDetectSourceLanguageConfig autoDetectSourceLanguageConfig = AutoDetectSourceLanguageConfig.fromSourceLanguageConfigs(sourceLanguageConfigs);

        AudioConfig audioConfig = AudioConfig.fromStreamInput(this.audio);

        SpeechRecognizer speechRecognizer = new SpeechRecognizer(speechConfig, autoDetectSourceLanguageConfig, audioConfig);


        // Subscribes to events.

        // Uncomment this to see intermediate recognition results. Since this is verbose and the WAV file is long, it is commented out by default in this sample.
        speechRecognizer.recognizing.addEventListener((s, e) -> {
            AutoDetectSourceLanguageResult autoDetectSourceLanguageResult = AutoDetectSourceLanguageResult.fromResult(e.getResult());
            String language = autoDetectSourceLanguageResult.getLanguage();
            System.out.println(" RECOGNIZING: Text = " + e.getResult().getText());
            System.out.println(" RECOGNIZING: Language = " + language);
        });

        speechRecognizer.recognized.addEventListener((s, e) -> {
            AutoDetectSourceLanguageResult autoDetectSourceLanguageResult = AutoDetectSourceLanguageResult.fromResult(e.getResult());
            String language = autoDetectSourceLanguageResult.getLanguage();
            if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                addResponse(e.getResult().getText());
                System.out.println(" RECOGNIZED: Text = " + e.getResult().getText());
                System.out.println(" RECOGNIZED: Language = " + language);
            } else if (e.getResult().getReason() == ResultReason.NoMatch) {
                if (language == null || language.isEmpty() || language.equalsIgnoreCase("unknown")) {
                    System.out.println(" NOMATCH: Speech Language could not be detected.");
                } else {
                    System.out.println(" NOMATCH: Speech could not be recognized.");
                }
            }
        });

        speechRecognizer.canceled.addEventListener((s, e) -> {
            System.out.println(" CANCELED: Reason = " + e.getReason());
            if (e.getReason() == CancellationReason.Error) {
                System.out.println(" CANCELED: ErrorCode = " + e.getErrorCode());
                System.out.println(" CANCELED: ErrorDetails = " + e.getErrorDetails());
                System.out.println(" CANCELED: Did you update the subscription info?");
            }
        });

        speechRecognizer.sessionStarted.addEventListener((s, e) -> {
            System.out.println("\n Session started event.");
        });


        speechRecognizer.startContinuousRecognitionAsync();
    }

    public static byte[] convertAudioFormat(byte[] audioData) {
        // Convert byte array to short array
        short[] shortArray = new short[audioData.length / 2];
        ByteBuffer.wrap(audioData).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shortArray);

        // Resample from 48kHz to 16kHz
        int sourceSampleRate = 48000;
        int targetSampleRate = 16000;
        int ratio = sourceSampleRate / targetSampleRate;
        int targetLength = shortArray.length / ratio;
        short[] resampledArray = new short[targetLength];

        for (int i = 0; i < targetLength; i++) {
            int sourceIndex = i * ratio;
            resampledArray[i] = shortArray[sourceIndex];
        }

        // Convert short array back to byte array
        ByteBuffer byteBuffer = ByteBuffer.allocate(resampledArray.length * 2);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        for (short value : resampledArray) {
            byteBuffer.putShort(value);
        }
        byte[] convertedAudioData = byteBuffer.array();

        return convertedAudioData;
    }
}
