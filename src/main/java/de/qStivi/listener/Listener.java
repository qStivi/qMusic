package de.qStivi.listener;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import static org.slf4j.LoggerFactory.getLogger;

public class Listener extends ListenerAdapter {

    private static final Logger logger = getLogger(Listener.class);

    @SuppressWarnings({"DuplicatedCode"})
    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {


        new Thread(() -> {

            var channelID = event.getChannel().getIdLong();
            var channel = event.getChannel();
            var parent = channel.getParent();
            var categoryID = parent == null ? 0 : parent.getIdLong();
            var author = event.getAuthor();

            if (author.isBot()) return;
            if (event.isWebhookMessage()) return;

            String messageRaw = event.getMessage().getContentRaw();

        /*
        Reactions
         */
            if (messageRaw.toLowerCase().startsWith("ree")) {
                String[] words = messageRaw.split("\\s+");
                String ree = words[0];
                String ees = ree.substring(1);
                channel.sendMessage(ree + ees + ees).queue();
            }

            if (messageRaw.toLowerCase().startsWith("hmm")) {
                event.getMessage().addReaction("U+1F914").queue();
            }

        }).start();
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        new Thread(() -> {

            var channelID = event.getChannel().getIdLong();
            var channel = event.getChannel();
            var parent = channel.getParent();
            var categoryID = parent == null ? 0 : parent.getIdLong();
            var reactingUser = event.getUser();

        /*
        Why is this so stupid!?
        Also there has to be a better way. At least regarding the AtomicReference...
         */
            AtomicReference<User> messageAuthor = new AtomicReference<>();
            event.retrieveMessage().queue(message -> messageAuthor.set(message.getAuthor()));
            while (messageAuthor.get() == null) Thread.onSpinWait();

        }).start();
    }
}

class Task {
    TimerTask timerTask;
    Long id;

    public Task(TimerTask timerTask, Long id) {
        this.timerTask = timerTask;
        this.id = id;
    }
}