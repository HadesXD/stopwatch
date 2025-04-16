package com.example.diploma;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:stopwatch.db";

    public DatabaseManager() {
        printDbPath();
        createTableIfNotExists();
    }

    // Save a time entry
    public void saveEntry(String duration, String description) {
        String sql = "INSERT INTO time_entries (duration, description, date_created, last_modified) VALUES (?, ?, ?, ?)";
        String currentTimestamp = getCurrentTimestamp();

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, duration);
            pstmt.setString(2, description);
            pstmt.setString(3, currentTimestamp);
            pstmt.setString(4, currentTimestamp);
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
                "description TEXT," +
                "date_created TEXT NOT NULL," +
                "last_modified TEXT NOT NULL)";  // Added last_modified column

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

    // Fetch all entries from the database
    public List<Entry> getAllEntries() {
        List<Entry> entries = new ArrayList<>();
        String sql = "SELECT id, duration, description, date_created, last_modified FROM time_entries ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String duration = rs.getString("duration");
                String description = rs.getString("description");
                String dateCreated = rs.getString("date_created");
                String lastModified = rs.getString("last_modified");

                entries.add(new Entry(id, duration, description, dateCreated, lastModified));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return entries;
    }

    // Update the description and set the last_modified timestamp
    public void updateDescription(int entryId, String newDescription) {
        String sql = "UPDATE time_entries SET description = ?, last_modified = ? WHERE id = ?";

        String currentTimestamp = getCurrentTimestamp();

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newDescription);
            pstmt.setString(2, currentTimestamp);
            pstmt.setInt(3, entryId);
            pstmt.executeUpdate();
            System.out.println("Entry with ID " + entryId + " updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete a time entry
    public void deleteEntry(int entryId) {
        String sql = "DELETE FROM time_entries WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, entryId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Entry deleted successfully.");
            } else {
                System.out.println("No entry found with ID: " + entryId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to get the current timestamp
    private String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }
}
