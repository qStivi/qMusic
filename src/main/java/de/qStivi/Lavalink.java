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
        registerLavalinkListeners(LAVALINK);
        registerLavalinkNodes(LAVALINK);
    }

    public static Link get(long guildID) {
        return LAVALINK.getOrCreateLink(guildID);
    }

    public static LavalinkClient getClient() {
        return LAVALINK;
    }

    public static LavalinkPlayer getCachedPlayer(long guildID) {
        return get(guildID).getCachedPlayer();
    }

    private static void registerLavalinkNodes(LavalinkClient client) {

        // Create Lavalink node providing the name, server uri and password
        var lavalinkNode = new NodeOptions.Builder().setName("Node 1").setServerUri("http://192.168.137.150:2333").setPassword("youshallnotpass").build();

        // Add the node to the client
        client.addNode(lavalinkNode);

        // Subscribe to the TrackStartEvent to log the track start
        client.on(TrackStartEvent.class).subscribe((event) -> LOGGER.debug("{}: track started: {}", event.getNode().getName(), event.getTrack().getInfo()));
    }

    private static void registerLavalinkListeners(LavalinkClient client) {

        // Subscribe to the ReadyEvent to log the session id
        client.on(ReadyEvent.class).subscribe((event) -> LOGGER.debug("Node '{}' is ready, session id is '{}'!", event.getNode().getName(), event.getSessionId()));

        // Subscribe to the StatsEvent to log some stats
        client.on(StatsEvent.class).subscribe((event) -> LOGGER.debug("Node '{}' has stats, current players: {}/{} (link count {})", event.getNode().getName(), event.getPlayingPlayers(), event.getPlayers(), client.getLinks().size()));

        // Subscribe to the TrackStartEvent to handle the track start
        client.on(TrackStartEvent.class).subscribe((event) -> AudioLoader.getInstance(event.getGuildId()).mngr.scheduler.onTrackStart(event.getTrack()));

        // Subscribe to the TrackEndEvent to handle the track end
        client.on(TrackEndEvent.class).subscribe((event) -> AudioLoader.getInstance(event.getGuildId()).mngr.scheduler.onTrackEnd(event.getTrack(), event.getEndReason()));

        // Subscribe to the EmittedEvent to log all events
        client.on(EmittedEvent.class).subscribe((event) -> {
            if (event instanceof TrackStartEvent) {
                LOGGER.debug("Is a track start event!");
            }

            LOGGER.debug("Node '{}' emitted event: {}", event.getNode().getName(), event);
        });
    }
}
