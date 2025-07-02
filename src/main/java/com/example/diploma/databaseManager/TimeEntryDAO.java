package com.example.diploma.databaseManager;

import com.example.diploma.entities.Entry;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeEntryDAO {
    private static final Logger LOGGER = Logger.getLogger(TimeEntryDAO.class.getName());
    private static final String URL = "jdbc:sqlite:stopwatch.db";

    private String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    // Create a new time entry and return its ID
    public Integer saveEntry(Integer filterId, String duration, String description) {
        final String sql = "INSERT INTO time_entries (filter_id, duration, description, date_created, last_modified) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String timestamp = getTimestamp();
            stmt.setString(1, duration);
            stmt.setString(2, description);
            stmt.setString(3, timestamp);
            stmt.setString(4, timestamp);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save entry", e);
            return null;
        }
    }

    // Get entries by filter name (through filter_entries junction)
    public List<Entry> getEntriesForFilter(String filterName) {
        List<Entry> entries = new ArrayList<>();
        final String sql = """
            SELECT te.* FROM time_entries te
            JOIN filter_entries fe ON te.id = fe.entry_id
            JOIN filters f ON f.id = fe.filter_id
            WHERE f.name = ?
            ORDER BY te.id DESC
        """;

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, filterName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                entries.add(new Entry(
                        rs.getInt("id"),
                        -1,
                        rs.getString("duration"),
                        rs.getString("description"),
                        rs.getString("date_created"),
                        rs.getString("last_modified")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch entries", e);
        }
        return entries;
    }

    // Update a time entry's description
    public boolean updateDescription(int entryId, String newDescription) {
        final String sql = "UPDATE time_entries SET description = ?, last_modified = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newDescription);
            stmt.setString(2, getTimestamp());
            stmt.setInt(3, entryId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update entry", e);
            return false;
        }
    }

    // Delete a time entry
    public boolean deleteEntry(int entryId) {
        final String sql = "DELETE FROM time_entries WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entryId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete entry", e);
            return false;
        }
    }
}
