package com.dofus.rentabilizer;

import com.dofus.rentabilizer.cli.AddSessionCommand;
import com.dofus.rentabilizer.cli.HistoryCommand;
import com.dofus.rentabilizer.cli.StatsCommand;
import com.dofus.rentabilizer.db.Database;
import com.dofus.rentabilizer.ui.MainWindow;
import picocli.CommandLine;

import javax.swing.*;

@CommandLine.Command(
        name = "dr",
        mixinStandardHelpOptions = true,
        version = "Dofus-Rentabilizer 0.2.0",
        description = "Suivi de la rentabilite en kamas/heure selon vos sessions de farm",
        subcommands = {
                AddSessionCommand.class,
                StatsCommand.class,
                HistoryCommand.class
        }
)
public final class Main implements Runnable {

    public static void main(String[] args) {
        Database.init();
        if (args != null && args.length > 0) {
            int exitCode = new CommandLine(new Main()).execute(args);
            System.exit(exitCode);
        } else {
            SwingUtilities.invokeLater(() -> {
                MainWindow window = new MainWindow();
                window.setVisible(true);
            });
        }
    }

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }
}
