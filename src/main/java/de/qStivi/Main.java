package de.qStivi;

import de.qStivi.commands.CommandHandler;
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
        try {
            JDA = JDABuilder.createLight(Properties.DISCORD)
                    .addEventListeners(new DiscordListeners(), new CommandHandler())
                    .setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                    .enableCache(CacheFlag.VOICE_STATE)
                    .setMemberCachePolicy(MemberCachePolicy.VOICE)
                    .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(Lavalink.getClient()))
                    .setActivity(Activity.customStatus("/play"))
                    .build();

            CommandHandler.updateCommands();

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (JDA != null) {
                        MessageManager.deleteAllMessages();
                        JDA.shutdown();
                    }
                    CommandHandler.shutdown();
                    LOGGER.info("Application shutdown gracefully.");
                } catch (Exception e) {
                    LOGGER.error("Error during shutdown", e);
                }
            }));
        } catch (Exception e) {
            LOGGER.error("Failed to initialize JDA", e);
        }
    }
}