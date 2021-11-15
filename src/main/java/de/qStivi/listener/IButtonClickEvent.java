package de.qStivi.listener;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public interface IButtonClickEvent {
    void handle(ButtonClickEvent event);
}
