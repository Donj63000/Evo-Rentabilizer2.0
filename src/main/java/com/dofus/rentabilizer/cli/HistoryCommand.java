package com.dofus.rentabilizer.cli;

import com.dofus.rentabilizer.domain.SessionRecord;
import com.dofus.rentabilizer.service.SessionService;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@CommandLine.Command(
        name = "history",
        description = "Liste les dernieres sessions enregistrees"
)
public class HistoryCommand implements Runnable {
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
            System.out.printf("%-5s %-24s %-10s %-19s %-19s %8s %12s %8s %-32s%n",
                    "ID", "Zone", "Pos", "Debut", "Fin", "Min", "Kamas", "K/h", "Note");
            System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
            for (SessionRecord session : sessions) {
                System.out.printf("%-5d %-24s %-10s %-19s %-19s %8d %12d %8.0f %-32s%n",
                        session.id(),
                        session.zoneName(),
                        session.position() == null ? "-" : session.position(),
                        formatDate(session.startedAtIso()),
                        formatDate(session.endedAtIso()),
                        session.durationMinutes(),
                        session.kamasTotal(),
                        session.kamasPerHour(),
                        formatNote(session.note()));
            }
        } catch (Exception e) {
            throw new CommandLine.ExecutionException(new CommandLine(this),
                    "Impossible de recuperer l'historique: " + e.getMessage(), e);
        }
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isBlank()) {
            return "-";
        }
        try {
            return LocalDateTime.parse(iso).format(DISPLAY_DATE);
        } catch (Exception e) {
            return iso;
        }
    }

    private String formatNote(String note) {
        if (note == null || note.isBlank()) {
            return "-";
        }
        String trimmed = note.trim();
        if (trimmed.length() > 32) {
            return trimmed.substring(0, 29) + "...";
        }
        return trimmed;
    }
}
