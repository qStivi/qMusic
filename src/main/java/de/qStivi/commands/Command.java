package de.qStivi.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.sql.SQLException;

import static org.slf4j.LoggerFactory.getLogger;

public class Command {
    ICommand command;
    SlashCommandEvent event;

    public Command(ICommand command, SlashCommandEvent event) {
        this.command = command;
        this.event = event;
    }

    void handle() throws SQLException, ClassNotFoundException, InterruptedException {
        event.reply("Loading...").complete();

        this.command.handle(this.event);

        getLogger(CommandHandler.class).info(event.getUser().getAsTag() + " used /" + command.getName());

//        System.gc(); // Because Memory usage gets crazy after a while
    }
}