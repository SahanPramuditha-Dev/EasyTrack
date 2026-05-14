package com.example.easytrack.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.easytrack.R;
import com.example.easytrack.interfaces.OnTaskClickListener;
import com.example.easytrack.models.Task;
import java.util.List;

/**
 * TaskAdapter is the bridge between our list of Task objects and the RecyclerView.
 * It's responsible for taking each task and turning it into a visual row on the screen.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;

    // The constructor takes the list of tasks and a listener for button clicks
    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    /**
     * This is called when the RecyclerView needs a new row layout.
     * We inflate the 'item_task' XML layout here.
     */
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    /**
     * This is where we fill the row with data from a specific task.
     * It's called as the user scrolls through the list.
     */
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvTaskName.setText(task.getName());
        holder.cbTask.setChecked(task.isDone());

        // Visual feedback: strike through text if the task is finished
        updateStrikeThrough(holder.tvTaskName, task.isDone());

        // Handle clicking the checkbox
        holder.cbTask.setOnClickListener(v -> {
            boolean checked = holder.cbTask.isChecked();
            task.setDone(checked);
            updateStrikeThrough(holder.tvTaskName, checked);
            listener.onTaskCheckClick(task);
        });

        // Handle clicking the Edit and Delete buttons
        holder.btnEdit.setOnClickListener(v -> listener.onTaskEditClick(task));
        holder.btnDelete.setOnClickListener(v -> listener.onTaskDeleteClick(task));
    }

    /**
     * A helper method to visually "cross out" completed tasks.
     */
    private void updateStrikeThrough(TextView textView, boolean isDone) {
        if (isDone) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setAlpha(0.5f); // Fade it out a bit too
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /**
     * Call this when the list of tasks changes to refresh the screen.
     */
    public void updateTasks(List<Task> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged(); // Tells the list to redraw itself
    }

    /**
     * TaskViewHolder is a container for the UI elements in a single row.
     * It helps the RecyclerView reuse views efficiently.
     */
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName;
        CheckBox cbTask;
        ImageButton btnEdit, btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            cbTask = itemView.findViewById(R.id.cb_task);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
