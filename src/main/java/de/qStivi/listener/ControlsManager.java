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
        var pm = PlayerManager.getINSTANCE();
        switch (Objects.requireNonNull(type)) {
            case "play" -> pm.unpause(guild);
            case "pause" -> pm.pause(guild);
            case "stop" -> pm.stop(guild);
            case "repeat" -> pm.setRepeat(guild, !pm.isRepeating(guild));
            case "skip" -> pm.skip(guild);
        }
        if (pm.isRepeating(guild)) {
            if (!event.getMessage().getContentRaw().toLowerCase().contains("repeat")) event.getHook().editOriginal(event.getMessage().getContentRaw() + "\n\uD83D\uDD01 **__REPEATING__** \uD83D\uDD01").queue();
        } else {
            var link = event.getMessage().getContentRaw().replaceAll("\n\uD83D\uDD01 \\*\\*__REPEATING__\\*\\* \uD83D\uDD01", "");
            event.getHook().editOriginal(link).queue();
        }
        event.deferEdit().queue();
    }
}
