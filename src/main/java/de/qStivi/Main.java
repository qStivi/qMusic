package de.qStivi;

import de.qStivi.commands.CommandHandler;
import de.qStivi.listener.ControlsManager;
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
    public static JDA JDA;

    public static void main(String[] args) {

        JDA = JDABuilder.createLight(Properties.DISCORD).addEventListeners(new ControlsManager(), new CommandHandler()).setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS).enableCache(CacheFlag.VOICE_STATE).setMemberCachePolicy(MemberCachePolicy.VOICE).setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(Lavalink.getClient())).setActivity(Activity.playing("/help")).build();

        CommandHandler.updateCommands();

    }


}
