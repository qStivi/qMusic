package de.qStivi.commands;

import de.qStivi.audio.PlayerManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.time.Duration;

public class SkipCommand implements ICommand {

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        PlayerManager.skip(event.getGuild());
        PlayerManager.setRepeat(event.getGuild(), false);
        event.getHook().editOriginal("Skipping....").delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
    }

    @Override
    public @Nonnull
    String getName() {
        return "skip";
    }

    @Override
    public @Nonnull
    String getDescription() {
        return "Plays next song in queue.";
    }
}
