package com.dofus.rentabilizer.domain;

public record SessionRecord(
        long id,
        String zoneName,
        String position,
        String startedAtIso,
        String endedAtIso,
        int durationMinutes,
        long kamasTotal,
        String note
) {

    public double kamasPerHour() {
        return durationMinutes == 0 ? 0 : (kamasTotal * 60.0) / durationMinutes;
    }
}
