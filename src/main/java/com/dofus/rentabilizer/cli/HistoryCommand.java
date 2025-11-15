package com.dofus.rentabilizer.cli;

import com.dofus.rentabilizer.domain.SessionRecord;
import com.dofus.rentabilizer.service.SessionService;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(
        name = "history",
        description = "Liste les dernieres sessions enregistrees"
)
public class HistoryCommand implements Runnable {

    @CommandLine.Option(names = {"-n", "--limit"}, defaultValue = "20", description = "Nombre de lignes a afficher")
    private int limit;

    private final SessionService sessionService = new SessionService();

    @Override
    public void run() {
        if (limit <= 0) {
            throw new CommandLine.ParameterException(new CommandLine(this),
                    "Le nombre de lignes doit etre superieur a 0");
        }

        try {
            List<SessionRecord> sessions = sessionService.latestSessions(limit);
            System.out.printf("%-5s %-28s %-19s %-19s %8s %12s %8s%n",
                    "ID", "Zone", "Debut", "Fin", "Min", "Kamas", "K/h");
            System.out.println("-----------------------------------------------------------------------------------------------");
            for (SessionRecord session : sessions) {
                System.out.printf("%-5d %-28s %-19s %-19s %8d %12d %8.0f%n",
                        session.id(),
                        session.zoneName(),
                        session.startedAtIso(),
                        session.endedAtIso(),
                        session.durationMinutes(),
                        session.kamasTotal(),
                        session.kamasPerHour());
            }
        } catch (Exception e) {
            throw new CommandLine.ExecutionException(new CommandLine(this),
                    "Impossible de recuperer l'historique: " + e.getMessage(), e);
        }
    }
}
