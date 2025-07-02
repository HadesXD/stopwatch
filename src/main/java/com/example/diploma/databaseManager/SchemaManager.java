package com.example.diploma.databaseManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SchemaManager {
    private static final Logger LOGGER = Logger.getLogger(SchemaManager.class.getName());
    private static final String URL = "jdbc:sqlite:stopwatch.db";

    public void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS filters (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL UNIQUE
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS time_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    duration TEXT NOT NULL,
                    description TEXT NOT NULL,
                    date_created TEXT NOT NULL,
                    last_modified TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_filters (
                    user_id INTEGER NOT NULL,
                    filter_id INTEGER NOT NULL,
                    PRIMARY KEY (user_id, filter_id),
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
                    FOREIGN KEY (filter_id) REFERENCES filters(id) ON DELETE CASCADE ON UPDATE CASCADE
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS filter_entries (
                    filter_id INTEGER NOT NULL,
                    entry_id INTEGER NOT NULL,
                    PRIMARY KEY (filter_id, entry_id),
                    FOREIGN KEY (filter_id) REFERENCES filters(id) ON DELETE CASCADE ON UPDATE CASCADE,
                    FOREIGN KEY (entry_id) REFERENCES time_entries(id) ON DELETE CASCADE ON UPDATE CASCADE
                )
            """);

            LOGGER.info("✅ All tables created or verified successfully.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Failed to create database tables", e);
        }
    }
}
