package de.qStivi.commands;

import de.qStivi.Main;
import de.qStivi.audio.AudioLoader;
import de.qStivi.audio.GuildMusicManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        event.getHook().editOriginal("Loading...").complete();

        var query = Objects.requireNonNull(event.getOption(QUERY)).getAsString();
        var randomOption = event.getOption(RANDOM);
        var guild = event.getGuild();

        var random = false;

        if (randomOption != null) {
            random = randomOption.getAsBoolean();
        }

//        var player = QPlayer.getInstance(guild);

//        player.setMessage(event.getHook().retrieveOriginal().complete());

        // We are already connected, go ahead and play
        if (guild.getSelfMember().getVoiceState().inAudioChannel()) {
            event.deferReply(false).queue();
        } else {
            // Connect to VC first
            joinHelper(event);
        }

        Main.LAVALINK.getOrCreateLink(event.getGuild().getIdLong()).loadItem(query).subscribe(new AudioLoader(event, GuildMusicManager.getInstance(guild.getIdLong(), Main.LAVALINK)));



//        player.openAudioConnection(event);
//
//        player.playQuery(query);
    }

    // Makes sure that the bot is in a voice channel!
    private void joinHelper(SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (memberVoiceState.inAudioChannel()) {
            event.getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
        }

        GuildMusicManager.getInstance(event.getGuild().getIdLong(), Main.LAVALINK);

        event.getHook().editOriginal("Joining your channel!").queue();
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
