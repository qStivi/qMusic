package de.qStivi.apis.stt;

import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QResponseObserver implements ResponseObserver<StreamingRecognizeResponse> {
    private final Logger LOGGER = LoggerFactory.getLogger(QResponseObserver.class);

    public Queue<String> getQueue() {
        return queue;
    }

    public final Queue<String> queue = new ConcurrentLinkedQueue<>();
    @Override
    public void onStart(StreamController controller) {
        LOGGER.info("new ResponseObserver started...");
    }

    @Override
    public void onResponse(StreamingRecognizeResponse response) {
        LOGGER.info("new response received...");
        response.getResultsList().forEach(result -> {
            result.getAlternativesList().forEach(alternative -> {
                LOGGER.info("Transcript : " + alternative.getTranscript());
            });
        });
        var yee = response.getResultsList().get(0).getAlternativesList().get(0).getTranscript();
        queue.offer(yee);
    }

    @Override
    public void onError(Throwable t) {
        LOGGER.error(t.toString());
    }

    @Override
    public void onComplete() {
//        for (StreamingRecognizeResponse response : responses) {
//            StreamingRecognitionResult result = response.getResultsList().get(0);
//            SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
//            LOGGER.info("Transcript : " + alternative.getTranscript());
//        }
    }
}
