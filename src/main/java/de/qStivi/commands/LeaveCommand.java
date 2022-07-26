package de.qStivi.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class LeaveCommand implements ICommand {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        var g = event.getGuild();
        if (g == null) {
            logger.error("Guild was null!");
            return;
        }
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
