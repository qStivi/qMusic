package de.qStivi.listener;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.sql.SQLException;

public interface ISlashCommandEvent {
    void handle(SlashCommandEvent event) throws SQLException, ClassNotFoundException, InterruptedException;
}
