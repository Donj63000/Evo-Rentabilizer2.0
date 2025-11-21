package com.dofus.rentabilizer.service;

import com.dofus.rentabilizer.db.Database;
import com.dofus.rentabilizer.domain.SessionRecord;
import com.dofus.rentabilizer.domain.ZoneStatsRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SessionService {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void addSession(String zoneName, int minutes, long kamas, String position, LocalDateTime endedAt) throws SQLException {
        if (minutes <= 0) {
            throw new IllegalArgumentException("La duree doit etre superieure a 0");
        }
        if (kamas < 0) {
            throw new IllegalArgumentException("Les kamas doivent etre positifs");
        }
        String normalizedPosition = normalizePosition(position);

        LocalDateTime start = endedAt.minusMinutes(minutes);
        long zoneId = Database.upsertZone(zoneName);

        String sql = """
                INSERT INTO sessions(zone_id, started_at, ended_at, duration_minutes, kamas_total, position)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.newConnectionWithForeignKeys();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, zoneId);
            ps.setString(2, start.format(ISO));
            ps.setString(3, endedAt.format(ISO));
            ps.setInt(4, minutes);
            ps.setLong(5, kamas);
            ps.setString(6, normalizedPosition);
            ps.executeUpdate();
        }
    }

    public List<SessionRecord> latestSessions(int limit) throws SQLException {
        if (limit <= 0) {
            throw new IllegalArgumentException("Le nombre demande doit etre > 0");
        }

        String sql = """
                SELECT s.id,
                       z.name AS zone_name,
                       s.position,
                       s.started_at,
                       s.ended_at,
                       s.duration_minutes,
                       s.kamas_total
                FROM sessions s
                JOIN zones z ON z.id = s.zone_id
                ORDER BY s.ended_at DESC, s.id DESC
                LIMIT ?;
                """;

        List<SessionRecord> sessions = new ArrayList<>();
        try (Connection connection = Database.newConnectionWithForeignKeys();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sessions.add(new SessionRecord(
                            rs.getLong("id"),
                            rs.getString("zone_name"),
                            rs.getString("position"),
                            rs.getString("started_at"),
                            rs.getString("ended_at"),
                            rs.getInt("duration_minutes"),
                            rs.getLong("kamas_total")
                    ));
                }
            }
        }
        return sessions;
    }

    public List<ZoneStatsRecord> zoneStats() throws SQLException {
        String query = """
                SELECT z.name AS zone,
                       COUNT(s.id) AS sessions_count,
                       SUM(s.duration_minutes) AS minutes_total,
                       SUM(s.kamas_total) AS kamas_total,
                       (SUM(s.kamas_total) * 60.0) / SUM(s.duration_minutes) AS kph_avg,
                       MAX(s.ended_at) AS last_session
                FROM sessions s
                JOIN zones z ON z.id = s.zone_id
                GROUP BY z.name
                ORDER BY kph_avg DESC;
                """;

        List<ZoneStatsRecord> stats = new ArrayList<>();
        try (Connection connection = Database.newConnectionWithForeignKeys();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                stats.add(new ZoneStatsRecord(
                        rs.getString("zone"),
                        rs.getInt("sessions_count"),
                        rs.getLong("minutes_total"),
                        rs.getLong("kamas_total"),
                        rs.getDouble("kph_avg"),
                        rs.getString("last_session")
                ));
            }
        }
        return stats;
    }

    public List<String> knownZones(int limit) throws SQLException {
        if (limit <= 0) {
            throw new IllegalArgumentException("Le nombre de zones demande doit etre > 0");
        }

        String sql = """
                SELECT z.name AS zone_name,
                       MAX(s.ended_at) AS last_session
                FROM zones z
                LEFT JOIN sessions s ON s.zone_id = z.id
                GROUP BY z.id
                ORDER BY last_session DESC, z.name COLLATE NOCASE ASC
                LIMIT ?;
                """;

        List<String> zones = new ArrayList<>();
        try (Connection connection = Database.newConnectionWithForeignKeys();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    zones.add(rs.getString("zone_name"));
                }
            }
        }
        return zones;
    }

    public void deleteSession(long sessionId) throws SQLException {
        try (Connection connection = Database.newConnectionWithForeignKeys();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM sessions WHERE id = ?")) {
            ps.setLong(1, sessionId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Session introuvable: " + sessionId);
            }
        }
    }

    public void updateSession(long sessionId, String zoneName, int minutes, long kamas, String position, LocalDateTime endedAt) throws SQLException {
        if (minutes <= 0) {
            throw new IllegalArgumentException("La duree doit etre superieure a 0");
        }
        if (kamas < 0) {
            throw new IllegalArgumentException("Les kamas doivent etre positifs");
        }
        if (zoneName == null || zoneName.trim().isEmpty()) {
            throw new IllegalArgumentException("La zone est obligatoire");
        }
        String normalizedPosition = normalizePosition(position);
        LocalDateTime start = endedAt.minusMinutes(minutes);
        long zoneId = Database.upsertZone(zoneName);

        String sql = """
                UPDATE sessions
                SET zone_id = ?, started_at = ?, ended_at = ?, duration_minutes = ?, kamas_total = ?, position = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.newConnectionWithForeignKeys();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, zoneId);
            ps.setString(2, start.format(ISO));
            ps.setString(3, endedAt.format(ISO));
            ps.setInt(4, minutes);
            ps.setLong(5, kamas);
            ps.setString(6, normalizedPosition);
            ps.setLong(7, sessionId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Session introuvable: " + sessionId);
            }
        }
    }

    public LocalDateTime parseIsoDateTime(String iso) {
        try {
            return LocalDateTime.parse(iso, ISO);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Horodatage invalide pour la session: " + iso, e);
        }
    }

    private String normalizePosition(String position) {
        if (position == null) {
            return null;
        }
        String trimmed = position.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String compact = trimmed.replaceAll("\\s+", "");
        if (!compact.matches("-?\\d+,-?\\d+")) {
            throw new IllegalArgumentException("Position invalide (format attendu: x,y)");
        }
        return compact;
    }
}
