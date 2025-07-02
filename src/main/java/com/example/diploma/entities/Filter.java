package com.example.diploma.entities;

public class Filter {
    private int id;
    private String name;

    public Filter(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
}
