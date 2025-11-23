package com.dofus.rentabilizer.cli;

import com.dofus.rentabilizer.service.SessionService;
import picocli.CommandLine;

@CommandLine.Command(
        name = "delete-session",
        description = "Supprime une session enregistree par son identifiant"
)
public class DeleteSessionCommand implements Runnable {

    @CommandLine.Parameters(paramLabel = "SESSION_ID", description = "Identifiant de la session a supprimer")
    private long sessionId;

    private final SessionService sessionService = new SessionService();

    @Override
    public void run() {
        try {
            sessionService.deleteSession(sessionId);
            System.out.printf("Session #%d supprimee.%n", sessionId);
        } catch (Exception e) {
            throw new CommandLine.ExecutionException(new CommandLine(this),
                    "Impossible de supprimer la session: " + e.getMessage(), e);
        }
    }
}
