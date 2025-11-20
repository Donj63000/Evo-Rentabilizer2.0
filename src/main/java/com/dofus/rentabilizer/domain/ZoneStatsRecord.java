package com.dofus.rentabilizer.domain;

public record ZoneStatsRecord(
        String zoneName,
        int sessionCount,
        long totalMinutes,
        long totalKamas,
        double averageKamasPerHour,
        String lastSessionIso
) {
}
