package com.example.taskmaster.Adapters;

import com.example.taskmaster.AddNewTask;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmaster.MainPage;
import com.example.taskmaster.Model.ToDoModel;
import com.example.taskmaster.R;
import com.example.taskmaster.Utils.DatabaseHandler;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<ToDoModel> todoList;
    private DatabaseHandler db;
    private MainPage activity;

    public ToDoAdapter(DatabaseHandler db, MainPage activity) {
        this.db = db;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        db.openDatabase();
        final ToDoModel item = todoList.get(position);

        // Task details
        holder.task.setText(item.getTask());
        holder.task.setChecked(toBoolean(item.getStatus()));

        // Priority handling
        holder.prioritySpinner.setSelection(item.getPriority() - 1);
        holder.itemView.setBackgroundColor(getPriorityColor(item.getPriority()));

        // Checkbox change listener
        holder.task.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                db.updateStatus(item.getId(), 1);
            } else {
                db.updateStatus(item.getId(), 0);
            }
        });

        // Spinner change listener
        holder.prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isInitialized = false;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitialized) {
                    int priority = position + 1;
                    db.updatePriority(item.getId(), priority);
                    item.setPriority(priority);
                    holder.itemView.setBackgroundColor(getPriorityColor(priority));
                }
                isInitialized = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public void setTasks(List<ToDoModel> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        ToDoModel item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public void editItem(int position) {
        ToDoModel item = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        bundle.putInt("priority", item.getPriority());
        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTask.TAG);
    }

    public Context getContext() {
        return activity;
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    private int getPriorityColor(int priority) {
        switch (priority) {
            case 1:
                return ContextCompat.getColor(activity, R.color.priority_high);
            case 2:
                return ContextCompat.getColor(activity, R.color.priority_medium);
            case 3:
                return ContextCompat.getColor(activity, R.color.priority_low);
            default:
                return ContextCompat.getColor(activity, R.color.priority_default);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        Spinner prioritySpinner;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
            prioritySpinner = view.findViewById(R.id.prioritySpinner);
        }
    }
}
