package com.growthhub.report;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.growthhub.database.dao.AchievementDao;
import com.growthhub.database.entity.Achievement;
import com.growthhub.database.entity.FocusRecord;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.database.repository.FocusRepository;
import com.growthhub.database.repository.StatisticsRepository;
import com.growthhub.model.StatItem;
import com.growthhub.util.DurationFormatter;
import com.growthhub.util.TimeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WeeklyReportGenerator {
    private static final String REPORT_DIR = "GrowthHub/reports";
    private static final String MIME_TEXT = "text/plain";
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final long DAY = 24L * 60L * 60L * 1000L;

    private final Context context;
    private final StatisticsRepository statisticsRepository;
    private final FocusRepository focusRepository;
    private final AchievementDao achievementDao;

    public WeeklyReportGenerator(Context context) {
        this.context = context.getApplicationContext();
        statisticsRepository = new StatisticsRepository(context);
        focusRepository = new FocusRepository(context);
        achievementDao = new AchievementDao(GrowthHubDbHelper.getInstance(context));
    }

    public WeeklyReport generate() {
        long now = System.currentTimeMillis();
        long weekStart = TimeUtils.startOfWeek();
        long previousWeekStart = weekStart - 7L * DAY;
        List<FocusRecord> records = focusRepository.getRecent(100000);

        List<FocusRecord> weekRecords = between(records, weekStart, now + 1);
        List<FocusRecord> previousWeekRecords = between(records, previousWeekStart, weekStart);
        long weekSeconds = sumDuration(weekRecords);
        long previousWeekSeconds = sumDuration(previousWeekRecords);
        int changePercent = changePercent(weekSeconds, previousWeekSeconds);
        int focusCount = weekRecords.size();
        long averageSeconds = focusCount == 0 ? 0 : weekSeconds / focusCount;
        int activeDays = activeDays(weekRecords);
        int activeRate = activeDays * 100 / 7;

        StatItem topCategory = first(statisticsRepository.engine().categoryRanking(weekStart));
        StatItem topTask = first(statisticsRepository.engine().taskRanking(weekStart));
        StatItem bestDay = bestDay(weekRecords);
        FocusRecord longest = longest(weekRecords);
        List<Achievement> weekAchievements = weekAchievements(weekStart, now + 1);
        GrowthLevel level = levelFor(weekSeconds);
        int score = growthScore(weekSeconds, activeDays, activeRate);
        String narrative = narrative(weekSeconds, changePercent, topCategory, longest, level);

        return new WeeklyReport(
                weekSeconds,
                previousWeekSeconds,
                changePercent,
                focusCount,
                averageSeconds,
                activeDays,
                activeRate,
                label(topCategory),
                value(topCategory),
                label(topTask),
                value(topTask),
                bestDay == null ? "暂无数据" : bestDay.label,
                bestDay == null ? 0 : bestDay.value,
                longest == null ? 0 : longest.duration,
                weekAchievements,
                level,
                score,
                narrative,
                renderText(weekSeconds, changePercent, focusCount, averageSeconds, activeDays, activeRate,
                        topCategory, topTask, bestDay, longest, weekAchievements, level, score, narrative));
    }

    public String exportTxt(WeeklyReport report) throws IOException {
        String fileName = "weekly_report_" +
                new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".txt";
        try (OutputStream out = openOutput(fileName)) {
            out.write(UTF8_BOM);
            out.write(report.text.getBytes(StandardCharsets.UTF_8));
        }
        return "Documents/GrowthHub/reports/" + fileName;
    }

    private List<FocusRecord> between(List<FocusRecord> records, long startInclusive, long endExclusive) {
        List<FocusRecord> result = new ArrayList<>();
        for (FocusRecord record : records) {
            if (record.startTime >= startInclusive && record.startTime < endExclusive) {
                result.add(record);
            }
        }
        return result;
    }

    private long sumDuration(List<FocusRecord> records) {
        long total = 0;
        for (FocusRecord record : records) {
            total += record.duration;
        }
        return total;
    }

    private int changePercent(long current, long previous) {
        if (previous <= 0) {
            return current > 0 ? 100 : 0;
        }
        return (int) Math.round((current - previous) * 100.0 / previous);
    }

    private int activeDays(List<FocusRecord> records) {
        Map<String, Boolean> days = new LinkedHashMap<>();
        for (FocusRecord record : records) {
            if (record.duration >= 60) {
                days.put(TimeUtils.formatDate(record.startTime), true);
            }
        }
        return days.size();
    }

    private StatItem bestDay(List<FocusRecord> records) {
        Map<String, Long> durations = new LinkedHashMap<>();
        for (FocusRecord record : records) {
            String day = TimeUtils.formatDate(record.startTime);
            Long current = durations.get(day);
            durations.put(day, (current == null ? 0 : current) + record.duration);
        }
        StatItem best = null;
        for (Map.Entry<String, Long> entry : durations.entrySet()) {
            if (best == null || entry.getValue() > best.value) {
                best = new StatItem(entry.getKey(), entry.getValue());
            }
        }
        return best == null || best.value == 0 ? null : best;
    }

    private FocusRecord longest(List<FocusRecord> records) {
        FocusRecord best = null;
        for (FocusRecord record : records) {
            if (best == null || record.duration > best.duration) {
                best = record;
            }
        }
        return best;
    }

    private List<Achievement> weekAchievements(long startInclusive, long endExclusive) {
        List<Achievement> result = new ArrayList<>();
        for (Achievement achievement : achievementDao.getAll()) {
            if (achievement.unlockTime >= startInclusive && achievement.unlockTime < endExclusive) {
                result.add(achievement);
            }
        }
        return result;
    }

    private GrowthLevel levelFor(long weekSeconds) {
        if (weekSeconds > 20L * 60L * 60L) return GrowthLevel.MASTER;
        if (weekSeconds >= 8L * 60L * 60L) return GrowthLevel.BUILDER;
        if (weekSeconds >= 2L * 60L * 60L) return GrowthLevel.EXPLORER;
        return GrowthLevel.BEGINNER;
    }

    private int growthScore(long weekSeconds, int activeDays, int activeRate) {
        int durationScore = (int) Math.min(60, Math.round(weekSeconds / 1200.0));
        int streakScore = Math.min(20, activeDays * 3);
        int activeScore = Math.min(20, activeRate / 5);
        return Math.min(100, durationScore + streakScore + activeScore);
    }

    private String narrative(long weekSeconds, int changePercent, StatItem topCategory, FocusRecord longest, GrowthLevel level) {
        String change = changePercent >= 0 ? "提升 " + changePercent + "%" : "下降 " + Math.abs(changePercent) + "%";
        String category = topCategory == null ? "暂未形成明显领域" : topCategory.label;
        String longestText = longest == null ? "暂无完整专注记录" : DurationFormatter.format(longest.duration);
        return "本周你累计专注 " + DurationFormatter.format(weekSeconds) + "，比上周" + change + "。\n\n" +
                "最专注的领域是" + category + "，最长一次专注达到 " + longestText + "。\n\n" +
                level.narrative + "继续保持。";
    }

    private String renderText(long weekSeconds, int changePercent, int focusCount, long averageSeconds,
                              int activeDays, int activeRate, StatItem topCategory, StatItem topTask,
                              StatItem bestDay, FocusRecord longest, List<Achievement> achievements,
                              GrowthLevel level, int score, String narrative) {
        StringBuilder builder = new StringBuilder();
        builder.append("Growth Hub 每周成长报告\n");
        builder.append("生成日期：").append(TimeUtils.formatDateTime(System.currentTimeMillis())).append("\n\n");
        builder.append("本周总专注时长：").append(DurationFormatter.format(weekSeconds)).append("\n");
        builder.append("较上周变化：").append(changePercent >= 0 ? "↑ " : "↓ ").append(Math.abs(changePercent)).append("%\n");
        builder.append("专注次数：").append(focusCount).append(" 次\n");
        builder.append("平均每次：").append(DurationFormatter.format(averageSeconds)).append("\n");
        builder.append("活跃天数：").append(activeDays).append("/7\n");
        builder.append("活跃率：").append(activeRate).append("%\n\n");
        builder.append("洞察\n");
        builder.append("最专注分类：").append(label(topCategory)).append("\n");
        builder.append("最专注任务：").append(label(topTask)).append("\n");
        builder.append("最高效率日期：").append(bestDay == null ? "暂无数据" : bestDay.label + " · " + DurationFormatter.format(bestDay.value)).append("\n");
        builder.append("最长专注记录：").append(longest == null ? "暂无数据" : DurationFormatter.format(longest.duration)).append("\n\n");
        builder.append("本周新解锁成就\n");
        if (achievements.isEmpty()) {
            builder.append("本周暂未解锁新成就，继续保持成长节奏。\n\n");
        } else {
            for (Achievement achievement : achievements) {
                builder.append("- ").append(achievement.title).append("：").append(achievement.description).append("\n");
            }
            builder.append("\n");
        }
        builder.append("成长评级：").append(level.title).append("\n");
        builder.append("Growth Score：").append(score).append("/100\n\n");
        builder.append("周报总结\n").append(narrative).append("\n");
        return builder.toString();
    }

    private StatItem first(List<StatItem> items) {
        return items == null || items.isEmpty() ? null : items.get(0);
    }

    private String label(StatItem item) {
        return item == null ? "暂无数据" : item.label;
    }

    private long value(StatItem item) {
        return item == null ? 0 : item.value;
    }

    private OutputStream openOutput(String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            String relativePath = Environment.DIRECTORY_DOCUMENTS + "/" + REPORT_DIR + "/";
            resolver.delete(collection,
                    MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " + MediaStore.MediaColumns.RELATIVE_PATH + "=?",
                    new String[]{fileName, relativePath});
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, MIME_TEXT);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
            Uri uri = resolver.insert(collection, values);
            if (uri == null) throw new IOException("无法创建周报文件");
            OutputStream out = resolver.openOutputStream(uri);
            if (out == null) throw new IOException("无法打开周报文件");
            return out;
        }
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), REPORT_DIR);
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("无法创建周报目录");
        return new FileOutputStream(new File(dir, fileName), false);
    }

    public enum GrowthLevel {
        BEGINNER("成长新手", "你正在建立最初的专注节奏，"),
        EXPLORER("成长探索者", "你已经开始形成稳定的成长节奏，"),
        BUILDER("持续建设者", "你本周的投入非常扎实，成长节奏正在持续强化，"),
        MASTER("成长大师", "你展现出高强度且稳定的自律状态，");

        public final String title;
        public final String narrative;

        GrowthLevel(String title, String narrative) {
            this.title = title;
            this.narrative = narrative;
        }
    }

    public static class WeeklyReport {
        public final long weekSeconds;
        public final long previousWeekSeconds;
        public final int changePercent;
        public final int focusCount;
        public final long averageSeconds;
        public final int activeDays;
        public final int activeRate;
        public final String topCategory;
        public final long topCategorySeconds;
        public final String topTask;
        public final long topTaskSeconds;
        public final String bestDay;
        public final long bestDaySeconds;
        public final long longestSeconds;
        public final List<Achievement> weekAchievements;
        public final GrowthLevel growthLevel;
        public final int growthScore;
        public final String narrative;
        public final String text;

        WeeklyReport(long weekSeconds, long previousWeekSeconds, int changePercent, int focusCount,
                     long averageSeconds, int activeDays, int activeRate, String topCategory,
                     long topCategorySeconds, String topTask, long topTaskSeconds, String bestDay,
                     long bestDaySeconds, long longestSeconds, List<Achievement> weekAchievements,
                     GrowthLevel growthLevel, int growthScore, String narrative, String text) {
            this.weekSeconds = weekSeconds;
            this.previousWeekSeconds = previousWeekSeconds;
            this.changePercent = changePercent;
            this.focusCount = focusCount;
            this.averageSeconds = averageSeconds;
            this.activeDays = activeDays;
            this.activeRate = activeRate;
            this.topCategory = topCategory;
            this.topCategorySeconds = topCategorySeconds;
            this.topTask = topTask;
            this.topTaskSeconds = topTaskSeconds;
            this.bestDay = bestDay;
            this.bestDaySeconds = bestDaySeconds;
            this.longestSeconds = longestSeconds;
            this.weekAchievements = weekAchievements;
            this.growthLevel = growthLevel;
            this.growthScore = growthScore;
            this.narrative = narrative;
            this.text = text;
        }
    }
}
