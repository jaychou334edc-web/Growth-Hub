package com.growthhub.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.growthhub.database.entity.Tag;
import com.growthhub.database.helper.GrowthHubDbHelper;

import java.util.ArrayList;
import java.util.List;

public class TagDao {
    private final GrowthHubDbHelper helper;

    public TagDao(GrowthHubDbHelper helper) {
        this.helper = helper;
    }

    public long insert(String name) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("create_time", System.currentTimeMillis());
        return helper.getWritableDatabase().insert("tag", null, values);
    }

    public int update(long id, String name) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        return helper.getWritableDatabase().update("tag", values, "id=?", new String[]{String.valueOf(id)});
    }

    public int delete(long id) {
        helper.getWritableDatabase().delete("task_tag", "tag_id=?", new String[]{String.valueOf(id)});
        return helper.getWritableDatabase().delete("tag", "id=?", new String[]{String.valueOf(id)});
    }

    public List<Tag> getAll() {
        List<Tag> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().query("tag", null, null, null, null, null, "create_time DESC");
        try {
            while (c.moveToNext()) {
                Tag item = new Tag();
                item.id = c.getLong(c.getColumnIndexOrThrow("id"));
                item.name = c.getString(c.getColumnIndexOrThrow("name"));
                item.createTime = c.getLong(c.getColumnIndexOrThrow("create_time"));
                result.add(item);
            }
        } finally {
            c.close();
        }
        return result;
    }
}
