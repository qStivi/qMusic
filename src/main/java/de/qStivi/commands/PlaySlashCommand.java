package de.qStivi.commands;

import de.qStivi.NoResultsException;
import de.qStivi.apis.YouTubeAPI;
import de.qStivi.audio.LavaPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class PlaySlashCommand implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaySlashCommand.class);

    private static final String QUERY = "query";
    private static final String RANDOM = "random";

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.STRING, QUERY, "The thing you want to play (search or link)", true)
                .addOption(OptionType.BOOLEAN, RANDOM, "Whether you want to randomize the order of playback (default true)", false);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        var query = Objects.requireNonNull(event.getOption(QUERY)).getAsString();
        var randomOption = event.getOption(QUERY);
        var guild = event.getGuild();

        var random = false;

        if (randomOption != null) {
            random = randomOption.getAsBoolean();
        }

        LavaPlayer.openAudioConnection(event);

        LavaPlayer.play(query, guild, random);

        while (LavaPlayer.trackIsLoading(guild)) {
            Thread.onSpinWait();
        }
        if (LavaPlayer.loadFailed(guild)) {
            LOGGER.info("Load Failed!");
            event.getHook().editOriginal("Load Failed!").queue();
//            var id = YouTubeAPI.getSearchResults(query).get(0).getId().getVideoId();
//            LavaPlayer.play(id, guild);
//            while (LavaPlayer.trackIsLoading(guild)) {
//                Thread.onSpinWait();
//            }
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "play";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Provide a link or search query to some video, music or playlist and I will try to play it for you.";
    }
}
