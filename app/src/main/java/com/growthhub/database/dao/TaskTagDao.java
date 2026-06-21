package com.growthhub.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.growthhub.database.helper.GrowthHubDbHelper;

import java.util.ArrayList;
import java.util.List;

public class TaskTagDao {
    private final GrowthHubDbHelper helper;

    public TaskTagDao(GrowthHubDbHelper helper) {
        this.helper = helper;
    }

    public long bind(long taskId, long tagId) {
        ContentValues values = new ContentValues();
        values.put("task_id", taskId);
        values.put("tag_id", tagId);
        return helper.getWritableDatabase().insertWithOnConflict("task_tag", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int unbind(long taskId, long tagId) {
        return helper.getWritableDatabase().delete("task_tag", "task_id=? AND tag_id=?",
                new String[]{String.valueOf(taskId), String.valueOf(tagId)});
    }

    public List<Long> getTagIds(long taskId) {
        List<Long> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().query("task_tag", new String[]{"tag_id"}, "task_id=?",
                new String[]{String.valueOf(taskId)}, null, null, null);
        try {
            while (c.moveToNext()) result.add(c.getLong(0));
        } finally {
            c.close();
        }
        return result;
    }
}
