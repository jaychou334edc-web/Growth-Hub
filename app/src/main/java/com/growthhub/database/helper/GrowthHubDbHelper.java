package com.growthhub.database.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.growthhub.constant.AchievementCondition;

public class GrowthHubDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "growth_hub.db";
    private static final int DB_VERSION = 1;
    private static GrowthHubDbHelper instance;

    public static synchronized GrowthHubDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new GrowthHubDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private GrowthHubDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE category(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "create_time INTEGER)");

        db.execSQL("CREATE TABLE task(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category_id INTEGER," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "status INTEGER NOT NULL," +
                "create_time INTEGER," +
                "update_time INTEGER)");
        db.execSQL("CREATE INDEX idx_task_category ON task(category_id)");

        db.execSQL("CREATE TABLE tag(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "create_time INTEGER)");

        db.execSQL("CREATE TABLE task_tag(" +
                "task_id INTEGER," +
                "tag_id INTEGER)");
        db.execSQL("CREATE UNIQUE INDEX idx_task_tag ON task_tag(task_id, tag_id)");

        db.execSQL("CREATE TABLE focus_record(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "task_id INTEGER," +
                "start_time INTEGER," +
                "end_time INTEGER," +
                "duration INTEGER," +
                "mode INTEGER," +
                "mood INTEGER," +
                "note TEXT," +
                "create_time INTEGER)");
        db.execSQL("CREATE INDEX idx_focus_task ON focus_record(task_id)");
        db.execSQL("CREATE INDEX idx_focus_start_time ON focus_record(start_time)");

        db.execSQL("CREATE TABLE countdown_event(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "target_date INTEGER," +
                "description TEXT," +
                "remind_days_before INTEGER," +
                "create_time INTEGER)");

        db.execSQL("CREATE TABLE quote(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "content TEXT," +
                "is_system INTEGER," +
                "create_time INTEGER)");

        db.execSQL("CREATE TABLE achievement(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "description TEXT," +
                "unlock_time INTEGER," +
                "condition_type INTEGER," +
                "condition_value INTEGER)");

        seedAchievements(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // V1 has no migration yet. Future upgrades must use ALTER/COPY migration, never DROP tables.
    }

    private void seedAchievements(SQLiteDatabase db) {
        long now = System.currentTimeMillis();
        db.execSQL("INSERT INTO achievement(title,description,unlock_time,condition_type,condition_value) VALUES(?,?,?,?,?)",
                new Object[]{"首次专注", "完成第一条专注记录", 0, AchievementCondition.FIRST_FOCUS, 1});
        db.execSQL("INSERT INTO achievement(title,description,unlock_time,condition_type,condition_value) VALUES(?,?,?,?,?)",
                new Object[]{"累计10小时", "累计专注达到10小时", 0, AchievementCondition.TOTAL_SECONDS, 10 * 60 * 60});
        db.execSQL("INSERT INTO achievement(title,description,unlock_time,condition_type,condition_value) VALUES(?,?,?,?,?)",
                new Object[]{"累计50小时", "累计专注达到50小时", 0, AchievementCondition.TOTAL_SECONDS, 50 * 60 * 60});
        db.execSQL("INSERT INTO achievement(title,description,unlock_time,condition_type,condition_value) VALUES(?,?,?,?,?)",
                new Object[]{"累计100小时", "累计专注达到100小时", 0, AchievementCondition.TOTAL_SECONDS, 100 * 60 * 60});
        db.execSQL("INSERT INTO achievement(title,description,unlock_time,condition_type,condition_value) VALUES(?,?,?,?,?)",
                new Object[]{"近30天活跃率90%", "近30天活跃率达到90%", 0, AchievementCondition.ACTIVE_RATE_PERCENT, 90});
        db.execSQL("INSERT INTO category(name,description,create_time) VALUES(?,?,?)",
                new Object[]{"学习", "默认分类", now});
        db.execSQL("INSERT INTO task(category_id,title,description,status,create_time,update_time) VALUES(?,?,?,?,?,?)",
                new Object[]{1, "示例任务：阅读", "可直接用于第一次专注演示", 0, now, now});
    }
}
