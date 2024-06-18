package de.qStivi.commands.context;

import de.qStivi.ChatMessage;
import de.qStivi.audio.AudioLoader;
import de.qStivi.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shutdown implements ICommand<UserContextInteractionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Shutdown.class);

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.user(getName());
    }

    @Override
    public void handle(UserContextInteractionEvent event) {
        event.reply("Shutting down...").setEphemeral(true).complete();
        event.getInteraction().getHook().deleteOriginal().complete();
        if (event.getJDA().getSelfUser().getId().equals(event.getTarget().getId()) && event.getUser().getId().equals("219108246143631364")) {
            AudioLoader.getInstance(event.getGuild().getIdLong()).mngr.stop();
            ChatMessage.getInstance().delete();
            event.getJDA().shutdown();
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "Shutdown";
    }
}
