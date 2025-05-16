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
    public void saveEntry(String filterName, String duration, String description) {
        String timestamp = getCurrentTimestamp();
        String getFilterIdSQL  = "SELECT id FROM filters WHERE name = ?";
        String insertEntrySQL = "INSERT INTO time_entries (filter_id, duration, description, date_created, last_modified) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Start transaction

            int filterId;

            // Fetch filter ID from name
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

            // Insert time entry
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

    // Ensure the table exists
    public void createTableIfNotExists() {
        // Create the filters table if it doesn't exist
        String sqlFilters = "CREATE TABLE IF NOT EXISTS filters (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE NOT NULL" +
                ")";

        // SQL to create time_entries table with foreign key constraint
        String sqlTimeEntries = """
        CREATE TABLE IF NOT EXISTS time_entries (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            filter_id INTEGER NOT NULL,
            duration TEXT NOT NULL,
            description TEXT NOT NULL,
            date_created TEXT NOT NULL,
            last_modified TEXT,
            FOREIGN KEY (filter_id) REFERENCES filters(id) ON DELETE CASCADE ON UPDATE CASCADE
        )
        """;

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


    // Show DB file path
    private void printDbPath() {
        File dbFile = new File("stopwatch.db");
        System.out.println("Using DB at: " + dbFile.getAbsolutePath());
    }

    // Insert new filter if not exists
    public boolean saveFilter(String filterName) {
        String sql = "INSERT OR IGNORE INTO filters (name) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filterName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all filters
    public List<String> getAllFilters() {
        List<String> filters = new ArrayList<>();
        String sql = "SELECT name FROM filters ORDER BY name ASC";

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
