package de.qStivi;

import de.qStivi.commands.CommandHandler;
import de.qStivi.commands.ICommand;
import de.qStivi.listener.ControlsManager;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
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


// TODO what happens when the bot is added to a guild while its running?
public class Main extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static JDA JDA;

    public static void main(String[] args) {

        String botToken = System.getenv(Properties.DISCORD);
        LavalinkClient client = new LavalinkClient(
                Helpers.getUserIdFromToken(botToken)
        );

        JDA = JDABuilder.createLight(Properties.DISCORD)
                .addEventListeners(new ControlsManager(), new CommandHandler())
                .setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .enableCache(CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(client))
                .setActivity(Activity.playing("/help")).build();

        CommandHandler.updateCommands();
    }
}
