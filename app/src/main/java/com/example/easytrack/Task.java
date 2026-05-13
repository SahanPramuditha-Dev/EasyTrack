package com.example.easytrack;

public class Task {
    private int id;
    private String name;
    private boolean isDone;

    public Task(int id, String name, boolean isDone) {
        this.id = id;
        this.name = name;
        this.isDone = isDone;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }
}
