package de.qStivi.listener;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class ButtonClickEventHandler implements IButtonClickEvent {
    private static final Logger logger = getLogger(ButtonClickEventHandler.class);

    @Override
    public void handle(@NotNull ButtonClickEvent event) {
        event.deferEdit().complete();
        var button = event.getButton();
        var buttonIdAndPrefix = button.getId();
        var prefix = buttonIdAndPrefix.split(" ")[0];
        var buttonId = buttonIdAndPrefix.split(" ")[1];
        var user = event.getUser();
        var UserID = user.getIdLong();
        var hook = event.getHook();

        logger.info("Button \"" + buttonIdAndPrefix + "\" was clicked by " + user.getAsTag());

        if (prefix.equals("test")) {
            // Do shit
        }
    }
}
