
package com.example.taskmaster.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.taskmaster.Model.ToDoModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Info
    private static final String DEFAULT_DATABASE_NAME = "AppDatabase";
    private static final int DATABASE_VERSION = 3;

    // Task Table
    private static final String TODO_TABLE = "todo";
    private static final String ID = "id";
    private static final String TASK = "task";
    private static final String STATUS = "status";
    private static final String PRIORITY = "priority";

    // User Table
    private static final String USER_TABLE = "users";
    private static final String USER_ID = "user_id";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";

    private SQLiteDatabase db;

    // Constructor
    public DatabaseHandler(Context context, String databaseName) {
        super(context, databaseName == null ? DEFAULT_DATABASE_NAME : databaseName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Task Table
        String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TASK + " TEXT, "
                + STATUS + " INTEGER, "
                + PRIORITY + " INTEGER)";
        db.execSQL(CREATE_TODO_TABLE);

        // Create User Table
        String CREATE_USER_TABLE = "CREATE TABLE " + USER_TABLE + "("
                + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USERNAME + " TEXT UNIQUE, "
                + EMAIL + " TEXT, "
                + PASSWORD + " TEXT)";
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Add the user table if upgrading from version 2
            String CREATE_USER_TABLE = "CREATE TABLE " + USER_TABLE + "("
                    + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + USERNAME + " TEXT UNIQUE, "
                    + EMAIL + " TEXT, "
                    + PASSWORD + " TEXT)";
            db.execSQL(CREATE_USER_TABLE);
        }
    }

    public void openDatabase() {
        db = this.getWritableDatabase();
    }

    public void closeDatabase() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    // --- Task Table Operations ---

    public void insertTask(ToDoModel task) {
        ContentValues cv = new ContentValues();
        cv.put(TASK, task.getTask());
        cv.put(STATUS, 0); // Default status: incomplete
        cv.put(PRIORITY, task.getPriority());
        db.insert(TODO_TABLE, null, cv);
    }

    public List<ToDoModel> getAllTasks() {
        List<ToDoModel> taskList = new ArrayList<>();
        Cursor cur = null;
        db.beginTransaction();
        try {
            cur = db.query(TODO_TABLE, null, null, null, null, null, PRIORITY + " DESC");
            if (cur != null && cur.moveToFirst()) {
                do {
                    ToDoModel task = new ToDoModel();
                    task.setId(cur.getInt(cur.getColumnIndexOrThrow(ID)));
                    task.setTask(cur.getString(cur.getColumnIndexOrThrow(TASK)));
                    task.setStatus(cur.getInt(cur.getColumnIndexOrThrow(STATUS)));
                    task.setPriority(cur.getInt(cur.getColumnIndexOrThrow(PRIORITY)));
                    taskList.add(task);
                } while (cur.moveToNext());
            }
        } finally {
            db.endTransaction();
            if (cur != null) {
                cur.close();
            }
        }
        return taskList;
    }

    public void updateStatus(int id, int status) {
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TODO_TABLE, cv, ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void updateTask(int id, String task, int priority) {
        ContentValues cv = new ContentValues();
        cv.put(TASK, task);
        cv.put(PRIORITY, priority);
        db.update(TODO_TABLE, cv, ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void updatePriority(int id, int priority) {
        ContentValues cv = new ContentValues();
        cv.put(PRIORITY, priority);
        db.update(TODO_TABLE, cv, ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteTask(int id) {
        db.delete(TODO_TABLE, ID + " = ?", new String[]{String.valueOf(id)});
    }

    // --- User Table Operations ---

    public boolean checkUsernameExists(String username) {
        Cursor cursor = db.query(USER_TABLE,
                new String[]{USER_ID},
                USERNAME + " = ?",
                new String[]{username},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean registerUser(String username, String email, String password) {
        ContentValues cv = new ContentValues();
        cv.put(USERNAME, username);
        cv.put(EMAIL, email);
        cv.put(PASSWORD, password);

        long result = db.insert(USER_TABLE, null, cv);
        return result != -1; // Returns true if registration is successful
    }

    public boolean checkUser(String username, String password) {
        Cursor cursor = db.query(USER_TABLE,
                new String[]{USER_ID},
                USERNAME + " = ? AND " + PASSWORD + " = ?",
                new String[]{username, password},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
