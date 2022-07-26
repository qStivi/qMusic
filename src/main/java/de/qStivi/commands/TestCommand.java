package de.qStivi.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class TestCommand implements ICommand {

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
//        var hook = event.getHook();
//        hook.sendMessage("test").complete();
//        var sm = SelectionMenu.create("myID");
//        sm.addOption("Label", "Value", "Desc.");
//        sm.addOption("yee", "yee", "yee");
//        sm.setPlaceholder("Placeholder");
//        hook.editOriginalComponents().setActionRow(sm.build()).queue();
    }

    @NotNull
    @Override
    public String getName() {
        return "test";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Test";
    }
}
