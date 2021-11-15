package de.qStivi.commands;

import de.qStivi.audio.PlayerManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class SkipCommand implements ICommand {

    @NotNull
    @Override
    public CommandData getCommand() {
        return new CommandData(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandEvent event) {
        PlayerManager.getINSTANCE().skip(event.getGuild());
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
