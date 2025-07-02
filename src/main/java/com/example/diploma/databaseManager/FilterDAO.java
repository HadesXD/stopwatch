package com.example.diploma.databaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilterDAO {
    private static final Logger LOGGER = Logger.getLogger(FilterDAO.class.getName());
    private static final String URL = "jdbc:sqlite:stopwatch.db";

    public boolean createFilter(String name) {
        final String sql = "INSERT OR IGNORE INTO filters (name) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.executeUpdate();
            LOGGER.info("Filter created: " + name);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create filter", e);
            return false;
        }
    }

    public Integer getFilterId(String name) {
        final String sql = "SELECT id FROM filters WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get filter ID", e);
            return null;
        }
    }

    public List<String> getFiltersForUser(int userId) {
        List<String> filters = new ArrayList<>();
        final String sql = """
            SELECT f.name FROM filters f JOIN user_filters uf ON f.id = uf.filter_id
            WHERE uf.user_id = ?
            ORDER BY f.name ASC
        """;

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                filters.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch filters for user", e);
        }
        return filters;
    }

    public boolean linkFilterToEntry(int filterId, int entryId) {
        final String sql = "INSERT OR IGNORE INTO filter_entries (filter_id, entry_id) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, filterId);
            stmt.setInt(2, entryId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to link filter to entry", e);
            return false;
        }
    }
}
