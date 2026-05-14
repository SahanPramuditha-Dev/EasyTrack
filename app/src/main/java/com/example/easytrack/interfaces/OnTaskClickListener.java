package com.example.easytrack.interfaces;

import com.example.easytrack.models.Task;

/**
 * This interface acts as a communication bridge between the TaskAdapter and the TasksActivity.
 * Since the adapter only knows how to display items, it uses this listener to tell the activity
 * when a user interacts with a task (like clicking edit or delete).
 */
public interface OnTaskClickListener {
    /** Called when the user toggles the completion checkbox */
    void onTaskCheckClick(Task task);

    /** Called when the user clicks the trash icon to remove a task */
    void onTaskDeleteClick(Task task);

    /** Called when the user clicks the pencil icon to rename a task */
    void onTaskEditClick(Task task);
}
