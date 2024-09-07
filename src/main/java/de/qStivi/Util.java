package de.qStivi;

import de.qStivi.audio.AudioLoader;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.TimeUnit;

public class Util {
    // Makes sure that the bot is in a voice channel!
    public static void joinHelper(SlashCommandInteractionEvent event) {
        // Join the voice channel of the user if the bot is not in a voice channel
        if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {

            final Member member = event.getMember();
            final GuildVoiceState memberVoiceState = member.getVoiceState();

            if (memberVoiceState.inAudioChannel()) {
                event.getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
            }

            ChatMessage.getInstance(event).edit("Joining your channel!");
        }
    }

    public static void sendQueue(SlashCommandInteractionEvent event) {
        ChatMessage.getInstance(event).edit("Loading...");

        // Wait for song(s) to be loaded
        // TODO Do this in a better way
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        StringBuilder sb = new StringBuilder();

        var i = 0;
        for (Track audioTrack : AudioLoader.getInstance(event.getGuild().getIdLong()).mngr.scheduler.queue) {
            sb.append(i++).append(". ").append("`").append(audioTrack.getInfo().getTitle()).append("`").append(" by ").append("`").append(audioTrack.getInfo().getAuthor()).append("`").append("\n");
            if (i == 10) {
                sb.append("...");
                break;
            }
        }

        if (sb.isEmpty()) {
            sb.append("The queue is empty!");
        }

        event.getChannel().sendMessage(sb.toString()).queue((msg) -> msg.delete().queueAfter(3, TimeUnit.MINUTES));
    }
}
