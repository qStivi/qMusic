package de.qStivi;

import de.qStivi.audio.GuildMusicManager;
import de.qStivi.commands.CommandHandler;
import de.qStivi.commands.ICommand;
import de.qStivi.listener.ControlsManager;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


// TODO what happens when the bot is added to a guild while its running?
public class Main extends ListenerAdapter {

    public static final LavalinkClient LAVALINK = new LavalinkClient(Helpers.getUserIdFromToken(Properties.DISCORD));
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static JDA JDA;
    private static final int SESSION_INVALID = 4006;

    public static void main(String[] args) {

        JDA = JDABuilder.createLight(Properties.DISCORD)
                .addEventListeners(new ControlsManager(), new CommandHandler())
                .setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .enableCache(CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(LAVALINK))
                .setActivity(Activity.playing("/help")).build();

        CommandHandler.updateCommands();

        LAVALINK.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());

        registerLavalinkListeners(LAVALINK);
        registerLavalinkNodes(LAVALINK);

        // Got a lot of 4006 closecodes? Try this "fix"
        LAVALINK.on(WebSocketClosedEvent.class).subscribe((event) -> {
            if (event.getCode() == SESSION_INVALID) {
                final var guildId = event.getGuildId();
                final var guild = JDA.getGuildById(guildId);

                if (guild == null) {
                    return;
                }

                final var connectedChannel = guild.getSelfMember().getVoiceState().getChannel();

                // somehow
                if (connectedChannel == null) {
                    return;
                }

                JDA.getDirectAudioController().reconnect(connectedChannel);
            }
        });
    }


    private static void registerLavalinkNodes(LavalinkClient client) {
        List.of(
                client.addNode(
                        new NodeOptions.Builder()
                                .setName("proxmox")
                                .setServerUri("http://192.168.137.150:2333")
                                .setPassword("youshallnotpass")
                                .build()
                )

//                client.addNode(
//                        new NodeOptions.Builder()
//                                .setName("pi")
//                                .setServerUri("ws://pi.local.duncte123.lgbt:2333")
//                                .setPassword("youshallnotpass")
//                                .setRegionFilter(RegionGroup.US)
//                                .build()
//                )
        ).forEach((node) -> {
            node.on(TrackStartEvent.class).subscribe((event) -> {
                final LavalinkNode node1 = event.getNode();

                LOGGER.trace(
                        "{}: track started: {}",
                        node1.getName(),
                        event.getTrack().getInfo()
                );
            });
        });
    }

    private static void registerLavalinkListeners(LavalinkClient client) {
        client.on(ReadyEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();

            LOGGER.info(
                    "Node '{}' is ready, session id is '{}'!",
                    node.getName(),
                    event.getSessionId()
            );
        });

        client.on(StatsEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();

            LOGGER.info(
                    "Node '{}' has stats, current players: {}/{} (link count {})",
                    node.getName(),
                    event.getPlayingPlayers(),
                    event.getPlayers(),
                    client.getLinks().size()
            );
        });

        client.on(TrackStartEvent.class).subscribe((event) -> {
            GuildMusicManager.getInstance(event.getGuildId(), LAVALINK).scheduler.onTrackStart(event.getTrack());
        });

        client.on(TrackEndEvent.class).subscribe((event) -> {
            GuildMusicManager.getInstance(event.getGuildId(), LAVALINK).scheduler.onTrackEnd(event.getTrack(), event.getEndReason());
        });

        client.on(EmittedEvent.class).subscribe((event) -> {
            if (event instanceof TrackStartEvent) {
                LOGGER.info("Is a track start event!");
            }

            final var node = event.getNode();

            LOGGER.info(
                    "Node '{}' emitted event: {}",
                    node.getName(),
                    event
            );
        });
    }
}
