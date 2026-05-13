package com.example.easytrack;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskViewModel extends ViewModel {
    private final DatabaseHelper dbHelper;
    private final int userId;
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TaskViewModel(DatabaseHelper dbHelper, int userId) {
        this.dbHelper = dbHelper;
        this.userId = userId;
        loadTasks();
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void loadTasks() {
        executorService.execute(() -> {
            List<Task> taskList = dbHelper.getAllTasks(userId);
            tasks.postValue(taskList);
        });
    }

    public void addTask(String name) {
        executorService.execute(() -> {
            if (dbHelper.addTask(name, userId)) {
                toastMessage.postValue("Task added");
                loadTasks();
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
        executorService.shutdown();
    }
}
