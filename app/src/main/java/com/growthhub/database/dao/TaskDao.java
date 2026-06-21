package com.growthhub.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.growthhub.constant.TaskStatus;
import com.growthhub.database.entity.TaskItem;
import com.growthhub.database.helper.GrowthHubDbHelper;

import java.util.ArrayList;
import java.util.List;

public class TaskDao {
    private final GrowthHubDbHelper helper;

    public TaskDao(GrowthHubDbHelper helper) {
        this.helper = helper;
    }

    public long insert(long categoryId, String title, String description) {
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put("category_id", categoryId);
        values.put("title", title);
        values.put("description", description);
        values.put("status", TaskStatus.ACTIVE);
        values.put("create_time", now);
        values.put("update_time", now);
        return helper.getWritableDatabase().insert("task", null, values);
    }

    public int update(long id, long categoryId, String title, String description, int status) {
        ContentValues values = new ContentValues();
        values.put("category_id", categoryId);
        values.put("title", title);
        values.put("description", description);
        values.put("status", status);
        values.put("update_time", System.currentTimeMillis());
        return helper.getWritableDatabase().update("task", values, "id=?", new String[]{String.valueOf(id)});
    }

    public int updateStatus(long id, int status) {
        ContentValues values = new ContentValues();
        values.put("status", status);
        values.put("update_time", System.currentTimeMillis());
        return helper.getWritableDatabase().update("task", values, "id=?", new String[]{String.valueOf(id)});
    }

    public TaskItem getById(long id) {
        Cursor c = helper.getReadableDatabase().query("task", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (c.moveToFirst()) return map(c);
        } finally {
            c.close();
        }
        return null;
    }

    public List<TaskItem> getAll() {
        return query(null, null);
    }

    public List<TaskItem> getSelectable() {
        return query("status IN (?,?)", new String[]{String.valueOf(TaskStatus.ACTIVE), String.valueOf(TaskStatus.COMPLETED)});
    }

    private List<TaskItem> query(String selection, String[] args) {
        List<TaskItem> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().query("task", null, selection, args, null, null, "update_time DESC");
        try {
            while (c.moveToNext()) result.add(map(c));
        } finally {
            c.close();
        }
        return result;
    }

    private TaskItem map(Cursor c) {
        TaskItem item = new TaskItem();
        item.id = c.getLong(c.getColumnIndexOrThrow("id"));
        item.categoryId = c.getLong(c.getColumnIndexOrThrow("category_id"));
        item.title = c.getString(c.getColumnIndexOrThrow("title"));
        item.description = c.getString(c.getColumnIndexOrThrow("description"));
        item.status = c.getInt(c.getColumnIndexOrThrow("status"));
        item.createTime = c.getLong(c.getColumnIndexOrThrow("create_time"));
        item.updateTime = c.getLong(c.getColumnIndexOrThrow("update_time"));
        return item;
    }
}
