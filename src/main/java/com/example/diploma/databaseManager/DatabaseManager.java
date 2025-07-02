package com.example.diploma.databaseManager;

import com.example.diploma.entities.Entry;
import com.example.diploma.entities.User;

import java.util.List;

public class DatabaseManager {
    private final UserDAO userDAO;
    private final FilterDAO filterDAO;
    private final TimeEntryDAO timeEntryDAO;

    public DatabaseManager() {
        this.userDAO = new UserDAO();
        this.filterDAO = new FilterDAO();
        this.timeEntryDAO = new TimeEntryDAO();

        new SchemaManager().initializeDatabase();
    }

    // --- User operations ---

    public boolean registerUser(String username, String password) {
        return userDAO.registerUser(username, password);
    }

    public boolean validateUser(String username, String password) {
        return userDAO.validateUser(username, password);
    }

    public User getUser(String username) {
        return userDAO.getUserObject(username);
    }

    public Integer getUserId(String username) {
        return userDAO.getUserId(username);
    }

    public boolean assignFilterToUser(int userId, String filterName) {
        boolean created = filterDAO.createFilter(filterName);
        Integer filterId = filterDAO.getFilterId(filterName);
        if (filterId != null) {
            return userDAO.linkUserToFilter(userId, filterId);
        }
        return false;
    }

    // --- Filter operations ---

    public boolean saveFilter(String name) {
        return filterDAO.createFilter(name);
    }

    public List<String> getFiltersForUser(Integer userId) {
        return filterDAO.getFiltersForUser(userId);
    }

    // --- Time entry operations ---

    public boolean saveEntry(String filterName, String duration, String description) {
        Integer filterId = filterDAO.getFilterId(filterName);
        Integer entryId = timeEntryDAO.saveEntry(filterId, duration, description);
        if (entryId != null && filterId != null) {
            return filterDAO.linkFilterToEntry(filterId, entryId);
        }
        return false;
    }

    public List<Entry> getEntriesForFilter(String filterName) {
        return timeEntryDAO.getEntriesForFilter(filterName);
    }

    public boolean updateEntryDescription(int entryId, String newDescription) {
        return timeEntryDAO.updateDescription(entryId, newDescription);
    }

    public boolean deleteEntry(int entryId) {
        return timeEntryDAO.deleteEntry(entryId);
    }
}
