package de.qStivi.apis.stt;

import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class QResponseObserver implements ResponseObserver<StreamingRecognizeResponse> {
    private final Logger LOGGER = LoggerFactory.getLogger(QResponseObserver.class);
    ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();
    @Override
    public void onStart(StreamController controller) {
        LOGGER.info("new ResponseObserver started...");
    }

    @Override
    public void onResponse(StreamingRecognizeResponse response) {
        LOGGER.info("new response received...");
        responses.add(response);
        response.getResultsList().forEach(result -> {
            result.getAlternativesList().forEach(alternative -> {
                LOGGER.info("Transcript : " + alternative.getTranscript());
            });
        });
    }

    @Override
    public void onError(Throwable t) {
        LOGGER.error(t.toString());
    }

    @Override
    public void onComplete() {
        for (StreamingRecognizeResponse response : responses) {
            StreamingRecognitionResult result = response.getResultsList().get(0);
            SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
            LOGGER.info("Transcript : " + alternative.getTranscript());
        }
    }
}
