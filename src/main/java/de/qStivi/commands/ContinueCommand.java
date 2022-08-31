package de.qStivi.commands;

import de.qStivi.audio.PlayerManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.time.Duration;

public class ContinueCommand implements ICommand {

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        PlayerManager.unpause(event.getGuild()); //TODO add check if continue has worked if possible
        event.getHook().editOriginal("Continuing...").delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
    }

    @Override
    public @Nonnull
    String getName() {
        return "continue";
    }

    @Override
    public @Nonnull
    String getDescription() {
        return "Continues to play paused music.";
    }
}
