package de.qStivi;

import de.qStivi.audio.AudioLoader;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;

public class Lavalink {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Lavalink.class);
    private static final LavalinkClient LAVALINK;

    static {
        LAVALINK = new LavalinkClient(Helpers.getUserIdFromToken(Properties.DISCORD));
        registerLavalinkListeners();
        registerLavalinkNodes();
    }

    public static Link get(long guildID) {
        return LAVALINK.getOrCreateLink(guildID);
    }

    public static LavalinkClient getClient() {
        return LAVALINK;
    }

    public static LavalinkPlayer getCachedPlayer(long guildID) {
        return get(guildID).getPlayer().block();
    }

    private static void registerLavalinkNodes() {
        LAVALINK.addNode(new NodeOptions.Builder().setName("Node 1").setServerUri("http://192.168.137.150:2333").setPassword("youshallnotpass").build());
    }

    private static void registerLavalinkListeners() {

        // Subscribe to the TrackStartEvent to log the track start
        LAVALINK.on(TrackStartEvent.class).subscribe((event) -> LOGGER.debug("{}: track started: {}", event.getNode().getName(), event.getTrack().getInfo()));

        // Subscribe to the ReadyEvent to log the session id
        LAVALINK.on(ReadyEvent.class).subscribe((event) -> LOGGER.debug("Node '{}' is ready, session id is '{}'!", event.getNode().getName(), event.getSessionId()));

        // Subscribe to the StatsEvent to log some stats
        LAVALINK.on(StatsEvent.class).subscribe((event) -> {
            LOGGER.debug("Node '{}' has stats, current players: {}/{} (link count {})", event.getNode().getName(), event.getPlayingPlayers(), event.getPlayers(), LAVALINK.getLinks().size());
        });

        LAVALINK.on(PlayerUpdateEvent.class).subscribe((event) -> {
            LAVALINK.getLinks().forEach((link) -> {
                var track = link.getPlayer().block().getTrack();
                while (track == null) {
                    LOGGER.debug("Track is null, trying to get the track again...");
                    track = link.getCachedPlayer().getTrack();
                }
                var trackInfo = track.getInfo();
                var position = link.getCachedPlayer().getPosition();
                var duration = trackInfo.getLength();
                var progressBar = generateProgressBar(position, duration);
                ChatMessage.getInstance().edit(progressBar + "\n" + trackInfo.getUri());
            });
        });

        // Subscribe to the TrackStartEvent to handle the track start
        LAVALINK.on(TrackStartEvent.class).subscribe((event) -> AudioLoader.getInstance(event.getGuildId()).mngr.scheduler.onTrackStart(event.getTrack()));

        // Subscribe to the TrackEndEvent to handle the track end
        LAVALINK.on(TrackEndEvent.class).subscribe((event) -> AudioLoader.getInstance(event.getGuildId()).mngr.scheduler.onTrackEnd(event.getTrack(), event.getEndReason()));

        // Subscribe to the EmittedEvent to log all events
        LAVALINK.on(EmittedEvent.class).subscribe((event) -> {
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
        return String.format("%s %s %s", currentTime, progressBar.toString(), totalTime);
    }

    private static String formatTime(long timeInSeconds) {
        long minutes = timeInSeconds / 60;
        long seconds = timeInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

}
