package de.qStivi.commands;

import de.qStivi.ChatMessage;
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
                .addOption(OptionType.STRING, QUERY, "The thing you want to play (search or link)", true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event, ChatMessage message) {
        message.edit("Loading...");

        var query = Objects.requireNonNull(event.getOption(QUERY)).getAsString();
        var random = event.getOption(RANDOM) != null && event.getOption(RANDOM).getAsBoolean();

        // Try parsing as URL and prepend "ytsearch:" if it fails
        try {
            new java.net.URL(query);
        } catch (java.net.MalformedURLException e) {
            query = "ytsearch:" + query;
        }

        var guild = event.getGuild();

        joinHelper(event, message);

        Main.LAVALINK.getOrCreateLink(guild.getIdLong()).loadItem(query).subscribe(new AudioLoader(GuildMusicManager.getInstance(guild.getIdLong()), message, false));

    }

    // Makes sure that the bot is in a voice channel!
    private void joinHelper(SlashCommandInteractionEvent event, ChatMessage message) {
        // If the bot is already in a voice channel, return
        if (event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            return;
        }

        // Else join the voice channel of the user

        final Member member = event.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (memberVoiceState.inAudioChannel()) {
            event.getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
        }

        GuildMusicManager.getInstance(event.getGuild().getIdLong());

        message.edit("Joining your channel!");
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
