package de.qStivi.commands;

import de.qStivi.ChatMessage;
import de.qStivi.audio.GuildMusicManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PauseSlashCommand implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PauseSlashCommand.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event, ChatMessage message) {
        event.getHook().editOriginal("Pausing...").complete();

        GuildMusicManager.getInstance(event.getGuild().getIdLong()).pause();
    }

    @NotNull
    @Override
    public String getName() {
        return "pause";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Pauses the current playback.";
    }
}