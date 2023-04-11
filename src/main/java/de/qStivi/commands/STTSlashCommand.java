package de.qStivi.commands;

import de.qStivi.apis.stt.SpeechToText;
import de.qStivi.audio.QAudioSTTHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class STTSlashCommand implements ICommand<SlashCommandInteractionEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(STTSlashCommand.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.getHook().editOriginal("yee").queue();
        var audioManager = event.getGuild().getAudioManager();
        audioManager.openAudioConnection(event.getMember().getVoiceState().getChannel());
        var STTHandler = new QAudioSTTHandler(audioManager);
        audioManager.setReceivingHandler(STTHandler);
        var currentTime = System.currentTimeMillis();
        var maxLength = Duration.ofSeconds(10).toMillis();
    }

    @NotNull
    @Override
    public String getName() {
        return "stt";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "stt test command";
    }
}
