package de.qStivi.commands;

import net.dv8tion.jda.api.entities.Emoji;
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
        var hoock = event.getHook();
        var sm = SelectionMenu.create("myID");
        sm.addOption("Label", "Value", "Desc.", Emoji.fromMarkdown(":upside_down:"));
        sm.addOption("yee", "yee", "yee", Emoji.fromMarkdown(":yee:"));
        sm.setPlaceholder("Placeholder");
        hoock.editOriginalComponents().setActionRow(sm.build()).queue();
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
