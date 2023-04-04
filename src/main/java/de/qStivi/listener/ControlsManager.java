package de.qStivi.listener;

import de.qStivi.audio.LavaPlayer;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ControlsManager extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlsManager.class);

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        var type = Objects.requireNonNull(event.getButton()).getId();
        var guild = event.getGuild();
        switch (Objects.requireNonNull(type)) {
            case "play" -> LavaPlayer.resume(guild);
            case "pause" -> LavaPlayer.pause(guild);
            case "stop" -> LavaPlayer.stop(guild);
            case "repeat" -> LavaPlayer.toggleRepeat(guild);
            case "skip" -> LavaPlayer.skip(event);
        }
        if (LavaPlayer.isRepeating(guild)) {
            if (!event.getMessage().getContentRaw().toLowerCase().contains("repeat")) event.getHook().editOriginal(event.getMessage().getContentRaw() + "\n\uD83D\uDD01 **__REPEATING__** \uD83D\uDD01").queue();
        } else {
            var link = event.getMessage().getContentRaw().replaceAll("\n\uD83D\uDD01 \\*\\*__REPEATING__\\*\\* \uD83D\uDD01", "");
            event.getHook().editOriginal(link).queue();
        }
        event.deferEdit().queue();
    }
}
