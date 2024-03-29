package de.qStivi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

// TODO Logging
public class QAudioEventAdapter extends AudioEventAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AudioEventAdapter.class);
    private final AudioPlayer player;
    private final Queue<AudioTrack> queue = new LinkedBlockingQueue<>();
    private boolean isRepeating = false;
    private volatile Message message;

    public QAudioEventAdapter(AudioPlayer player) {
        super();
        this.player = player;
        LOGGER.info("TrackScheduler() - New TrackScheduler initialized");
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        super.onPlayerPause(player);
        LOGGER.info("onPlayerPause()");
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        super.onPlayerResume(player);
        LOGGER.info("onPlayerResume()");
    }


    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
//        super.onTrackStart(player, track);
        LOGGER.info("onTrackStart() - Track: " + track.getInfo().title + " (" + track.getIdentifier() + ")");
        updateTrackInfo(track);
    }


    public void updateTrackInfo(AudioTrack track) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (track == null) {
            track = player.getPlayingTrack();
        }
        var sb = new StringBuilder();
        if (isRepeating) {
            sb.append("Repeating: ");
        } else {
            sb.append("Playing: ");
        }
        sb.append(track.getInfo().author)
                .append(" - ")
                .append(track.getInfo().title)
                .append(" (")
                .append(track.getIdentifier())
                .append(")\n")
                .append("https://youtu.be/")
                .append(track.getIdentifier());

        message.editMessage(sb.toString()).queue();
        if (message.getComponents().isEmpty()) message.editMessageComponents(ActionRow.of(Button.primary("play", Emoji.fromFormatted("<:play:929131671004012584>")), Button.primary("pause", Emoji.fromFormatted("<:pause:929131670957854721>")), Button.primary("stop", Emoji.fromFormatted("<:stop:929130911382007848>")), Button.primary("skip", Emoji.fromFormatted("<:skip:929131670660067370>")), Button.primary("repeat", Emoji.fromFormatted("<:repeat:929131670941089864>")))).queue();
    }
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        super.onTrackEnd(player, track, endReason);
        LOGGER.info("onTrackEnd() - Track: " + track.getInfo().title + " (" + track.getIdentifier() + ") - End Reason: " + endReason.toString());

        if (endReason.mayStartNext) {
            if (isRepeating) {
                player.playTrack(track.makeClone());
//                player.playTrack(queue.poll());
            } else {
                player.playTrack(queue.poll());
            }
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        super.onTrackException(player, track, exception);
        LOGGER.info("onTrackException() - Track: " + track.getInfo().title + " (" + track.getIdentifier() + ") - FriendlyException: " + exception.getMessage());
        // An already playing track threw an exception (track end event will still be received separately)
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        super.onTrackStuck(player, track, thresholdMs);
        LOGGER.info("onTrackStuck() - Track: " + track.getInfo().title + " (" + track.getIdentifier() + ") - thresholdMs: " + thresholdMs);
        // Audio track has been unable to provide us any audio, might want to just start a new track
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
        super.onTrackStuck(player, track, thresholdMs, stackTrace);
        LOGGER.info("onTrackStuck() - Track: " + track.getInfo().title + " (" + track.getIdentifier() + ") - thresholdMs: " + thresholdMs + " stackTrace: " + Arrays.deepToString(stackTrace));
        // Audio track has been unable to provide us any audio, might want to just start a new track
    }

    void queue(AudioTrack track) {
        var trackInfo = track.getInfo().title + " (" + track.getIdentifier() + ")";
        if (player.getPlayingTrack() != null) {
//            LOGGER.info("queue() - Queueing track: " + trackInfo);
            if (queue.offer(track)) {
//                LOGGER.info("queue() - Successfully queued track: " + trackInfo);
            } else {
//                LOGGER.error("queue() - Error while queueing track: " + trackInfo);
            }
        } else {
//            LOGGER.info("queue() - Playing track: " + trackInfo);
            player.playTrack(track);
        }
//        queue.forEach(audioTrack -> LOGGER.info(audioTrack.getIdentifier()));
    }

    Queue<AudioTrack> getQueue() {
        return new LinkedBlockingQueue<>(queue);
    }

    boolean toggleRepeat() {
        isRepeating = !isRepeating;
        updateTrackInfo(player.getPlayingTrack());
        return isRepeating;
    }

    void skip() {
        player.playTrack(queue.poll());
    }

    void stop() {
        queue.clear();
        skip();
    }

    boolean isRepeating() {
        return isRepeating;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
