package de.qStivi.commands;

import de.qStivi.audio.QAudioEchoHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class EchoSlashCommand implements ICommand<SlashCommandInteractionEvent> {
    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        var audioManager = event.getGuild().getAudioManager();
        audioManager.openAudioConnection(event.getMember().getVoiceState().getChannel());
        var echoHandler = new QAudioEchoHandler();
        audioManager.setSendingHandler(echoHandler);
        audioManager.setReceivingHandler(echoHandler);
    }

    @NotNull
    @Override
    public String getName() {
        return "echo";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "echo test command";
    }
}
