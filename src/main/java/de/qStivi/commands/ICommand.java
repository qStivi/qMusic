package de.qStivi.commands;

import de.qStivi.ChatMessage;
import de.qStivi.NoResultsException;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface ICommand<T extends GenericCommandInteractionEvent> {

    @NotNull
    CommandData getCommand();

    void handle(T event, ChatMessage message) throws NoResultsException, IOException;

    @NotNull
    String getName();

    @NotNull
    default String getDescription() {
        //noinspection ConstantConditions
        return null;
    }
}
