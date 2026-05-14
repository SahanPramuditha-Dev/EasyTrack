package com.example.easytrack.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.easytrack.database.DatabaseHelper;

/**
 * TaskViewModelFactory is a "helper" class that Android needs to create our TaskViewModel.
 * Since our ViewModel needs a DatabaseHelper and a User ID in its constructor, 
 * we use this factory to pass those values in safely.
 */
public class TaskViewModelFactory implements ViewModelProvider.Factory {
    private final DatabaseHelper dbHelper;
    private final int userId;

    public TaskViewModelFactory(DatabaseHelper dbHelper, int userId) {
        this.dbHelper = dbHelper;
        this.userId = userId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // We check if the class being requested is actually our TaskViewModel
        if (modelClass.isAssignableFrom(TaskViewModel.class)) {
            return (T) new TaskViewModel(dbHelper, userId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
