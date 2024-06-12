package de.qStivi.commands;

import de.qStivi.ChatMessage;
import de.qStivi.audio.AudioLoader;
import de.qStivi.audio.GuildMusicManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoopSlashCommand implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoopSlashCommand.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event, ChatMessage message) {
        var isLooping = AudioLoader.getInstance(event.getGuild().getIdLong()).mngr.scheduler.loop;

        if (isLooping) {
            message.edit("Looping disabled.");
        } else {
            message.edit("Looping enabled.");
        }

        AudioLoader.getInstance(event.getGuild().getIdLong()).mngr.loop();
    }

    @NotNull
    @Override
    public String getName() {
        return "loop";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Loops the current playback.";
    }
}