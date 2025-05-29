package com.example.diploma;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:stopwatch.db";

    private static final String sqlFilters = """
            CREATE TABLE IF NOT EXISTS filters (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL)""";

    private static final String sqlTimeEntries = """
        CREATE TABLE IF NOT EXISTS time_entries (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            filter_id INTEGER NOT NULL,
            duration TEXT NOT NULL,
            description TEXT NOT NULL,
            date_created TEXT NOT NULL,
            last_modified TEXT,
            FOREIGN KEY (filter_id) REFERENCES filters(id) ON DELETE CASCADE ON UPDATE CASCADE
        )""";

    public DatabaseManager() {
        File dbFile = new File("stopwatch.db");
        System.out.println("Using DB at: " + dbFile.getAbsolutePath());
        createTableIfNotExists();
    }

    public void createTableIfNotExists() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // Enable foreign key support in SQLite
            stmt.execute("PRAGMA foreign_keys = ON");

            // Create filters first (must exist for the FK)
            stmt.execute(sqlFilters);
            stmt.execute(sqlTimeEntries);
            System.out.println("Checked/created tables: filters, time_entries (with FK constraint)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllFilters() {
        List<String> filters = new ArrayList<>();
        final String sql = "SELECT name FROM filters ORDER BY name ASC";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                filters.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filters;
    }

    public boolean saveFilter(String filterName) {
        final String sql = "INSERT OR IGNORE INTO filters (name) VALUES (?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, filterName);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Entry> getAllEntries() {
        List<Entry> entries = new ArrayList<>();
        final String sql = "SELECT id, filter_id, duration, description, date_created, last_modified FROM time_entries ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int filterId = rs.getInt("filter_id");
                String duration = rs.getString("duration");
                String description = rs.getString("description");
                String dateCreated = rs.getString("date_created");
                String lastModified = rs.getString("last_modified");
                entries.add(new Entry(id, filterId, duration, description, dateCreated, lastModified));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public void saveEntry(String filterName, String duration, String description) {
        String timestamp = getCurrentTimestamp();
        final String getFilterIdSQL  = "SELECT id FROM filters WHERE name = ?";
        final String insertEntrySQL = "INSERT INTO time_entries " +
                "(filter_id, duration, description, date_created, last_modified) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Start transaction
            int filterId;
            try (PreparedStatement getFilterIdStmt = conn.prepareStatement(getFilterIdSQL)) {
                getFilterIdStmt.setString(1, filterName);
                ResultSet rs = getFilterIdStmt.executeQuery();
                if (rs.next()) {
                    filterId = rs.getInt("id");
                } else {
                    System.err.println("❌ Filter not found: " + filterName);
                    return;
                }
            }

            try (PreparedStatement insertEntryStmt = conn.prepareStatement(insertEntrySQL)) {
                insertEntryStmt.setInt(1, filterId);
                insertEntryStmt.setString(2, duration);
                insertEntryStmt.setString(3, description);
                insertEntryStmt.setString(4, timestamp);
                insertEntryStmt.setString(5, timestamp);
                insertEntryStmt.executeUpdate();
            }
            conn.commit();
            System.out.println("✅ Entry saved successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void updateDescription(int entryId, String newDescription) {
        final String sql = "UPDATE time_entries SET description = ?, last_modified = ? WHERE id = ?";
        String currentTimestamp = getCurrentTimestamp();

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newDescription);
            stmt.setString(2, currentTimestamp);
            stmt.setInt(3, entryId);
            stmt.executeUpdate();
            System.out.println("Entry with ID " + entryId + " updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEntry(int entryId) {
        final String sql = "DELETE FROM time_entries WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entryId);
            int rowsAffected = stmt.executeUpdate();

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
