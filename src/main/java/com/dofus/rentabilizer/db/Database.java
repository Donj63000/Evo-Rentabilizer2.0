package com.dofus.rentabilizer.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final Path DB_DIR = Paths.get(System.getProperty("user.home"), ".dofus-rentabilizer");
    private static final Path DB_FILE = DB_DIR.resolve("data.db");

    private Database() {
    }

    public static void init() {
        try {
            Files.createDirectories(DB_DIR);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de creer le dossier de donnees " + DB_DIR, e);
        }

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS zones (
                      id     INTEGER PRIMARY KEY AUTOINCREMENT,
                      name   TEXT NOT NULL UNIQUE,
                      server TEXT,
                      notes  TEXT
                    );
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS sessions (
                      id                INTEGER PRIMARY KEY AUTOINCREMENT,
                      zone_id           INTEGER NOT NULL,
                      started_at        TEXT NOT NULL,
                      ended_at          TEXT NOT NULL,
                      duration_minutes  INTEGER NOT NULL CHECK (duration_minutes > 0),
                      kamas_total       INTEGER NOT NULL CHECK (kamas_total >= 0),
                      FOREIGN KEY(zone_id) REFERENCES zones(id) ON DELETE CASCADE
                    );
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_sessions_zone ON sessions(zone_id)");
        } catch (SQLException e) {
            throw new IllegalStateException("Initialisation de la base de donnees impossible", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:" + DB_FILE;
        return DriverManager.getConnection(url);
    }

    public static long upsertZone(String zoneName) throws SQLException {
        String trimmed = zoneName == null ? "" : zoneName.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Le nom de zone est obligatoire");
        }

        try (Connection connection = getConnection()) {
            try (PreparedStatement find = connection.prepareStatement("SELECT id FROM zones WHERE name = ?")) {
                find.setString(1, trimmed);
                try (ResultSet rs = find.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }

            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO zones(name) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, trimmed);
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                }
            }
        }

        throw new SQLException("Impossible de creer/recuperer la zone " + zoneName);
    }

    public static Connection newConnectionWithForeignKeys() throws SQLException {
        Connection connection = getConnection();
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }
}
