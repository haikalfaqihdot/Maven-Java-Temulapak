package com.temulapak.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Memastikan database PostgreSQL tersedia sebelum Spring Boot dijalankan.
 * Nilai default sama dengan application.properties (postgres / admin).
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void ensureDatabaseExists() {
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "5432");
        String name = env("DB_NAME", "temulapak");
        String user = env("DB_USERNAME", "postgres");
        String pass = env("DB_PASSWORD", "admin");

        if (!name.matches("[A-Za-z0-9_]+")) {
            System.err.println("[TemuLapak] Nama database tidak valid: " + name);
            return;
        }

        String url = "jdbc:postgresql://" + host + ":" + port + "/postgres";
        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            boolean ada;
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    ada = rs.next();
                }
            }
            if (!ada) {
                try (Statement st = c.createStatement()) {
                    st.executeUpdate("CREATE DATABASE \"" + name + "\"");
                }
                System.out.println("[TemuLapak] Database '" + name + "' berhasil dibuat.");
            }
        } catch (SQLException e) {
            System.err.println("[TemuLapak] Tidak dapat memeriksa/membuat database: " + e.getMessage());
            System.err.println("[TemuLapak] Pastikan PostgreSQL berjalan di " + host + ":" + port
                    + " dengan user '" + user + "' dan password yang sesuai.");
        }
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}
