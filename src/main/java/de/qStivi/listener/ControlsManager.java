package de.qStivi.listener;

import de.qStivi.audio.PlayerManager;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ControlsManager extends ListenerAdapter {
    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
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
            event.getHook().editOriginal(event.getMessage().getContentRaw() + "\nCurrent song **__IS__** currently being **__REPEATED__**!").queue();
        } else {
            var link = event.getMessage().getContentRaw().replaceAll("\nCurrent song \\*\\*__IS__\\*\\* currently being \\*\\*__REPEATED__\\*\\*!", "");
            event.getHook().editOriginal(link).queue();
        }
        event.deferEdit().queue();
    }
}
