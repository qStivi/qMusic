package de.qStivi.listener;

import de.qStivi.commands.STTSlashCommand;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionListener extends ListenerAdapter {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getMember().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            if (event.getChannelLeft() != null) {
                LOGGER.info("Left channel: " + event.getChannelLeft().getName());
            }
            if (event.getChannelJoined() != null) {
                LOGGER.info("Joined channel: " + event.getChannelJoined().getName());
            }
            // TODO reset all audio stuff
        }
    }
}
