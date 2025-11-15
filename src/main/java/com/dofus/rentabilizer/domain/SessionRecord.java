package com.dofus.rentabilizer.domain;

public record SessionRecord(
        long id,
        String zoneName,
        String startedAtIso,
        String endedAtIso,
        int durationMinutes,
        long kamasTotal
) {

    public double kamasPerHour() {
        return durationMinutes == 0 ? 0 : (kamasTotal * 60.0) / durationMinutes;
    }
}
