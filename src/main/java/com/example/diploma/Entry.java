package com.example.diploma;

public class Entry {
    private int id;
    private int filterId;
    private String duration;
    private String description;
    private String dateCreated;
    private String lastModified;

    public Entry(int id, int filterId, String duration, String description, String dateCreated, String lastModified) {
        this.id = id;
        this.filterId = filterId;
        this.duration = duration;
        this.description = description;
        this.dateCreated = dateCreated;
        this.lastModified = lastModified;
    }

    public int getId() {
        return id;
    }

    public int getFilterId() { return filterId; }

    public String getDuration() {
        return duration;
    }

    public String getDescription() {
        return description;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return "filter ID: " + filterId + " | Duration: " + duration +
                " | Description " + (description == null ? "" : description) +
                " | Created: " + dateCreated + " | Last Modified: " + lastModified;
    }
}
