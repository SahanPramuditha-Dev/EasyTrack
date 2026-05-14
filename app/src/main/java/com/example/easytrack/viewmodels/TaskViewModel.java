package com.example.easytrack.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.easytrack.database.DatabaseHelper;
import com.example.easytrack.models.Task;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TaskViewModel is the "brain" for the TasksActivity.
 * It sits between the Activity (the UI) and the DatabaseHelper (the data).
 * By using a ViewModel, we ensure that our task list survives screen rotations 
 * and other configuration changes.
 */
public class TaskViewModel extends ViewModel {
    private final DatabaseHelper dbHelper;
    private final int userId;
    
    // LiveData allows the UI to "observe" the data. 
    // When the task list changes, the UI updates automatically.
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    
    // A separate channel to send short messages (like "Task added") to the UI
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    
    // Using a background thread so the app doesn't freeze during database operations
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TaskViewModel(DatabaseHelper dbHelper, int userId) {
        this.dbHelper = dbHelper;
        this.userId = userId;
        loadTasks(); // Fetch the initial list of tasks
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    /**
     * Re-fetches the tasks from the database in the background.
     */
    public void loadTasks() {
        executorService.execute(() -> {
            List<Task> taskList = dbHelper.getAllTasks(userId);
            tasks.postValue(taskList); // PostValue is used to update LiveData from a background thread
        });
    }

    public void addTask(String name) {
        executorService.execute(() -> {
            if (dbHelper.addTask(name, userId)) {
                toastMessage.postValue("Task added");
                loadTasks(); // Refresh the list
            }
        });
    }

    public void updateTaskStatus(Task task) {
        executorService.execute(() -> {
            dbHelper.updateTaskStatus(task.getId(), task.isDone());
            loadTasks();
        });
    }

    public void updateTaskName(int taskId, String newName) {
        executorService.execute(() -> {
            if (dbHelper.updateTaskName(taskId, newName)) {
                toastMessage.postValue("Task updated");
                loadTasks();
            }
        });
    }

    public void deleteTask(int taskId) {
        executorService.execute(() -> {
            if (dbHelper.deleteTask(taskId)) {
                toastMessage.postValue("Task deleted");
                loadTasks();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Always shut down the background service when the ViewModel is destroyed
        executorService.shutdown();
    }
}
