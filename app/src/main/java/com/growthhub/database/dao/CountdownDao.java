package com.growthhub.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.growthhub.database.entity.CountdownEvent;
import com.growthhub.database.helper.GrowthHubDbHelper;

import java.util.ArrayList;
import java.util.List;

public class CountdownDao {
    private final GrowthHubDbHelper helper;

    public CountdownDao(GrowthHubDbHelper helper) {
        this.helper = helper;
    }

    public long insert(String title, long targetDate, String description, int remindDaysBefore) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("target_date", targetDate);
        values.put("description", description);
        values.put("remind_days_before", remindDaysBefore);
        values.put("create_time", System.currentTimeMillis());
        return helper.getWritableDatabase().insert("countdown_event", null, values);
    }

    public int delete(long id) {
        return helper.getWritableDatabase().delete("countdown_event", "id=?", new String[]{String.valueOf(id)});
    }

    public List<CountdownEvent> getAll() {
        List<CountdownEvent> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().query("countdown_event", null, null, null, null, null, "target_date ASC");
        try {
            while (c.moveToNext()) {
                CountdownEvent item = new CountdownEvent();
                item.id = c.getLong(c.getColumnIndexOrThrow("id"));
                item.title = c.getString(c.getColumnIndexOrThrow("title"));
                item.targetDate = c.getLong(c.getColumnIndexOrThrow("target_date"));
                item.description = c.getString(c.getColumnIndexOrThrow("description"));
                item.remindDaysBefore = c.getInt(c.getColumnIndexOrThrow("remind_days_before"));
                item.createTime = c.getLong(c.getColumnIndexOrThrow("create_time"));
                result.add(item);
            }
        } finally {
            c.close();
        }
        return result;
    }
}
