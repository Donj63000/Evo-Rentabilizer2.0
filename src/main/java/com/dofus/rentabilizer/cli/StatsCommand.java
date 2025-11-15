package com.dofus.rentabilizer.cli;

import com.dofus.rentabilizer.domain.ZoneStatsRecord;
import com.dofus.rentabilizer.service.SessionService;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(
        name = "stats",
        description = "Affiche les statistiques agregees par zone (sessions, temps, kamas, ratio K/h)"
)
public class StatsCommand implements Runnable {
    private final SessionService sessionService = new SessionService();

    @Override
    public void run() {
        try {
            List<ZoneStatsRecord> stats = sessionService.zoneStats();
            System.out.printf("%-28s %8s %14s %14s %10s%n",
                    "Zone", "Sess.", "Min. total", "Kamas total", "K/h");
            System.out.println("--------------------------------------------------------------------------------------");
            for (ZoneStatsRecord stat : stats) {
                System.out.printf("%-28s %8d %14d %14d %10.0f%n",
                        stat.zoneName(),
                        stat.sessionCount(),
                        stat.totalMinutes(),
                        stat.totalKamas(),
                        stat.averageKamasPerHour());
            }
        } catch (Exception e) {
            throw new CommandLine.ExecutionException(new CommandLine(this),
                    "Impossible de recuperer les statistiques: " + e.getMessage(), e);
        }
    }
}
