package de.qStivi.listener;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.qStivi.audio.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ControlsManager extends ListenerAdapter {
    private static ControlsManager INSTANCE;
    private Timer timer;
    private Message msg;

    private ControlsManager() {
    }

    public static ControlsManager getINSTANCE() {

        if (INSTANCE == null) {
            INSTANCE = new ControlsManager();
        }

        return INSTANCE;
    }

    public void sendMessage(SlashCommandEvent event, Guild guild) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        if (msg != null) {
            try {
                msg.delete().complete();
                msg = null;
            } catch (Exception ignored) {
            }
        }
        // msg = event;

        msg = event.getTextChannel().sendMessage("Playing...").complete();
        msg.addReaction("▶").queue();
        msg.addReaction("⏸").queue();
        msg.addReaction("⏹").queue();
        msg.addReaction("\uD83D\uDD02").queue();
        msg.addReaction("⏭").queue();

        timer = new Timer();
        timer.schedule(task(guild), 2000, 2000);
    }

    private TimerTask task(Guild guild) {
        return new TimerTask() {
            @Override
            public void run() {
                Random rand = new Random();
                final float hue = rand.nextFloat();
                // Saturation between 0.1 and 0.5
                final float saturation = (rand.nextInt(5000) + 1000) / 10000f;
                final float luminance = 0.9f;
                final Color color = Color.getHSBColor(hue, saturation, luminance);

                AudioTrack track = PlayerManager.getINSTANCE().getMusicManager(guild).audioPlayer.getPlayingTrack();
                if (track == null) {
                    timer.cancel();
                    msg.delete().complete();
                    return;
                }
                var id = track.getIdentifier();
                var totalTime = track.getDuration();
                var timeRemaining = track.getPosition();
                var name = track.getInfo().title;
                var interpret = track.getInfo().author;

                DateFormat formatter = new SimpleDateFormat("mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                String timeRemainingFormatted = formatter.format(timeRemaining);
                String totalTimeFormatted = formatter.format(totalTime);
                if (totalTime > 3600000) {
                    totalTimeFormatted = "Too long bitch!";
                }

                EmbedBuilder embed = new EmbedBuilder();

                embed.setColor(color)
                        .setAuthor(interpret)
                        .setTitle(name, "https://youtu.be/" + id)
                        .setDescription(timeRemainingFormatted + "o-------------------------------------------------" + totalTimeFormatted);

                if (PlayerManager.getINSTANCE().isRepeating(guild)) {
                    embed.setFooter("Currently repeating");
                } else {
                    embed.setFooter(null);
                }

                msg.editMessage(embed.build()).queue();
            }
        };
    }

    @Override
    public void onGenericMessageReaction(@NotNull GenericMessageReactionEvent event) {
        if (event.getUser() == null) return;
        if (event.getUser().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        if (msg == null) return;
        if (event.getMessageIdLong() == msg.getIdLong())
            if (!event.getUser().isBot()) {

                if (event.getReactionEmote().getEmoji().equals("⏸")) {
                    PlayerManager.getINSTANCE().pause(event.getGuild());
                }

                if (event.getReactionEmote().getEmoji().equals("▶")) {
                    PlayerManager.getINSTANCE().continueTrack(event.getGuild());
                }

                if (event.getReactionEmote().getEmoji().equals("⏹")) {
                    PlayerManager.getINSTANCE().clearQueue(event.getGuild());
                    PlayerManager.getINSTANCE().skip(event.getGuild());
                }

                if (event.getReactionEmote().getEmoji().equals("\uD83D\uDD02")) {
                    PlayerManager.getINSTANCE().setRepeat(event.getGuild(), !PlayerManager.getINSTANCE().isRepeating(event.getGuild()));
                }

                if (event.getReactionEmote().getEmoji().equals("⏭")) {
                    PlayerManager.getINSTANCE().skip(event.getGuild());
                }
            }
    }
}
