package de.qStivi.listener;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlsManager extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlsManager.class);

//    @Override
//    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
//        event.deferEdit().queue();
//        var type = Objects.requireNonNull(event.getButton()).getId();
//        var guild = event.getGuild();
//        var lavaPlayer = QPlayer.getInstance(guild);
//        switch (Objects.requireNonNull(type)) {
//            case "play" -> lavaPlayer.resume();
//            case "pause" -> lavaPlayer.pause();
//            case "stop" -> lavaPlayer.stop();
//            case "repeat" -> lavaPlayer.toggleRepeat();
//            case "skip" -> lavaPlayer.skip(event);
//        }
////        if (lavaPlayer.isRepeating()) {
////            if (!event.getMessage().getContentRaw().toLowerCase().contains("repeat")) event.getHook().editOriginal(event.getMessage().getContentRaw() + "\n\uD83D\uDD01 **__REPEATING__** \uD83D\uDD01").queue();
////        } else {
////            var link = event.getMessage().getContentRaw().replaceAll("\n\uD83D\uDD01 \\*\\*__REPEATING__\\*\\* \uD83D\uDD01", "");
////            event.getHook().editOriginal(link).queue();
////        }
//    }
}
