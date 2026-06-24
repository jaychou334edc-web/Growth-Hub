package com.growthhub.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.growthhub.R;
import com.growthhub.database.dao.AchievementDao;
import com.growthhub.database.entity.Achievement;
import com.growthhub.database.entity.FocusRecord;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.database.repository.FocusRepository;
import com.growthhub.database.repository.StatisticsRepository;
import com.growthhub.database.repository.TaskRepository;
import com.growthhub.model.StatItem;
import com.growthhub.ui.achievement.AchievementActivity;
import com.growthhub.ui.category.CategoryActivity;
import com.growthhub.ui.countdown.CountdownActivity;
import com.growthhub.ui.quote.QuoteActivity;
import com.growthhub.ui.task.TagActivity;
import com.growthhub.ui.task.TaskActivity;
import com.growthhub.util.TimeUtils;

import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private View content;
    private View heroCard;
    private View statsGrid;
    private View summaryCard;
    private View achievementCard;
    private View managementCard;
    private View dataCard;
    private View settingsCard;
    private TextView growthLevel;
    private TextView growthScore;
    private TextView totalFocus;
    private TextView currentStreak;
    private TextView achievementCount;
    private TextView statFocus;
    private TextView statSessions;
    private TextView statActive;
    private TextView statCategories;
    private TextView summaryCategory;
    private TextView summaryDay;
    private TextView summaryLongest;
    private TextView achievementTitle;
    private TextView achievementDesc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        bindViews(root);
        bindNavigation(root);
        bindComingSoon(root);
        animatePage();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void bindViews(View root) {
        content = root.findViewById(R.id.profile_content);
        heroCard = root.findViewById(R.id.profile_hero_card);
        statsGrid = root.findViewById(R.id.profile_stats_grid);
        summaryCard = root.findViewById(R.id.profile_summary_card);
        achievementCard = root.findViewById(R.id.profile_achievement);
        managementCard = root.findViewById(R.id.profile_management_card);
        dataCard = root.findViewById(R.id.profile_data_card);
        settingsCard = root.findViewById(R.id.profile_settings_card);
        growthLevel = root.findViewById(R.id.profile_growth_level);
        growthScore = root.findViewById(R.id.profile_growth_score);
        totalFocus = root.findViewById(R.id.profile_total_focus);
        currentStreak = root.findViewById(R.id.profile_current_streak);
        achievementCount = root.findViewById(R.id.profile_achievement_count);
        statFocus = root.findViewById(R.id.profile_stat_focus);
        statSessions = root.findViewById(R.id.profile_stat_sessions);
        statActive = root.findViewById(R.id.profile_stat_active);
        statCategories = root.findViewById(R.id.profile_stat_categories);
        summaryCategory = root.findViewById(R.id.profile_summary_category);
        summaryDay = root.findViewById(R.id.profile_summary_day);
        summaryLongest = root.findViewById(R.id.profile_summary_longest);
        achievementTitle = root.findViewById(R.id.profile_achievement_title);
        achievementDesc = root.findViewById(R.id.profile_achievement_desc);
    }

    private void bindNavigation(View root) {
        bind(root, R.id.profile_category, CategoryActivity.class);
        bind(root, R.id.profile_task, TaskActivity.class);
        bind(root, R.id.profile_tag, TagActivity.class);
        bind(root, R.id.profile_countdown, CountdownActivity.class);
        bind(root, R.id.profile_quote, QuoteActivity.class);
        root.findViewById(R.id.profile_achievement).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AchievementActivity.class)));
    }

    private void bindComingSoon(View root) {
        int[] ids = {
                R.id.profile_export,
                R.id.profile_backup,
                R.id.profile_restore,
                R.id.profile_notification,
                R.id.profile_about,
                R.id.profile_theme
        };
        for (int id : ids) {
            root.findViewById(id).setOnClickListener(v ->
                    Toast.makeText(requireContext(), "V2.2 即将上线", Toast.LENGTH_SHORT).show());
        }
    }

    private void bind(View root, int id, Class<?> activityClass) {
        View button = root.findViewById(id);
        button.setOnClickListener(v -> startActivity(new Intent(requireContext(), activityClass)));
    }

    private void render() {
        StatisticsRepository statisticsRepository = new StatisticsRepository(requireContext());
        TaskRepository taskRepository = new TaskRepository(requireContext());
        FocusRepository focusRepository = new FocusRepository(requireContext());
        AchievementDao achievementDao = new AchievementDao(GrowthHubDbHelper.getInstance(requireContext()));

        long totalSeconds = statisticsRepository.engine().totalDuration();
        int sessions = statisticsRepository.engine().totalCount();
        int activeRate = statisticsRepository.engine().activeRate30DaysPercent();
        int streak = currentStreak(statisticsRepository.recentDailyDurations(30));
        int categories = taskRepository.getCategories().size();
        List<Achievement> achievements = achievementDao.getAll();
        int unlocked = countUnlocked(achievements);
        int score = growthScore(totalSeconds, sessions, activeRate, streak, unlocked);
        int level = Math.max(1, score / 250 + 1);

        growthLevel.setText("Level " + level + " · " + levelTitle(level));
        growthScore.setText("Growth Score · " + score);
        totalFocus.setText(formatHeroDuration(totalSeconds));
        currentStreak.setText("连续专注 · " + streak + " 天");
        achievementCount.setText(unlocked + "/" + achievements.size() + " 枚成就");
        statFocus.setText("专注时长\n" + formatHeroDuration(totalSeconds));
        statSessions.setText("专注次数\n" + sessions);
        statActive.setText("活跃率\n" + activeRate + "%");
        statCategories.setText("分类\n" + categories);

        renderSummary(statisticsRepository, focusRepository);
        renderAchievementSnapshot(achievementDao, unlocked, achievements.size());
    }

    private void renderSummary(StatisticsRepository statisticsRepository, FocusRepository focusRepository) {
        StatItem category = first(statisticsRepository.engine().categoryRanking(0));
        StatItem bestDay = bestDay(statisticsRepository.recentDailyDurations(30));
        FocusRecord longest = longestSession(focusRepository.getRecent(500));

        summaryCategory.setText("最专注分类 · " +
                (category == null ? "暂无数据" : category.label + " · " + formatCompactDuration(category.value)));
        summaryDay.setText("最高效日期 · " +
                (bestDay == null ? "暂无数据" : bestDay.label + " · " + formatCompactDuration(bestDay.value)));
        summaryLongest.setText("最长专注 · " +
                (longest == null ? "暂无数据" : formatCompactDuration(longest.duration) + " · " + TimeUtils.formatDateTime(longest.startTime)));
    }

    private void renderAchievementSnapshot(AchievementDao achievementDao, int unlocked, int total) {
        Achievement latest = achievementDao.getLatestUnlocked();
        achievementTitle.setText("成就快照");
        if (latest == null) {
            achievementDesc.setText("已解锁 " + unlocked + "/" + total + " · 完成专注即可获得第一枚成就。");
            return;
        }
        achievementDesc.setText("已解锁 " + unlocked + "/" + total + " · 最新：" + latest.title);
    }

    private int currentStreak(List<StatItem> days) {
        int streak = 0;
        for (int i = days.size() - 1; i >= 0; i--) {
            if (days.get(i).value > 0) streak++;
            else if (streak > 0) break;
        }
        return streak;
    }

    private int countUnlocked(List<Achievement> achievements) {
        int count = 0;
        for (Achievement achievement : achievements) {
            if (achievement.unlockTime > 0) count++;
        }
        return count;
    }

    private int growthScore(long totalSeconds, int sessions, int activeRate, int streak, int unlocked) {
        long minutes = totalSeconds / 60;
        return (int) Math.min(9999, minutes + sessions * 12L + activeRate * 4L + streak * 25L + unlocked * 40L);
    }

    private String levelTitle(int level) {
        if (level >= 7) return "专注大师";
        if (level >= 6) return "深度工作者";
        if (level >= 5) return "成长探索者";
        if (level >= 4) return "持续成长者";
        if (level >= 3) return "稳定建设者";
        if (level >= 2) return "专注学习者";
        return "初学者";
    }

    private StatItem first(List<StatItem> items) {
        return items.isEmpty() ? null : items.get(0);
    }

    private StatItem bestDay(List<StatItem> days) {
        StatItem best = null;
        for (StatItem day : days) {
            if (best == null || day.value > best.value) best = day;
        }
        return best == null || best.value == 0 ? null : best;
    }

    private FocusRecord longestSession(List<FocusRecord> records) {
        FocusRecord best = null;
        for (FocusRecord record : records) {
            if (best == null || record.duration > best.duration) best = record;
        }
        return best;
    }

    private String formatHeroDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0 && minutes > 0) return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        if (hours > 0) return hours + "h";
        if (minutes > 0) return minutes + "m";
        return seconds + "s";
    }

    private String formatCompactDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0 && minutes > 0) return hours + "h " + minutes + "m";
        if (hours > 0) return hours + "h";
        if (minutes > 0) return minutes + "m";
        return seconds + "s";
    }

    private void animatePage() {
        content.setAlpha(0f);
        content.setTranslationY(dp(18));
        content.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(260)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        animateIn(heroCard, 40);
        animateIn(statsGrid, 130);
        animateIn(summaryCard, 220);
        animateIn(achievementCard, 310);
        animateIn(managementCard, 400);
        animateIn(dataCard, 490);
        animateIn(settingsCard, 580);
    }

    private void animateIn(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(dp(24));
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(360)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
