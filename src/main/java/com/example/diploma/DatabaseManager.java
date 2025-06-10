package com.example.diploma;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String URL = "jdbc:sqlite:stopwatch.db";

    private static final String sqlFilters = """
        CREATE TABLE IF NOT EXISTS filters (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT UNIQUE NOT NULL)
        """;

    private static final String sqlTimeEntries = """
        CREATE TABLE IF NOT EXISTS time_entries (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            filter_id INTEGER NOT NULL,
            duration TEXT NOT NULL,
            description TEXT NOT NULL,
            date_created TEXT NOT NULL,
            last_modified TEXT,
            FOREIGN KEY (filter_id) REFERENCES filters(id) ON DELETE CASCADE ON UPDATE CASCADE)
        """;

    public DatabaseManager() {
        var dbFile = new File("stopwatch.db");
        LOGGER.info("Using DB at: " + dbFile.getAbsolutePath());
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
            LOGGER.info("Checked/created tables: filters, time_entries (with FK constraint)");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create database", e);
        }
    }

    public List<String> getAllFilters() {
        List<String> filters = new ArrayList<>();
        final String sqlGetFilters = "SELECT name FROM filters ORDER BY name ASC";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlGetFilters)) {

            while (rs.next()) {
                filters.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching filters from the database", e);
        }
        return filters;
    }

    public boolean saveFilter(String filterName) {
        final String sqlSaveFilter = "INSERT OR IGNORE INTO filters (name) VALUES (?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sqlSaveFilter)) {

            stmt.setString(1, filterName);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving filter to the database", e);
            return false;
        }
    }

    public List<Entry> getEntries(String filterName) {
        List<Entry> entries = new ArrayList<>();
        final String sqlGetEntries = "SELECT te.* FROM time_entries te JOIN filters f ON te.filter_id = f.id " +
                "WHERE f.name = ? ORDER BY te.id DESC";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sqlGetEntries)) {

            stmt.setString(1, filterName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                var id = rs.getInt("id");
                var filterId = rs.getInt("filter_id");
                var duration = rs.getString("duration");
                var description = rs.getString("description");
                var dateCreated = rs.getString("date_created");
                var lastModified = rs.getString("last_modified");
                entries.add(new Entry(id, filterId, duration, description, dateCreated, lastModified));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching entries from the database", e);
        }
        return entries;
    }

    public void saveEntry(String filterName, String duration, String description) {
        var timestamp = getCurrentTimestamp();
        final String sqlGetFilter  = "SELECT id FROM filters WHERE name = ?";
        final String sqlInsertEntry = "INSERT INTO time_entries " +
                "(filter_id, duration, description, date_created, last_modified) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Start transaction
            int filterId;
            try (PreparedStatement getFilterIdStmt = conn.prepareStatement(sqlGetFilter)) {
                getFilterIdStmt.setString(1, filterName);
                ResultSet rs = getFilterIdStmt.executeQuery();
                if (rs.next()) {
                    filterId = rs.getInt("id");
                } else {
                    LOGGER.warning("❌ Filter not found: " + filterName);
                    return;
                }
            }

            try (PreparedStatement insertEntryStmt = conn.prepareStatement(sqlInsertEntry)) {
                insertEntryStmt.setInt(1, filterId);
                insertEntryStmt.setString(2, duration);
                insertEntryStmt.setString(3, description);
                insertEntryStmt.setString(4, timestamp);
                insertEntryStmt.setString(5, timestamp);
                insertEntryStmt.executeUpdate();
            }
            conn.commit();
            LOGGER.info("✅ Entry saved successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving entry to database", e);
        }

    }

    public void updateDescription(int entryId, String newDescription) {
        final String sqlUpdateEntry = "UPDATE time_entries SET description = ?, last_modified = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sqlUpdateEntry)) {

            stmt.setString(1, newDescription);
            stmt.setString(2, getCurrentTimestamp());
            stmt.setInt(3, entryId);
            stmt.executeUpdate();
            LOGGER.info("Entry with ID " + entryId + " updated.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating entry from database", e);
        }
    }

    public void deleteEntry(int entryId) {
        final String deleteEntrySql = "DELETE FROM time_entries WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(deleteEntrySql)) {

            stmt.setInt(1, entryId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Entry deleted successfully.");
            } else {
                LOGGER.info("No entry found with ID: " + entryId);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting entry from database", e);
        }
    }

    private String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }

}
