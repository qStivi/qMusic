package de.qStivi.commands.slash;

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
        event.deferReply().complete();
        AudioLoader.getInstance(event.getGuild().getIdLong()).mngr.toggleLoop();
        var isLooping = AudioLoader.getInstance(event.getGuild().getIdLong()).mngr.scheduler.loop;

        if (isLooping) {
            event.getHook().editOriginal("Looping enabled.").queue((m) -> m.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
        } else {
            event.getHook().editOriginal("Looping disabled.").queue((m) -> m.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
        }
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