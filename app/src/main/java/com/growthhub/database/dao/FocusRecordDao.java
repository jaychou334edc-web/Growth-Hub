package com.growthhub.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.growthhub.constant.FocusMode;
import com.growthhub.database.entity.FocusRecord;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.model.StatItem;
import com.growthhub.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class FocusRecordDao {
    private final GrowthHubDbHelper helper;

    public FocusRecordDao(GrowthHubDbHelper helper) {
        this.helper = helper;
    }

    public long insert(FocusRecord record) {
        ContentValues values = new ContentValues();
        values.put("task_id", record.taskId);
        values.put("start_time", record.startTime);
        values.put("end_time", record.endTime);
        values.put("duration", record.duration);
        values.put("mode", record.mode);
        values.put("mood", record.mood);
        values.put("note", record.note);
        values.put("create_time", record.createTime);
        return helper.getWritableDatabase().insert("focus_record", null, values);
    }

    public long sumDuration(long startInclusive, long endExclusive) {
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(duration),0) FROM focus_record WHERE start_time>=? AND start_time<?",
                new String[]{String.valueOf(startInclusive), String.valueOf(endExclusive)});
        try {
            return c.moveToFirst() ? c.getLong(0) : 0;
        } finally {
            c.close();
        }
    }

    public long sumAllDuration() {
        Cursor c = helper.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(duration),0) FROM focus_record", null);
        try {
            return c.moveToFirst() ? c.getLong(0) : 0;
        } finally {
            c.close();
        }
    }

    public int countAll() {
        Cursor c = helper.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM focus_record", null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    public List<FocusRecord> getRecent(int limit) {
        List<FocusRecord> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().query("focus_record", null, null, null, null, null,
                "start_time DESC", String.valueOf(limit));
        try {
            while (c.moveToNext()) result.add(map(c));
        } finally {
            c.close();
        }
        return result;
    }

    public List<String> getHistoryText() {
        List<String> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT f.start_time,f.end_time,f.duration,f.mode,f.note,t.title " +
                        "FROM focus_record f LEFT JOIN task t ON f.task_id=t.id " +
                        "ORDER BY f.start_time DESC LIMIT 100", null);
        try {
            while (c.moveToNext()) {
                String title = c.getString(5);
                String note = c.getString(4);
                String line = TimeUtils.formatDateTime(c.getLong(0)) + " - " + TimeUtils.formatDateTime(c.getLong(1)) +
                        "\n" + (title == null ? "未命名任务" : title) +
                        " | " + TimeUtils.formatDuration(c.getLong(2)) +
                        " | " + FocusMode.label(c.getInt(3));
                if (note != null && note.length() > 0) line += "\n备注：" + note;
                result.add(line);
            }
        } finally {
            c.close();
        }
        return result;
    }

    public List<StatItem> taskRanking(long startInclusive) {
        List<StatItem> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT COALESCE(t.title,'未命名任务'), SUM(f.duration) total " +
                        "FROM focus_record f LEFT JOIN task t ON f.task_id=t.id " +
                        "WHERE f.start_time>=? GROUP BY f.task_id ORDER BY total DESC LIMIT 10",
                new String[]{String.valueOf(startInclusive)});
        try {
            while (c.moveToNext()) result.add(new StatItem(c.getString(0), c.getLong(1)));
        } finally {
            c.close();
        }
        return result;
    }

    public List<StatItem> categoryRanking(long startInclusive) {
        List<StatItem> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT COALESCE(c.name,'未分类'), SUM(f.duration) total " +
                        "FROM focus_record f LEFT JOIN task t ON f.task_id=t.id " +
                        "LEFT JOIN category c ON t.category_id=c.id " +
                        "WHERE f.start_time>=? GROUP BY t.category_id ORDER BY total DESC LIMIT 10",
                new String[]{String.valueOf(startInclusive)});
        try {
            while (c.moveToNext()) result.add(new StatItem(c.getString(0), c.getLong(1)));
        } finally {
            c.close();
        }
        return result;
    }

    public List<StatItem> tagRanking(long startInclusive) {
        List<StatItem> result = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT tag.name, SUM(f.duration) total FROM focus_record f " +
                        "JOIN task_tag tt ON f.task_id=tt.task_id JOIN tag ON tt.tag_id=tag.id " +
                        "WHERE f.start_time>=? GROUP BY tag.id ORDER BY total DESC LIMIT 10",
                new String[]{String.valueOf(startInclusive)});
        try {
            while (c.moveToNext()) result.add(new StatItem(c.getString(0), c.getLong(1)));
        } finally {
            c.close();
        }
        return result;
    }

    public List<StatItem> dailyDurations(long startInclusive, int days) {
        List<StatItem> result = new ArrayList<>();
        long day = 24L * 60L * 60L * 1000L;
        for (int i = 0; i < days; i++) {
            long start = startInclusive + i * day;
            long end = start + day;
            result.add(new StatItem(TimeUtils.formatDate(start), sumDuration(start, end)));
        }
        return result;
    }

    private FocusRecord map(Cursor c) {
        FocusRecord item = new FocusRecord();
        item.id = c.getLong(c.getColumnIndexOrThrow("id"));
        item.taskId = c.getLong(c.getColumnIndexOrThrow("task_id"));
        item.startTime = c.getLong(c.getColumnIndexOrThrow("start_time"));
        item.endTime = c.getLong(c.getColumnIndexOrThrow("end_time"));
        item.duration = c.getLong(c.getColumnIndexOrThrow("duration"));
        item.mode = c.getInt(c.getColumnIndexOrThrow("mode"));
        item.mood = c.getInt(c.getColumnIndexOrThrow("mood"));
        item.note = c.getString(c.getColumnIndexOrThrow("note"));
        item.createTime = c.getLong(c.getColumnIndexOrThrow("create_time"));
        return item;
    }
}
