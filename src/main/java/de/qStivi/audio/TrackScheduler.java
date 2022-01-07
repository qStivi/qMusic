package de.qStivi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BlockingQueue<AudioTrack> queue;
    private final AudioPlayer player;
    private boolean isRepeating;

    TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        logger.info("new TrackScheduler()");
    }

    //region overrides
    //TODO try to use all these overrides they seem useful...
    @Override
    public void onPlayerPause(AudioPlayer player) {
        super.onPlayerPause(player);
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        super.onPlayerResume(player);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        super.onTrackStart(player, track);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        super.onTrackException(player, track, exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        super.onTrackStuck(player, track, thresholdMs);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
        super.onTrackStuck(player, track, thresholdMs, stackTrace);
    }

    @Override
    public void onEvent(AudioEvent event) {
        super.onEvent(event);
    }

    /**
     * Handles what happens after a track has stopped playing.
     *
     * @param player    Discord's {@link AudioPlayer}
     * @param track     {@link AudioTrack} which has stopped playing.
     * @param endReason The reason why the {@link AudioTrack} has stopped playing.
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (isRepeating) {
                logger.info("Repeat track...");
                this.player.startTrack(track.makeClone(), false);
            } else {
                logger.info("Next track...");
                this.player.startTrack(this.queue.poll(), false);
            }
        }
    }
    //endregion

    //region getter and setter
    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
    }
    //endregion

    /**
     * Starts playing an {@link AudioTrack} or adds it to the queue if it couldn't be played without interrupting another.
     *
     * @param track {@link AudioTrack} to be queued
     * @return true if track was queued
     */
    void queue(AudioTrack track) {
        var queued = false;
        if (!this.player.startTrack(track, true)) {
            queued = this.queue.offer(track);
            if (queued) logger.info(track.getInfo().title + " has been queued.");
        }
        logger.info("Skipped queueing " + track.getInfo().title);
    }

    /**
     * Returns the currently playing track or null if no track is playing.
     *
     * @return {@link AudioTrack} or null
     */
    @Nullable AudioTrack getCurrentTrack() {
        return player.getPlayingTrack();
    }

    /**
     * Pauses currently playing {@link AudioTrack}.
     */
    void pause() {
        player.setPaused(true);
        logger.info("paused...");
    }

    /**
     * Skips currently playing {@link AudioTrack}.
     */
    void skip() {
        player.startTrack(queue.poll(), false);
        logger.info("skipping...");
    }

    /**
     * Unpauses currently playing {@link AudioTrack}.
     */
    void unpause() {
        player.setPaused(false);
        logger.info("unpaused...");
    }

    /**
     * Stops currently playing {@link AudioTrack} and clears queue.
     */
    void stop() {
        queue.clear();
        skip();
        logger.info("stopping...");
    }
}
