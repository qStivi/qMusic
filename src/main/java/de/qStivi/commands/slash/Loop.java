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

public class Loop implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Loop.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        var isLooping = AudioLoader.getInstance(event.getGuild().getIdLong()).mngr.scheduler.loop;

        if (isLooping) {
            ChatMessage.getInstance(event.getHook()).setMessage("Looping disabled.");
        } else {
            ChatMessage.getInstance(event.getHook()).setMessage("Looping enabled.");
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