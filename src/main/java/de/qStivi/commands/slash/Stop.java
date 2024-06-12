package de.qStivi.commands.slash;

import de.qStivi.ChatMessage;
import de.qStivi.audio.AudioLoader;
import de.qStivi.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stop implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Stop.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        ChatMessage.getInstance(event.getHook()).setMessage("Stopping...");

        AudioLoader.getInstance(event.getGuild().getIdLong()).mngr.stop();
    }

    @NotNull
    @Override
    public String getName() {
        return "stop";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Stops the current playback.";
    }
}
