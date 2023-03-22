package de.qStivi.commands;

import de.qStivi.NoResultsException;
import de.qStivi.apis.YouTubeAPI;
import de.qStivi.audio.LavaPlayer;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PlaySlashCommand implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaySlashCommand.class);

    private static final String COMMAND_NAME = "link_or_search";

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription()).addOption(OptionType.STRING, COMMAND_NAME, getDescription(), true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) throws NoResultsException, IOException {
        var option = event.getOption(COMMAND_NAME);
        if (option == null) {
            event.reply("Something ent wrong!").queue();
            return;
        }
        var guild = event.getGuild();
        if (guild == null) {
            event.reply("Something ent wrong!").queue();
            return;
        }
        var member = event.getMember();
        if (member == null) {
            event.reply("Something ent wrong!").queue();
            return;
        }
        var voiceState = member.getVoiceState();
        if (voiceState == null) {
            event.reply("Something ent wrong!").queue();
            return;
        }
        guild.getAudioManager().openAudioConnection(voiceState.getChannel());

        LavaPlayer.playOrdered(option.getAsString(), guild);
        while (LavaPlayer.trackIsLoading(guild)) {
            // TODO Is there a better way to do this?
        }
        if (LavaPlayer.loadFailed(guild)) {
            var id = YouTubeAPI.getSearchResults(option.getAsString()).get(0).getId().getVideoId();
            LavaPlayer.playOrdered(id, guild);
            while (LavaPlayer.trackIsLoading(guild)) {
                // TODO Is there a better way to do this?
            }
        }
        var track = LavaPlayer.getPlayingTrack(guild);
        event.reply("Playing: " + track.getInfo().author + " - " + track.getInfo().title + " (" + track.getIdentifier() + ")\n" + "https://youtu.be/" + track.getIdentifier()).addComponents(ActionRow.of(Button.primary("play", Emoji.fromFormatted("<:play:929131671004012584>")), Button.primary("pause", Emoji.fromFormatted("<:pause:929131670957854721>")), Button.primary("stop", Emoji.fromFormatted("<:stop:929130911382007848>")), Button.primary("skip", Emoji.fromFormatted("<:skip:929131670660067370>")), Button.primary("repeat", Emoji.fromFormatted("<:repeat:929131670941089864>")))).queue();
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
