package de.qStivi.commands;

import de.qStivi.ChatMessage;
import de.qStivi.audio.GuildMusicManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoopCommand implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoopCommand.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event, ChatMessage message) {
        event.getHook().editOriginal("Looping...").complete();

        GuildMusicManager.getInstance(event.getGuild().getIdLong()).loop();
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