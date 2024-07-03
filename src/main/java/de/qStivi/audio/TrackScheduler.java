package de.qStivi.audio;

import de.qStivi.ChatMessage;
import de.qStivi.Lavalink;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TrackScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackScheduler.class);
    public final Queue<Track> queue = new LinkedList<>();
    private final GuildMusicManager guildMusicManager;
    public boolean loop = false;

    public TrackScheduler(GuildMusicManager guildMusicManager) {
        this.guildMusicManager = guildMusicManager;
        LOGGER.info("New TrackScheduler initialized.");
    }

    public void enqueue(Track track, boolean shouldSkipQueue, boolean shouldPlayNow) {
        LOGGER.info("Enqueuing track: {}", track.getInfo().getTitle());

        if (isPlayerEmpty()) {
            startTrack(track);
        } else {
            if (shouldSkipQueue) {
                skipQueue(track);
            } else {
                queue.offer(track);
                LOGGER.info("Track added to queue: {}", track.getInfo().getTitle());
            }
        }

        LOGGER.info("Track enqueued: {}", track.getInfo().getTitle());
    }

    public void enqueuePlaylist(List<Track> tracks, boolean shouldSkipQueue, boolean shouldPlayNow, boolean shuffle) {
        LOGGER.info("Enqueuing playlist with {} tracks.", tracks.size());

        if (shuffle) {
            shuffleTracks(tracks);
        }

        if (isPlayerEmpty()) {
            queue.addAll(tracks);
            startTrack(queue.poll());
        } else {
            if (shouldSkipQueue) {
                skipQueue(tracks);
            } else {
                queue.addAll(tracks);
            }
        }

        LOGGER.info("Playlist enqueued.");
    }

    public void onTrackStart(Track track) {
        LOGGER.info("Track started: {}", track.getInfo().getTitle());
        ChatMessage.getInstance().edit(track.getInfo().getUri());
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        LOGGER.info("Track ended: {}", lastTrack.getInfo().getTitle());

        if (endReason.getMayStartNext()) {
            handleTrackEnd(lastTrack);
        }
    }

    private void startTrack(Track track) {
        LOGGER.info("Starting track: {}", track.getInfo().getTitle());
        Lavalink.getLink(guildMusicManager.guildId).createOrUpdatePlayer().setTrack(track).setVolume(35).subscribe();
    }

    private boolean isPlayerEmpty() {
        return Lavalink.getCachedPlayer(guildMusicManager.guildId).getTrack() == null;
    }

    private void skipQueue(Track track) {
        var queueCopy = new LinkedList<>(queue);
        queue.clear();
        queue.offer(track);
        queue.addAll(queueCopy);
        AudioLoader.getInstance(guildMusicManager.guildId).setShouldSkipQueue(false);
    }

    private void skipQueue(List<Track> tracks) {
        var queueCopy = new LinkedList<>(queue);
        queue.clear();
        queue.addAll(tracks);
        queue.addAll(queueCopy);
        AudioLoader.getInstance(guildMusicManager.guildId).setShouldSkipQueue(false);
    }

    private void shuffleTracks(List<Track> tracks) {
        LOGGER.info("Shuffling playlist.");
        java.util.Collections.shuffle(tracks);
    }

    private void handleTrackEnd(Track lastTrack) {
        if (loop) {
            LOGGER.info("Looping last track: {}", lastTrack.getInfo().getTitle());
            startTrack(lastTrack);
        } else {
            var nextTrack = queue.poll();
            if (nextTrack != null) {
                LOGGER.info("Next track: {}", nextTrack.getInfo().getTitle());
                startTrack(nextTrack);
            } else {
                LOGGER.info("No more tracks in queue.");
                ChatMessage.getInstance().delete();
            }
        }
    }
}