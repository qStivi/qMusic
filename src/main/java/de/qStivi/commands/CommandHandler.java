package de.qStivi.commands;

import de.qStivi.listener.ISlashCommandEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class CommandHandler implements ISlashCommandEvent {
    private static final Logger logger = getLogger(CommandHandler.class);

    public static final List<ICommand> COMMAND_LIST = new ArrayList<>();
    public final BlockingQueue<Command> queue = new LinkedBlockingQueue<>();

    public CommandHandler() {
        logger.debug("Registering commands.");
        COMMAND_LIST.add(new StopCommand());
        COMMAND_LIST.add(new ContinueCommand());
        COMMAND_LIST.add(new PauseCommand());
        COMMAND_LIST.add(new RepeatCommand());
        COMMAND_LIST.add(new SkipCommand());
        COMMAND_LIST.add(new JoinCommand());
        COMMAND_LIST.add(new LeaveCommand());
        COMMAND_LIST.add(new PlayCommand());

        var timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                var thread = new Thread(() -> {
                    try {
                        var command = queue.poll(1, TimeUnit.SECONDS);
                        if (command != null) command.handle();
                    } catch (SQLException | ClassNotFoundException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }
        }, 0, 500);
    }

    @Override
    public void handle(@NotNull SlashCommandEvent event) throws SQLException, ClassNotFoundException, InterruptedException {

        for (var command : COMMAND_LIST) {
            if (command.getCommand().getName().equals(event.getName())) {

                queue.put(new Command(command, event));
                logger.debug("Command queued.");

            }
        }
    }
}
