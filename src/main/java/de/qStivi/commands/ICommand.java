package de.qStivi.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import javax.annotation.Nonnull;

public interface ICommand {

    @Nonnull
    CommandData getCommand();

    void handle(SlashCommandInteractionEvent event);

    @Nonnull
    String getName();

    @Nonnull
    String getDescription();
}
