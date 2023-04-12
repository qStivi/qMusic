package de.qStivi.speechToText;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ContinuousGoogleTextToSpeech extends SpeechToText implements ResponseObserver<StreamingRecognizeResponse> {

    private static final int STREAMING_LIMIT = 290000; // ~5 minutes
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final Thread thread;
    private ClientStream<StreamingRecognizeRequest> clientStream;

    public ContinuousGoogleTextToSpeech() {
        reset();

        this.thread = new Thread(() -> {
            var startTime = System.currentTimeMillis();
            while (true) {
                var estimatedTime = System.currentTimeMillis() - startTime;
                if (hasAudio()) {
                    clientStream.send(
                            StreamingRecognizeRequest.newBuilder()
                                    .setAudioContent(ByteString.copyFrom(getAudio()))
                                    .build()
                    );
                }
                if (estimatedTime >= STREAMING_LIMIT) {
                    LOGGER.info("Time limit reached. Restarting stream.");
                    reset();
                }
            }
        });
    }

    @NotNull
    private static StreamingRecognizeRequest getStreamingRecognizeRequest() {
        return StreamingRecognizeRequest.newBuilder().setStreamingConfig(
                StreamingRecognitionConfig.newBuilder().setConfig(
                        RecognitionConfig.newBuilder()
                                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                                .setLanguageCode("en-US")
                                .setSampleRateHertz(48000)
                                .build()
                ).build()
        ).build();
    }

    @Override
    public void queueAudio(byte[] data) {
        super.queueAudio(convertDiscordAudioToLinear16Audio(data));
    }


    private ClientStream<StreamingRecognizeRequest> createNewClient(StreamingRecognizeRequest configRequest) {
        try (var client = SpeechClient.create()) {
            return client.streamingRecognizeCallable().splitCall(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        var configRequest = getStreamingRecognizeRequest();
        this.clientStream = createNewClient(configRequest);
        this.clientStream.send(configRequest);
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        thread.interrupt();
    }

    @Override
    public void onStart(StreamController controller) {

    }

    @Override
    public void onResponse(StreamingRecognizeResponse response) {
        var results = response.getResultsList();
        for (var result : results) {
            addResponse(result.getAlternativesList().get(0).getTranscript());
        }
    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onComplete() {

    }
}
