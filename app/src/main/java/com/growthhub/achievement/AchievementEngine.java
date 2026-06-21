package com.growthhub.achievement;

import android.content.Context;

import com.growthhub.constant.AchievementCondition;
import com.growthhub.database.dao.AchievementDao;
import com.growthhub.database.dao.FocusRecordDao;
import com.growthhub.database.entity.Achievement;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.notification.NotificationHelper;
import com.growthhub.statistics.StatisticsEngine;

public class AchievementEngine {
    private final AchievementDao achievementDao;
    private final StatisticsEngine statisticsEngine;
    private final NotificationHelper notificationHelper;

    public AchievementEngine(Context context) {
        GrowthHubDbHelper helper = GrowthHubDbHelper.getInstance(context);
        achievementDao = new AchievementDao(helper);
        statisticsEngine = new StatisticsEngine(new FocusRecordDao(helper));
        notificationHelper = new NotificationHelper(context);
    }

    public void check() {
        long now = System.currentTimeMillis();
        long total = statisticsEngine.totalDuration();
        int count = statisticsEngine.totalCount();
        int rate = statisticsEngine.activeRate30DaysPercent();

        for (Achievement achievement : achievementDao.getLocked()) {
            boolean matched = false;
            if (achievement.conditionType == AchievementCondition.FIRST_FOCUS) {
                matched = count >= achievement.conditionValue;
            } else if (achievement.conditionType == AchievementCondition.TOTAL_SECONDS) {
                matched = total >= achievement.conditionValue;
            } else if (achievement.conditionType == AchievementCondition.ACTIVE_RATE_PERCENT) {
                matched = rate >= achievement.conditionValue;
            }

            if (matched && achievementDao.unlockIfLocked(achievement.id, now)) {
                notificationHelper.notifyAchievement(achievement.title, achievement.description);
            }
        }
    }
}
