package de.qStivi;

import de.qStivi.commands.CommandHandler;
import de.qStivi.commands.ICommand;
import de.qStivi.listener.ControlsManager;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class Bot extends ListenerAdapter {

    private static final Logger logger = getLogger(Bot.class);
    public static ShardManager SHARD_MANAGER;

    public static void main(String[] args) {
        SHARD_MANAGER = DefaultShardManagerBuilder.createLight(Config.get("TOKEN")).addEventListeners(new ControlsManager(), new CommandHandler(), new Bot()).setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS).enableCache(CacheFlag.VOICE_STATE).setMemberCachePolicy(MemberCachePolicy.VOICE).setActivity(Activity.playing("/help")).build();

        List<CommandData> commandDataList = new ArrayList<>();
        for (ICommand command : CommandHandler.COMMAND_LIST) {
            commandDataList.add(command.getCommand());
        }
        SHARD_MANAGER.getShards().forEach(jda -> jda.getGuilds().forEach(guild -> guild.updateCommands().addCommands(commandDataList).complete()));
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("Done!");
    }
}
