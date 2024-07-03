package de.qStivi;

import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordListeners extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordListeners.class);

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        LOGGER.info("Shutdown event received. Exiting application...");
        System.exit(0);
    }
}