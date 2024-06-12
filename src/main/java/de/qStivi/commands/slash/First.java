package de.qStivi.commands.slash;

import de.qStivi.ChatMessage;
import de.qStivi.Lavalink;
import de.qStivi.audio.AudioLoader;
import de.qStivi.commands.ICommand;
import dev.arbjerg.lavalink.client.player.Track;
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
import java.util.concurrent.TimeUnit;

public class First implements ICommand<SlashCommandInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(First.class);
    private static final String QUERY = "query";

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.STRING, QUERY, "The thing you want to play (search or link)", true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().complete();
        var query = Objects.requireNonNull(event.getOption(QUERY)).getAsString();

        // Try parsing as URL and prepend "ytsearch:" if it fails
        try {
            new java.net.URL(query);
        } catch (java.net.MalformedURLException e) {
            query = "ytsearch:" + query;
        }

        var guild = event.getGuild();

        joinHelper(event);

        var al = AudioLoader.getInstance(guild.getIdLong());
                al.shouldSkipQueue(true);

        Lavalink.get(guild.getIdLong()).loadItem(query).subscribe(al);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        event.getHook().editOriginal("Added song to queue! Here are the next 10 songs in the queue.").queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));

        StringBuilder sb = new StringBuilder();

        var i = 0;
        for (Track audioTrack : AudioLoader.getInstance(event.getGuild().getIdLong()).mngr.scheduler.queue) {
            sb.append(i++).append(". ").append("`").append(audioTrack.getInfo().getTitle()).append("`").append(" by ").append("`").append(audioTrack.getInfo().getAuthor()).append("`").append("\n");
            if (i == 10) {
                sb.append("...");
                break;
            }
        }

        event.getChannel().sendMessage(sb.toString()).queue((msg) -> msg.delete().queueAfter(15, TimeUnit.MINUTES));
    }

    // Makes sure that the bot is in a voice channel!
    private void joinHelper(SlashCommandInteractionEvent event) {
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

        ChatMessage.getInstance(event.getHook()).edit("Joining your channel!");
    }

    @NotNull
    @Override
    public String getName() {
        return "first";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Puts your song in the front of the queue.";
    }
}