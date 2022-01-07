package de.qStivi;

import de.qStivi.commands.CommandHandler;
import de.qStivi.commands.ICommand;
import de.qStivi.listener.ControlsManager;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class Bot {
    private static final Logger logger = getLogger(Bot.class);
    public static ShardManager SHARD_MANAGER;

    public static void main(String[] args) throws LoginException {
        SHARD_MANAGER = DefaultShardManagerBuilder.createLight(Config.get("TOKEN")).addEventListeners(new ControlsManager(), new CommandHandler()).setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS).enableCache(CacheFlag.VOICE_STATE).setMemberCachePolicy(MemberCachePolicy.VOICE).setActivity(Activity.of(Activity.ActivityType.PLAYING, "/help")).build();

        List<CommandData> commandDataList = new ArrayList<>();
        for (ICommand command : CommandHandler.COMMAND_LIST) {
            commandDataList.add(command.getCommand());
        }
        SHARD_MANAGER.getShards().forEach(jda -> jda.updateCommands().addCommands(commandDataList).complete());
    }
}
