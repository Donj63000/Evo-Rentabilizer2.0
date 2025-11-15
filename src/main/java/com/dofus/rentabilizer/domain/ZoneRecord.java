package com.dofus.rentabilizer.domain;

public record ZoneRecord(
        long id,
        String name,
        String server,
        String notes
) {
}
