package de.qStivi;

import de.qStivi.audio.AudioLoader;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lavalink {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lavalink.class);
    private static final LavalinkClient LAVALINK_CLIENT;

    static {
        LAVALINK_CLIENT = new LavalinkClient(Helpers.getUserIdFromToken(Properties.DISCORD));
        registerLavalinkListeners();
        registerLavalinkNodes();
    }

    public static Link getLink(long guildID) {
        return LAVALINK_CLIENT.getOrCreateLink(guildID);
    }

    public static LavalinkClient getClient() {
        return LAVALINK_CLIENT;
    }

    public static LavalinkPlayer getCachedPlayer(long guildID) {
        return getLink(guildID).getPlayer().block();
    }

    private static void registerLavalinkNodes() {
        LAVALINK_CLIENT.addNode(new NodeOptions.Builder().setName("Node 1").setServerUri("http://192.168.137.150:2333").setPassword("youshallnotpass").build());
    }

    private static void registerLavalinkListeners() {
        LAVALINK_CLIENT.on(TrackStartEvent.class).subscribe(event -> LOGGER.debug("{}: track started: {}", event.getNode().getName(), event.getTrack().getInfo()));

        LAVALINK_CLIENT.on(ReadyEvent.class).subscribe(event -> LOGGER.debug("Node '{}' is ready, session id is '{}'!", event.getNode().getName(), event.getSessionId()));

        LAVALINK_CLIENT.on(StatsEvent.class).subscribe(event -> LOGGER.debug("Node '{}' has stats, current players: {}/{} (link count {})", event.getNode().getName(), event.getPlayingPlayers(), event.getPlayers(), LAVALINK_CLIENT.getLinks().size()));

        LAVALINK_CLIENT.on(PlayerUpdateEvent.class).subscribe(event -> {
            LOGGER.debug("PlayerUpdateEvent: {}", event);
            LAVALINK_CLIENT.getLinks().forEach(link -> {
                var track = link.getPlayer().block().getTrack();
                while (track == null) {
                    LOGGER.debug("Track is null, trying to get the track again...");
                    track = link.getCachedPlayer().getTrack();
                }
                var trackInfo = track.getInfo();
                var position = link.getCachedPlayer().getPosition();
                var duration = trackInfo.getLength();
                var progressBar = generateProgressBar(position, duration);

                var sb = new StringBuilder();
                if (AudioLoader.getInstance(link.getGuildId()).mngr.scheduler.loop) {
                    sb.append(":repeat_one: Looping enabled :repeat_one:\n");
                }
                sb.append(progressBar).append("\n").append(trackInfo.getUri());

                ChatMessage.getInstance().edit(sb.toString());
            });
        });

        LAVALINK_CLIENT.on(TrackStartEvent.class).subscribe(event -> AudioLoader.getInstance(event.getGuildId()).mngr.scheduler.onTrackStart(event.getTrack()));

        LAVALINK_CLIENT.on(TrackEndEvent.class).subscribe(event -> AudioLoader.getInstance(event.getGuildId()).mngr.scheduler.onTrackEnd(event.getTrack(), event.getEndReason()));

        LAVALINK_CLIENT.on(EmittedEvent.class).subscribe(event -> {
            if (event instanceof TrackStartEvent) {
                LOGGER.debug("Is a track start event!");
            }
            LOGGER.debug("Node '{}' emitted event: {}", event.getNode().getName(), event);
        });
    }

    private static String generateProgressBar(long position, long duration) {
        int barLength = 20; // length of the progress bar
        char filledChar = '=';
        char unfilledChar = '-';

        // Calculate the number of filled and unfilled positions
        int filledLength = (int) ((double) position / duration * barLength);
        int unfilledLength = barLength - filledLength;

        // Create the progress bar string
        StringBuilder progressBar = new StringBuilder();
        progressBar.append("[");
        for (int i = 0; i < filledLength; i++) {
            progressBar.append(filledChar);
        }
        for (int i = 0; i < unfilledLength; i++) {
            progressBar.append(unfilledChar);
        }
        progressBar.append("]");

        // Convert milliseconds to seconds and format time in mm:ss
        String currentTime = formatTime(position / 1000);
        String totalTime = formatTime(duration / 1000);

        // Combine time and progress bar
        return String.format("%s %s %s", currentTime, progressBar, totalTime);
    }

    private static String formatTime(long timeInSeconds) {
        long minutes = timeInSeconds / 60;
        long seconds = timeInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
