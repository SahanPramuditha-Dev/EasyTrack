package com.example.easytrack.interfaces;

import com.example.easytrack.models.Task;

public interface OnTaskClickListener {
    void onTaskCheckClick(Task task);
    void onTaskDeleteClick(Task task);
    void onTaskEditClick(Task task);
}
