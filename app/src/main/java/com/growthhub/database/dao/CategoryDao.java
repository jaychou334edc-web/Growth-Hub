package com.growthhub.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.growthhub.database.entity.Category;
import com.growthhub.database.helper.GrowthHubDbHelper;

import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    private final GrowthHubDbHelper helper;

    public CategoryDao(GrowthHubDbHelper helper) {
        this.helper = helper;
    }

    public long insert(String name, String description) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("create_time", System.currentTimeMillis());
        return helper.getWritableDatabase().insert("category", null, values);
    }

    public int update(long id, String name, String description) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        return helper.getWritableDatabase().update("category", values, "id=?", new String[]{String.valueOf(id)});
    }

    public List<Category> getAll() {
        List<Category> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().query("category", null, null, null, null, null, "create_time DESC");
        try {
            while (c.moveToNext()) result.add(map(c));
        } finally {
            c.close();
        }
        return result;
    }

    private Category map(Cursor c) {
        Category item = new Category();
        item.id = c.getLong(c.getColumnIndexOrThrow("id"));
        item.name = c.getString(c.getColumnIndexOrThrow("name"));
        item.description = c.getString(c.getColumnIndexOrThrow("description"));
        item.createTime = c.getLong(c.getColumnIndexOrThrow("create_time"));
        return item;
    }
}
