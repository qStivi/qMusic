package de.qStivi;

import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordListeners extends ListenerAdapter {


    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        super.onShutdown(event);
        System.exit(0);
    }
}
