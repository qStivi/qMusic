package de.qStivi;

import de.qStivi.commands.CommandHandler;
import de.qStivi.commands.ICommand;
import de.qStivi.listener.ControlsManager;
import de.qStivi.listener.EventsPreprocessor;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import static org.slf4j.LoggerFactory.getLogger;

public class Bot {
    private static final Timer activityUpdate = new Timer();
    private static final String ACTIVITY = "Evolving...";
    private static final Logger logger = getLogger(Bot.class);

    public static void main(String[] args) throws LoginException {
        var shardManager = DefaultShardManagerBuilder.createLight(Config.get("TOKEN"))
                .addEventListeners(new EventsPreprocessor(), ControlsManager.getINSTANCE())
                .setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .enableCache(CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .build();

        List<CommandData> commandDataList = new ArrayList<>();
        for (ICommand command : CommandHandler.COMMAND_LIST) {
            commandDataList.add(command.getCommand());
        }
        shardManager.getShards().forEach(jda -> jda.updateCommands().addCommands(commandDataList).queue());
    }
}
