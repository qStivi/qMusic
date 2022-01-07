package de.qStivi.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;

public class TestCommand implements ICommand {
    @NotNull
    @Override
    public CommandData getCommand() {
        return new CommandData(getName(), getDescription());
    }

    @Override
    public void handle(SlashCommandEvent event) {
        var hook = event.getHook();
        hook.sendMessage("test").complete();
        var sm = SelectionMenu.create("myID");
        sm.addOption("Label", "Value", "Desc.");
        sm.addOption("yee", "yee", "yee");
        sm.setPlaceholder("Placeholder");
        hook.editOriginalComponents().setActionRow(sm.build()).queue();
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
