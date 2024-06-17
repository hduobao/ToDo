package com.hf.todolist.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.hf.todolist.dao.helper.CategoryDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private SQLiteDatabase database;
    private CategoryDatabaseHelper dbHelper;

    public CategoryDAO(Context context) {
        dbHelper = new CategoryDatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void addCategory(String name) {
        ContentValues values = new ContentValues();
        values.put(CategoryDatabaseHelper.COLUMN_NAME, name);
        database.insert(CategoryDatabaseHelper.TABLE_CATEGORIES, null, values);
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();

        Cursor cursor = database.query(CategoryDatabaseHelper.TABLE_CATEGORIES,
                new String[]{CategoryDatabaseHelper.COLUMN_NAME}, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            categories.add(cursor.getString(0));
            cursor.moveToNext();
        }

        cursor.close();
        return categories;
    }

    public void deleteCategory(String name) {
        database.delete(CategoryDatabaseHelper.TABLE_CATEGORIES,
                CategoryDatabaseHelper.COLUMN_NAME + " = ?", new String[]{name});
    }

    public void updateCategory(String oldName, String newName) {
        ContentValues values = new ContentValues();
        values.put(CategoryDatabaseHelper.COLUMN_NAME, newName);
        database.update(CategoryDatabaseHelper.TABLE_CATEGORIES, values,
                CategoryDatabaseHelper.COLUMN_NAME + " = ?", new String[]{oldName});
    }
}
