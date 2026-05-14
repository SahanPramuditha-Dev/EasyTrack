package com.example.easytrack.models;

/**
 * The Task model represents a single to-do item in our app.
 * It's a simple "Plain Old Java Object" (POJO) that holds the task's data
 * like its unique ID, description, and whether it's been completed.
 */
public class Task {
    private int id;           // Unique identifier from the database
    private String name;      // The actual text of the task (e.g., "Buy groceries")
    private boolean isDone;   // Keep track of whether the user checked this off

    // Simple constructor to bundle task data together
    public Task(int id, String name, boolean isDone) {
        this.id = id;
        this.name = name;
        this.isDone = isDone;
    }

    // Getters and Setters - the standard way to access private data in Java
    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isDone() { return isDone; }
    
    // We only need a setter for 'isDone' because tasks can be toggled
    public void setDone(boolean done) { isDone = done; }
}
