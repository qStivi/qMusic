package de.qStivi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

// TODO Logging
public class TrackScheduler extends AudioEventAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AudioEventAdapter.class);
    private final AudioPlayer player;
    private final Guild guild;
    private final Queue<AudioTrack> queue = new LinkedBlockingQueue<>();
    private boolean isRepeating = false;

    public TrackScheduler(AudioPlayer player, Guild guild) {
        super();
        this.player = player;
        this.guild = guild;
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
        super.onTrackStart(player, track);
        LOGGER.info("onTrackStart() - Track: " + track.getInfo().title + " (" + track.getIdentifier() + ")");
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        super.onTrackEnd(player, track, endReason);
        LOGGER.info("onTrackEnd() - Track: " + track.getInfo().title + " (" + track.getIdentifier() + ") - End Reason: " + endReason.toString());

        if (endReason.mayStartNext) {
            if (isRepeating) {
//                player.playTrack(track.makeClone());
                player.playTrack(queue.poll());
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

    @Override
    public void onEvent(AudioEvent event) {
        super.onEvent(event);
        LOGGER.info("onEvent() - Guild: " + guild.getId());
    }

    void queue(AudioTrack track) {
        var trackInfo = track.getInfo().title + " (" + track.getIdentifier() + ")";
        if (player.getPlayingTrack() != null) {
            LOGGER.info("queue() - Queueing track: " + trackInfo);
            if (queue.offer(track)) {
                LOGGER.info("queue() - Successfully queued track: " + trackInfo);
            } else {
                LOGGER.error("queue() - Error while queueing track: " + trackInfo);
            }
        } else {
            LOGGER.info("queue() - Playing track: " + trackInfo);
            player.playTrack(track);
        }
        queue.forEach(audioTrack -> LOGGER.info(audioTrack.getIdentifier()));
    }

    Queue<AudioTrack> getQueue() {
        return new LinkedBlockingQueue<>(queue);
    }

    boolean toggleRepeat() {
        isRepeating = !isRepeating;
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
}
