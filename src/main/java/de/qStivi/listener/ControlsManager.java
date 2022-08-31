package de.qStivi.listener;

import de.qStivi.audio.PlayerManager;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ControlsManager extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        var type = Objects.requireNonNull(event.getButton()).getId();
        var guild = event.getGuild();
        switch (Objects.requireNonNull(type)) {
            case "play" -> PlayerManager.unpause(guild);
            case "pause" -> PlayerManager.pause(guild);
            case "stop" -> PlayerManager.stop(guild);
            case "repeat" -> PlayerManager.setRepeat(guild, !PlayerManager.isRepeating(guild));
            case "skip" -> PlayerManager.skip(guild);
        }
        if (PlayerManager.isRepeating(guild)) {
            if (!event.getMessage().getContentRaw().toLowerCase().contains("repeat")) event.getHook().editOriginal(event.getMessage().getContentRaw() + "\n\uD83D\uDD01 **__REPEATING__** \uD83D\uDD01").queue();
        } else {
            var link = event.getMessage().getContentRaw().replaceAll("\n\uD83D\uDD01 \\*\\*__REPEATING__\\*\\* \uD83D\uDD01", "");
            event.getHook().editOriginal(link).queue();
        }
        event.deferEdit().queue();
    }
}
