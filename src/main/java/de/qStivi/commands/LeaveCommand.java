package de.qStivi.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class LeaveCommand implements ICommand {

    @NotNull
    @Override
    public CommandData getCommand() {
        return new CommandData(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandEvent event) {

        event.getGuild().getAudioManager(); //TODO why?
        event.getGuild().getAudioManager().closeAudioConnection();
        event.getHook().editOriginal("Bye Bye").delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
    }

    @Override
    public @NotNull
    String getName() {
        return "leave";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Disconnects bot from any voice channel.";
    }
}
