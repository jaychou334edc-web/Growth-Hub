package com.growthhub.statistics;

import com.growthhub.database.dao.FocusRecordDao;
import com.growthhub.model.StatItem;
import com.growthhub.util.TimeUtils;

import java.util.Calendar;
import java.util.List;

public class StatisticsEngine {
    private final FocusRecordDao focusRecordDao;

    public StatisticsEngine(FocusRecordDao focusRecordDao) {
        this.focusRecordDao = focusRecordDao;
    }

    public long todayDuration() {
        return focusRecordDao.sumDuration(TimeUtils.startOfToday(), System.currentTimeMillis() + 1);
    }

    public long weekDuration() {
        return focusRecordDao.sumDuration(TimeUtils.startOfWeek(), System.currentTimeMillis() + 1);
    }

    public long monthDuration() {
        return focusRecordDao.sumDuration(TimeUtils.startOfMonth(), System.currentTimeMillis() + 1);
    }

    public long totalDuration() {
        return focusRecordDao.sumAllDuration();
    }

    public int totalCount() {
        return focusRecordDao.countAll();
    }

    public long averageDuration() {
        int count = totalCount();
        return count == 0 ? 0 : totalDuration() / count;
    }

    public int activeRate30DaysPercent() {
        long day = 24L * 60L * 60L * 1000L;
        long today = TimeUtils.startOfToday();
        int activeDays = 0;
        for (int i = 0; i < 30; i++) {
            long start = today - i * day;
            long duration = focusRecordDao.sumDuration(start, start + day);
            if (duration >= 60) activeDays++;
        }
        return activeDays * 100 / 30;
    }

    public int monthFocusRatePercent() {
        Calendar c = Calendar.getInstance();
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        long day = 24L * 60L * 60L * 1000L;
        long monthStart = TimeUtils.startOfMonth();
        int activeDays = 0;
        for (int i = 0; i < dayOfMonth; i++) {
            long start = monthStart + i * day;
            if (focusRecordDao.sumDuration(start, start + day) >= 60) activeDays++;
        }
        return dayOfMonth == 0 ? 0 : activeDays * 100 / dayOfMonth;
    }

    public List<StatItem> taskRanking(long startInclusive) {
        return focusRecordDao.taskRanking(startInclusive);
    }

    public List<StatItem> categoryRanking(long startInclusive) {
        return focusRecordDao.categoryRanking(startInclusive);
    }

    public List<StatItem> tagRanking(long startInclusive) {
        return focusRecordDao.tagRanking(startInclusive);
    }
}
