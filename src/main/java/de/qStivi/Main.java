package de.qStivi;

import de.qStivi.audio.GuildMusicManager;
import de.qStivi.commands.CommandHandler;
import de.qStivi.listener.ControlsManager;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// TODO what happens when the bot is added to a guild while its running?
public class Main extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static LavalinkClient LAVALINK;
    public static JDA JDA;

    public static void main(String[] args) {

        LAVALINK = new LavalinkClient(Helpers.getUserIdFromToken(Properties.DISCORD));

        JDA = JDABuilder.createLight(Properties.DISCORD).addEventListeners(new ControlsManager(), new CommandHandler()).setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS).enableCache(CacheFlag.VOICE_STATE).setMemberCachePolicy(MemberCachePolicy.VOICE).setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(LAVALINK)).setActivity(Activity.playing("/help")).build();

        CommandHandler.updateCommands();

        registerLavalinkListeners(LAVALINK);
        registerLavalinkNodes(LAVALINK);
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
        client.on(TrackStartEvent.class).subscribe((event) -> GuildMusicManager.getInstance(event.getGuildId()).scheduler.onTrackStart(event.getTrack()));

        // Subscribe to the TrackEndEvent to handle the track end
        client.on(TrackEndEvent.class).subscribe((event) -> GuildMusicManager.getInstance(event.getGuildId()).scheduler.onTrackEnd(event.getTrack(), event.getEndReason()));

        // Subscribe to the EmittedEvent to log all events
        client.on(EmittedEvent.class).subscribe((event) -> {
            if (event instanceof TrackStartEvent) {
                LOGGER.debug("Is a track start event!");
            }

            LOGGER.debug("Node '{}' emitted event: {}", event.getNode().getName(), event);
        });
    }
}
