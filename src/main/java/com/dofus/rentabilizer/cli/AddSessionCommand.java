package com.dofus.rentabilizer.cli;

import com.dofus.rentabilizer.service.SessionService;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@CommandLine.Command(
        name = "add",
        description = "Ajoute une session de farm (zone, minutes, kamas)"
)
public class AddSessionCommand implements Runnable {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final SessionService sessionService = new SessionService();

    @CommandLine.Option(names = {"-z", "--zone"}, required = true, description = "Nom de la zone (ex: Porcos)")
    private String zoneName;

    @CommandLine.Option(names = {"-m", "--minutes"}, required = true, description = "Duree de farm en minutes (> 0)")
    private int minutes;

    @CommandLine.Option(names = {"-k", "--kamas"}, required = true, description = "Valeur totale en kamas (>= 0)")
    private long kamas;

    @CommandLine.Option(names = {"--end"}, description = "Horodatage ISO de fin (par defaut: maintenant)")
    private String endTimeIso;

    @Override
    public void run() {
        validateArguments();
        LocalDateTime endedAt = parseEnd();

        try {
            sessionService.addSession(zoneName, minutes, kamas, endedAt);
            double kph = (kamas * 60.0) / minutes;
            System.out.printf("Session enregistree: %s | %d min | %d kamas | %.0f K/h%n",
                    zoneName, minutes, kamas, kph);
        } catch (Exception e) {
            throw new CommandLine.ExecutionException(new CommandLine(this),
                    "Impossible d'enregistrer la session: " + e.getMessage(), e);
        }
    }

    private void validateArguments() {
        if (minutes <= 0) {
            throw new CommandLine.ParameterException(new CommandLine(this),
                    "La duree en minutes doit etre superieure a 0");
        }
        if (kamas < 0) {
            throw new CommandLine.ParameterException(new CommandLine(this),
                    "Le nombre de kamas doit etre positif");
        }
        if (zoneName == null || zoneName.trim().isEmpty()) {
            throw new CommandLine.ParameterException(new CommandLine(this),
                    "Le nom de la zone est obligatoire");
        }
    }

    private LocalDateTime parseEnd() {
        if (endTimeIso == null || endTimeIso.isBlank()) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(endTimeIso.trim(), ISO);
    }
}
