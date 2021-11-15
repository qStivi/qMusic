package de.qStivi.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class JoinCommand implements ICommand {
    private final static Logger LOGGER = LoggerFactory.getLogger(JoinCommand.class);

    public static boolean join(Guild guild, User author) {
        AtomicBoolean successful = new AtomicBoolean(false);
        var channels = guild.getVoiceChannels();
        channels.forEach(
                (channel) -> {
                    channel.getMembers().forEach(
                            (member) -> {
                                if (member.getId().equals(author.getId())) {
                                    guild.getAudioManager().openAudioConnection(channel);
                                    successful.set(true);
                                }
                            }
                    );
                }
        );
        return successful.get();
    }

    @NotNull
    @Override
    public CommandData getCommand() {
        return new CommandData(getName(), getDescription());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void handle(SlashCommandEvent event) {
        var hook = event.getHook();

        var success = join(event.getGuild(), event.getMember().getUser());
        if (success) {
            hook.editOriginal("Hi").queue();
        } else {
            hook.editOriginal("Something went wrong :(").queue();
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "join";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Connects or moves bot to your voice channel.";
    }
}
