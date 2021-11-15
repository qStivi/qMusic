package de.qStivi.listener;

import de.qStivi.commands.CommandHandler;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventsPreprocessor extends ListenerAdapter {
    public static final List<ISlashCommandEvent> SLASH_COMMAND_EVENTS = new ArrayList<>();
    public static final List<IButtonClickEvent> BUTTON_CLICK_EVENTS = new ArrayList<>();

    public EventsPreprocessor() {
        BUTTON_CLICK_EVENTS.add(new ButtonClickEventHandler());
        SLASH_COMMAND_EVENTS.add(new CommandHandler());
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        try {
            if (!isUserPermitted(event)) return;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        BUTTON_CLICK_EVENTS.forEach(iButtonClickEvent -> iButtonClickEvent.handle(event));
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        try {
            if (!isUserPermitted(event)) return;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        SLASH_COMMAND_EVENTS.forEach(iSlashCommandEvent -> {
            try {
                iSlashCommandEvent.handle(event);
            } catch (SQLException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean isUserPermitted(@NotNull SlashCommandEvent event) throws SQLException, ClassNotFoundException {
        return !event.getUser().isBot();
    }

    private boolean isUserPermitted(@NotNull ButtonClickEvent event) throws SQLException, ClassNotFoundException {
        var author = event.getUser();
        return !author.isBot();
    }
}
