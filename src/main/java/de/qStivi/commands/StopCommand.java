package de.qStivi.commands;

import de.qStivi.audio.GuildMusicManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopCommand implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopCommand.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.getHook().editOriginal("Stopping...").complete();

        GuildMusicManager.getInstance(event.getGuild().getIdLong()).stop();
    }

    @NotNull
    @Override
    public String getName() {
        return "Stop";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Stops the current playback.";
    }
}
