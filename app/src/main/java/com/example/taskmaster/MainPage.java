package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.taskmaster.Adapters.ToDoAdapter;
import com.example.taskmaster.Model.ToDoModel;
import com.example.taskmaster.Utils.DatabaseHandler;

import java.util.Collections;
import java.util.List;

public class MainPage extends AppCompatActivity implements DialogCloseListener {

    private DatabaseHandler db;
    private RecyclerView tasksRecyclerView;
    private ToDoAdapter tasksAdapter;
    private FloatingActionButton fab;
    private List<ToDoModel> taskList;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Hide the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Retrieve user-specific database name
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String databaseName = sharedPreferences.getString("current_user_database", null);

        if (databaseName == null) {
            Toast.makeText(this, "No database found. Please log in again.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Initialize Database
        db = new DatabaseHandler(this, databaseName);
        db.openDatabase();

        // Initialize UI Components
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        fab = findViewById(R.id.fab);
        logoutButton = findViewById(R.id.logoutButton); // Add logout button in XML

        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(db, MainPage.this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        // Add swipe and drag functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        // Load tasks and sort by priority
        taskList = db.getAllTasks();
        sortTasksByPriority(taskList);

        if (taskList.isEmpty()) {
            Toast.makeText(this, "No tasks to display. Add a new task!", Toast.LENGTH_SHORT).show();
        }

        tasksAdapter.setTasks(taskList);

        // FloatingActionButton to add new tasks
        fab.setOnClickListener(v -> AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG));

        // Logout Button Click Listener
        logoutButton.setOnClickListener(v -> logout());
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        // Refresh task list after closing the dialog
        taskList = db.getAllTasks();
        sortTasksByPriority(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Task saved successfully. You can view your tasks!", Toast.LENGTH_LONG).show();
    }

    private void sortTasksByPriority(List<ToDoModel> tasks) {
        Collections.sort(tasks, (task1, task2) -> Integer.compare(task2.getPriority(), task1.getPriority())); // Descending order
    }

    private void logout() {
        // Clear SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all session data
        editor.apply();

        // Close database
        if (db != null) {
            db.closeDatabase();
        }

        // Navigate back to login screen
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainPage.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close MainPage activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.closeDatabase();
        }
    }
}
