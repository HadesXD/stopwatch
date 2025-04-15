package com.example.diploma;

import java.io.File;
import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:stopwatch.db";

    public DatabaseManager() {
        printDbPath();
        createTableIfNotExists();
    }

    // Save a time entry
    public void saveEntry(String duration, String description) {
        String sql = "INSERT INTO time_entries (duration, description) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, duration);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
            System.out.println("Entry saved successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ensure the table exists
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS time_entries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "duration TEXT NOT NULL," +
                "description TEXT)";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Checked/created table: time_entries");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Show DB file path
    private void printDbPath() {
        File dbFile = new File("stopwatch.db");
        System.out.println("Using DB at: " + dbFile.getAbsolutePath());
    }

    // Optional: Print all tables in the DB
    public void listTables() {
        try (Connection conn = DriverManager.getConnection(URL);
             ResultSet rs = conn.getMetaData().getTables(null, null, "%", null)) {
            System.out.println("Tables in DB:");
            while (rs.next()) {
                System.out.println("- " + rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



