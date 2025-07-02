package com.example.diploma.databaseManager;

import com.example.diploma.entities.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private static final String URL = "jdbc:sqlite:stopwatch.db";

    public boolean registerUser(String username, String password) {
        if (userExists(username)) {
            System.out.println("BOI");
            return false;
        }

        final String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            LOGGER.info("User created: " + username);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "User creation failed: " + username, e);
            return false;
        }
    }

    public boolean userExists(String username) {
        final String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to check if user exists: " + username, e);
            return false;
        }
    }

    public boolean validateUser(String username, String password) {
        final String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error validating user", e);
            return false;
        }
    }

    // Get full User object from username
    public User getUserObject(String username) {
        final String sql = "SELECT id, username FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch user object", e);
        }
        return null;
    }

    // Get user ID (lightweight alternative)
    public Integer getUserId(String username) {
        final String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get user ID", e);
            return null;
        }
    }

    // Link user to a filter (many-to-many junction)
    public boolean linkUserToFilter(int userId, int filterId) {
        final String sql = "INSERT OR IGNORE INTO user_filters (user_id, filter_id) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, filterId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to link user to filter", e);
            return false;
        }
    }

    // Retrieve filters (names) associated with a user
    public List<String> getUserFilters(int userId) {
        List<String> filters = new ArrayList<>();
        final String sql = """
            SELECT f.name FROM filters f
            JOIN user_filters uf ON f.id = uf.filter_id
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
}
