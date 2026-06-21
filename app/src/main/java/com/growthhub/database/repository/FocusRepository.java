package com.growthhub.database.repository;

import android.content.Context;

import com.growthhub.achievement.AchievementEngine;
import com.growthhub.database.dao.FocusRecordDao;
import com.growthhub.database.entity.FocusRecord;
import com.growthhub.database.helper.GrowthHubDbHelper;

import java.util.List;

public class FocusRepository {
    private final Context context;
    private final FocusRecordDao focusRecordDao;

    public FocusRepository(Context context) {
        this.context = context.getApplicationContext();
        focusRecordDao = new FocusRecordDao(GrowthHubDbHelper.getInstance(context));
    }

    public long addFocusRecord(FocusRecord record) {
        long id = focusRecordDao.insert(record);
        if (id > 0) {
            new AchievementEngine(context).check();
        }
        return id;
    }

    public List<String> getHistoryText() {
        return focusRecordDao.getHistoryText();
    }

    public List<FocusRecord> getRecent(int limit) {
        return focusRecordDao.getRecent(limit);
    }
}
