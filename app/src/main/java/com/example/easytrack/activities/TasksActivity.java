package com.example.easytrack.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytrack.R;
import com.example.easytrack.adapters.TaskAdapter;
import com.example.easytrack.database.DatabaseHelper;
import com.example.easytrack.interfaces.OnTaskClickListener;
import com.example.easytrack.models.Task;
import com.example.easytrack.session.SessionManager;
import com.example.easytrack.utils.ToastHelper;
import com.example.easytrack.viewmodels.TaskViewModel;
import com.example.easytrack.viewmodels.TaskViewModelFactory;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class TasksActivity extends AppCompatActivity implements OnTaskClickListener {

    private static final String TAG = "TasksActivity";
    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private int currentUserId;
    private SessionManager sessionManager;
    private TextView tvProgressStatus;
    private LinearProgressIndicator progressBar;
    private LinearLayout layoutEmptyState;
    private RecyclerView rvTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tasks);

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        
        if (currentUserId == -1) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvProgressStatus = findViewById(R.id.tv_progress_status);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        rvTasks = findViewById(R.id.rv_tasks);

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, this);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(adapter);

        setupViewModel();

        findViewById(R.id.fab).setOnClickListener(v -> showTaskDialog(null));

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });

        findViewById(R.id.nav_about).setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
            finish();
        });

        findViewById(R.id.pill_tasks).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.tv_tasks)).setTextColor(getResources().getColor(R.color.button_green));
        ((android.widget.ImageView)findViewById(R.id.iv_tasks)).setColorFilter(getResources().getColor(R.color.button_green));
    }

    private void setupViewModel() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        TaskViewModelFactory factory = new TaskViewModelFactory(dbHelper, currentUserId);
        viewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);

        viewModel.getTasks().observe(this, tasks -> {
            this.taskList = tasks;
            adapter.updateTasks(tasks);
            
            if (tasks.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvTasks.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                rvTasks.setVisibility(View.VISIBLE);
            }
            
            updateProgress();
        });

        viewModel.getToastMessage().observe(this, message -> {
            if (message != null) {
                ToastHelper.showCustomToast(this, message);
            }
        });
    }

    private void updateProgress() {
        int total = taskList.size();
        int done = 0;
        for (Task t : taskList) {
            if (t.isDone()) done++;
        }
        
        tvProgressStatus.setText(done + " of " + total + " done");
        if (total > 0) {
            progressBar.setProgress((done * 100) / total);
        } else {
            progressBar.setProgress(0);
        }
    }

    @Override
    public void onTaskCheckClick(Task task) {
        viewModel.updateTaskStatus(task);
    }

    @Override
    public void onTaskDeleteClick(Task task) {
        viewModel.deleteTask(task.getId());
    }

    @Override
    public void onTaskEditClick(Task task) {
        showTaskDialog(task);
    }

    private void showTaskDialog(Task existingTask) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_task);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        EditText etTaskName = dialog.findViewById(R.id.et_task_name);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnAdd = dialog.findViewById(R.id.btn_add);

        if (existingTask != null) {
            btnAdd.setText("Update");
            etTaskName.setText(existingTask.getName());
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String name = etTaskName.getText().toString().trim();
            if (!name.isEmpty()) {
                if (existingTask == null) {
                    viewModel.addTask(name);
                } else {
                    viewModel.updateTaskName(existingTask.getId(), name);
                }
                dialog.dismiss();
            } else {
                ToastHelper.showCustomToast(this, "Enter task name");
            }
        });

        dialog.show();
    }
}
