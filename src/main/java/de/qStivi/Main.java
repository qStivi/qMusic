package de.qStivi;

import de.qStivi.commands.CommandHandler;
import de.qStivi.listener.ControlsManager;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static JDA JDA;

    public static void main(String[] args) {
        LOGGER.info("Initializing bot...");

        JDABuilder builder = JDABuilder.createLight(Properties.DISCORD)
                .setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .enableCache(CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(Lavalink.getClient()))
                .setActivity(Activity.customStatus("/play"));

        builder.addEventListeners(new DiscordListeners(), new ControlsManager(), new CommandHandler());

        try {
            JDA = builder.build();
            LOGGER.info("Bot is now running!");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize bot: ", e);
        }

        CommandHandler.updateCommands();
    }
}