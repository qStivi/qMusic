package de.qStivi.commands;

import de.qStivi.audio.GuildMusicManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PauseCommand implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PauseCommand.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.getHook().editOriginal("Pausing...").complete();

        GuildMusicManager.getInstance(event.getGuild().getIdLong()).pause();
    }

    @NotNull
    @Override
    public String getName() {
        return "Pause";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Pauses the current playback.";
    }
}