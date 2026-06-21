package com.growthhub.database.repository;

import android.content.Context;

import com.growthhub.database.dao.FocusRecordDao;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.model.StatItem;
import com.growthhub.statistics.StatisticsEngine;

import java.util.List;

public class StatisticsRepository {
    private final StatisticsEngine engine;
    private final FocusRecordDao focusRecordDao;

    public StatisticsRepository(Context context) {
        focusRecordDao = new FocusRecordDao(GrowthHubDbHelper.getInstance(context));
        engine = new StatisticsEngine(focusRecordDao);
    }

    public StatisticsEngine engine() {
        return engine;
    }

    public List<StatItem> recentDailyDurations(int days) {
        long start = System.currentTimeMillis() - (days - 1L) * 24L * 60L * 60L * 1000L;
        return focusRecordDao.dailyDurations(start, days);
    }
}
