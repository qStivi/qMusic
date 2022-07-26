package de.qStivi.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class JoinCommand implements ICommand {

    private final static Logger LOGGER = LoggerFactory.getLogger(JoinCommand.class);

    public static boolean join(Guild guild, Member author) {
        var voiceState = author.getVoiceState();
        if (voiceState == null) {
            LOGGER.info("Failed to join voice channel! CacheFlag.VOICE_STATE is disabled.");
            return false;
        }
        var channel = voiceState.getChannel();
        if (channel == null) {
            LOGGER.info("Failed to join voice channel! Member " + author.getEffectiveName() + " not in a voice channel.");
            return false;
        }
        guild.getAudioManager().openAudioConnection(channel);
        LOGGER.info("Successfully joined voice channel: " + channel.getName());
        return true;
    }

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        var hook = event.getHook();

        LOGGER.info("Trying to join voice channel...");
        var success = join(event.getGuild(), event.getMember());
        if (success) {
            hook.editOriginal("Hi").delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
        } else {
            hook.editOriginal("Something went wrong :(").delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
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
