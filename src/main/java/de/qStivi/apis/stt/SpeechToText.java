package de.qStivi.apis.stt;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SpeechToText {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpeechToText.class);

    private final ClientStream<StreamingRecognizeRequest> clientStream;
    private final ResponseObserver<StreamingRecognizeResponse> responseObserver;

    public SpeechToText() {
        try (SpeechClient client = SpeechClient.create()) {
            this.responseObserver = new QResponseObserver();
            this.clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setLanguageCode("en-US")
                .setSampleRateHertz(48000)
                .build();
        StreamingRecognitionConfig streamingRecognitionConfig = StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build();
        StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(streamingRecognitionConfig)
                .build();
        clientStream.send(request);

    }

    public void closeConnection()  {
        responseObserver.onComplete();
    }

    public void sendRequest(byte[] data) {
        var request =
                StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(data))
                        .build();
        clientStream.send(request);
    }
}
