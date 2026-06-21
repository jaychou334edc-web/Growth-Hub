package com.growthhub.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.growthhub.database.entity.Achievement;
import com.growthhub.database.helper.GrowthHubDbHelper;

import java.util.ArrayList;
import java.util.List;

public class AchievementDao {
    private final GrowthHubDbHelper helper;

    public AchievementDao(GrowthHubDbHelper helper) {
        this.helper = helper;
    }

    public List<Achievement> getAll() {
        List<Achievement> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().query("achievement", null, null, null, null, null, "id ASC");
        try {
            while (c.moveToNext()) result.add(map(c));
        } finally {
            c.close();
        }
        return result;
    }

    public List<Achievement> getLocked() {
        List<Achievement> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().query("achievement", null, "unlock_time=0", null, null, null, "id ASC");
        try {
            while (c.moveToNext()) result.add(map(c));
        } finally {
            c.close();
        }
        return result;
    }

    public Achievement getLatestUnlocked() {
        Cursor c = helper.getReadableDatabase().query("achievement", null, "unlock_time>0", null, null, null,
                "unlock_time DESC", "1");
        try {
            if (c.moveToFirst()) return map(c);
            return null;
        } finally {
            c.close();
        }
    }

    public boolean unlockIfLocked(long id, long unlockTime) {
        ContentValues values = new ContentValues();
        values.put("unlock_time", unlockTime);
        return helper.getWritableDatabase().update("achievement", values, "id=? AND unlock_time=0",
                new String[]{String.valueOf(id)}) == 1;
    }

    private Achievement map(Cursor c) {
        Achievement item = new Achievement();
        item.id = c.getLong(c.getColumnIndexOrThrow("id"));
        item.title = c.getString(c.getColumnIndexOrThrow("title"));
        item.description = c.getString(c.getColumnIndexOrThrow("description"));
        item.unlockTime = c.getLong(c.getColumnIndexOrThrow("unlock_time"));
        item.conditionType = c.getInt(c.getColumnIndexOrThrow("condition_type"));
        item.conditionValue = c.getLong(c.getColumnIndexOrThrow("condition_value"));
        return item;
    }
}
