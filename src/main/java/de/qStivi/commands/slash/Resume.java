package de.qStivi.commands.slash;

import de.qStivi.audio.AudioLoader;
import de.qStivi.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resume implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Resume.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().complete();
        AudioLoader.getInstance(event.getGuild().getIdLong()).mngr.continuePlaying();
        event.getHook().editOriginal("Resumed.").queue((m) -> m.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @NotNull
    @Override
    public String getName() {
        return "resume";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Resumes the current playback.";
    }
}