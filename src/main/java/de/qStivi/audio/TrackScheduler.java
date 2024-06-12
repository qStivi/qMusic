package de.qStivi.audio;

import de.qStivi.ChatMessage;
import de.qStivi.Lavalink;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TrackScheduler {
    public final Queue<Track> queue = new LinkedList<>();
    private final GuildMusicManager guildMusicManager;
    public boolean loop = false;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TrackScheduler.class);

    public TrackScheduler(GuildMusicManager guildMusicManager) {
        this.guildMusicManager = guildMusicManager;
        LOGGER.info("New TrackScheduler initialized.");
    }

    public void enqueue(Track track, boolean shouldSkipQueue) {
        LOGGER.info("Enqueuing track: {}", track.getInfo().getTitle());

        if (Lavalink.getCachedPlayer(guildMusicManager.guildId).getTrack() == null) {
            this.startTrack(track);
        } else {
            // If shouldSkipQueue is true, add the track to the front of the queue
            if (shouldSkipQueue) {
                var queueCopy = new LinkedList<>(this.queue);
                this.queue.clear();
                this.queue.offer(track);
                this.queue.addAll(queueCopy);
                AudioLoader.getInstance(guildMusicManager.guildId).shouldSkipQueue(false);
                return;
            }
            this.queue.offer(track);
            LOGGER.info("Added track to top of queue: {}", track.getInfo().getTitle());
        }

        LOGGER.info("Track enqueued: {}", track.getInfo().getTitle());
    }

    public void enqueuePlaylist(List<Track> tracks) {
        LOGGER.info("Enqueuing playlist with {} tracks.", tracks.size());
        this.queue.addAll(tracks);

        if (Lavalink.getCachedPlayer(guildMusicManager.guildId).getTrack() == null) {
            this.startTrack(this.queue.poll());
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
            LOGGER.info("End reason: {}", endReason.name());

            // Loop the last track if loop is enabled
            if (this.loop) {
                LOGGER.info("Looping last track: {}", lastTrack);
                this.startTrack(lastTrack);
                return;
            }

            LOGGER.info("Polling next track from queue.");
            final var nextTrack = this.queue.poll();

            // Start the next track if there is one
            if (nextTrack != null) {
                LOGGER.info("Next track: {}", nextTrack);
                this.startTrack(nextTrack);
            } else {
                LOGGER.info("No more tracks in queue.");
                ChatMessage.getInstance().delete();
            }
        }
    }

    private void startTrack(Track track) {
        LOGGER.info("Starting track: {}", track.getInfo().getTitle());

        Lavalink.get(guildMusicManager.guildId).createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(35)
                        .subscribe();

        LOGGER.info("Track started: {}", track.getInfo().getTitle());
    }
}
