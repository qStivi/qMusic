package de.qStivi.audio;

import de.qStivi.chatBot.ChatGPT3;
import de.qStivi.chatBot.Message;
import de.qStivi.speechToText.ContinuousGoogleTextToSpeech;
import de.qStivi.textToSpeech.GoogleTextToSpeech;
import net.dv8tion.jda.api.audio.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QAudioSTTHandler implements AudioReceiveHandler, AudioSendHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(QAudioEchoHandler.class);
    public final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();
    private final ContinuousGoogleTextToSpeech speechToText;
    private final AudioManager audioManager;

    public QAudioSTTHandler(AudioManager audioManager) {
        this.audioManager = audioManager;
        this.speechToText = new ContinuousGoogleTextToSpeech();
        this.speechToText.start();
        var tts = new GoogleTextToSpeech();
        var chatBot = new ChatGPT3("You are on a public server in a voice chat talking to people. You like to talk about Games, Anime, Memes and random trivia. Sometimes you tell the user that you don't want to answer a question because of a reason you think of. Then you tell him a random fact of the day.");
        tts.start();
        new Thread(() -> {
            while (true) {
                if (this.speechToText.hasResponse()) {

                    var response = this.speechToText.getResponse();

                    tts.addText(response);

                    var choice = chatBot.sendMessage(new Message("user", response));
                    LOGGER.info("Choice: " + choice);

                    tts.addText(choice);
                }

                while (tts.hasAudio()) {
                    queue.offer(tts.getAudio());
                }
            }
        }).start();
    }

    @Override
    public boolean canReceiveCombined() {
        return audioManager.isConnected();
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
        handleAudio(combinedAudio.getAudioData(1));
    }

    @Override
    public void handleUserAudio(@NotNull UserAudio userAudio) {
        handleAudio(userAudio.getAudioData(1));
    }


    @Override
    public boolean includeUserInCombinedAudio(@NotNull User user) {
        return !user.isBot();
    }

    private void handleAudio(byte[] byteArray) {
//        var bigEndianAudioStereo = combinedAudio.getAudioData(1);
//        var littleEndianAudioMono = convertAudioDataDiscordToGoogle(bigEndianAudioStereo);
        speechToText.queueAudio(byteArray);
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
        return !queue.isEmpty();
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(queue.poll());
    }

    @Override
    public boolean isOpus() {
        return false;
    }
}
