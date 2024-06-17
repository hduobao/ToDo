package com.hf.todolist.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.hf.todolist.dao.helper.TaskDatabaseHelper;
import com.hf.todolist.model.HfTask;

import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private SQLiteDatabase database;
    private TaskDatabaseHelper dbHelper;

    public TaskDAO(Context context) {
        dbHelper = new TaskDatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void addTask(HfTask task) {
        ContentValues values = new ContentValues();
        values.put(TaskDatabaseHelper.COLUMN_NAME, task.getName());
        values.put(TaskDatabaseHelper.COLUMN_CATEGORY, task.getCategory());
        values.put(TaskDatabaseHelper.COLUMN_DUE_DATE, task.getDueDate());
        values.put(TaskDatabaseHelper.COLUMN_PRIORITY, task.getPriority());
        values.put(TaskDatabaseHelper.COLUMN_COMPLETED, task.isCompleted() ? 1 : 0); // 添加completed字段
        database.insert(TaskDatabaseHelper.TABLE_TASKS, null, values);
    }

    public void deleteTask(long taskId) {
        database.delete(TaskDatabaseHelper.TABLE_TASKS,
                TaskDatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public int updateTask(HfTask task) {
        ContentValues values = new ContentValues();
        values.put(TaskDatabaseHelper.COLUMN_NAME, task.getName());
        values.put(TaskDatabaseHelper.COLUMN_CATEGORY, task.getCategory());
        values.put(TaskDatabaseHelper.COLUMN_DUE_DATE, task.getDueDate());
        values.put(TaskDatabaseHelper.COLUMN_PRIORITY, task.getPriority());
        values.put(TaskDatabaseHelper.COLUMN_COMPLETED, task.isCompleted() ? 1 : 0); // 添加completed字段
        int rows = database.update(TaskDatabaseHelper.TABLE_TASKS, values,
                TaskDatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});

        System.out.println("Update Task ID: " + task.getId() + ", Rows Affected: " + rows);
        return rows;
    }

    public List<HfTask> getPendingTasks() {
        List<HfTask> tasks = new ArrayList<>();

        Cursor cursor = database.query(TaskDatabaseHelper.TABLE_TASKS,
                null, TaskDatabaseHelper.COLUMN_COMPLETED + " = ?", new String[]{"0"},
                null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HfTask task = cursorToTask(cursor);
            tasks.add(task);
            cursor.moveToNext();
        }

        cursor.close();
        return tasks;
    }

    public List<HfTask> getCompletedTasks() {
        List<HfTask> tasks = new ArrayList<>();

        Cursor cursor = database.query(TaskDatabaseHelper.TABLE_TASKS,
                null, TaskDatabaseHelper.COLUMN_COMPLETED + " = ?", new String[]{"1"},
                null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HfTask task = cursorToTask(cursor);
            tasks.add(task);
            cursor.moveToNext();
        }

        cursor.close();
        return tasks;
    }


    public List<HfTask> getAllTasks() {
        List<HfTask> tasks = new ArrayList<>();

        Cursor cursor = database.query(TaskDatabaseHelper.TABLE_TASKS,
                null, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HfTask task = cursorToTask(cursor);
            tasks.add(task);
            cursor.moveToNext();
        }

        cursor.close();
        return tasks;
    }

    private HfTask cursorToTask(Cursor cursor) {
        HfTask task = new HfTask();
        task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_ID)));
        task.setName(cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_NAME)));
        task.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_CATEGORY)));
        task.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_DUE_DATE)));
        task.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_PRIORITY)));
        task.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_COMPLETED)) == 1); // 读取completed字段
        return task;
    }
}
