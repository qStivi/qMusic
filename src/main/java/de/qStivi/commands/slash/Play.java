package de.qStivi.commands.slash;

import de.qStivi.ChatMessage;
import de.qStivi.Lavalink;
import de.qStivi.NoResultsException;
import de.qStivi.audio.AudioLoader;
import de.qStivi.commands.ICommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class Play implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Play.class);
    private static final String QUERY = "query";

    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription()).addOption(OptionType.STRING, QUERY, "The thing you want to play (search or link)", true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) throws NoResultsException, IOException {
        try {
            event.deferReply().complete();
            if (!ChatMessage.isInstanceNull()) event.getHook().deleteOriginal().queue();

            var query = Objects.requireNonNull(event.getOption(QUERY)).getAsString();
            query = validateQuery(query);

            var guild = event.getGuild();
            joinHelper(event);

            var al = AudioLoader.getInstance(guild.getIdLong());
            Lavalink.getLink(guild.getIdLong()).loadItem(query).subscribe(al);

            ChatMessage.getInstance(event).edit("Loading your song!");
        } catch (Exception

                e) {
            LOGGER.error("Error handling play command", e);
            event.getHook().editOriginal("Failed to play the song.").queue();
        }
    }

    private String validateQuery(String query) {
        try {
            new java.net.URL(query);
        } catch (java.net.MalformedURLException e) {
            query = "ytsearch:" + query;
        }
        return query;
    }

    private void joinHelper(SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            return;
        }

        if (memberVoiceState.inAudioChannel()) {
            event.getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
        }

        ChatMessage.getInstance(event).edit("Joining your channel!");
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