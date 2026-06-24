package com.growthhub.ui.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.growthhub.R;
import com.growthhub.data.BackupManager;
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
import com.growthhub.ui.data.ExportActivity;
import com.growthhub.ui.data.RestoreActivity;
import com.growthhub.ui.quote.QuoteActivity;
import com.growthhub.ui.report.WeeklyReportActivity;
import com.growthhub.ui.settings.SettingsActivity;
import com.growthhub.ui.task.TagActivity;
import com.growthhub.ui.task.TaskActivity;
import com.growthhub.util.DurationFormatter;
import com.growthhub.util.TimeUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {
    private View content;
    private View heroCard;
    private View statsGrid;
    private View summaryCard;
    private View weeklyReportCard;
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
    private ExecutorService dataExecutor;
    private final ActivityResultLauncher<String> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted && isAdded()) {
                    backup();
                } else if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.storage_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dataExecutor = Executors.newSingleThreadExecutor();
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        bindViews(root);
        bindNavigation(root);
        bindDataCenter(root);
        bindSettings(root);
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
        weeklyReportCard = root.findViewById(R.id.profile_weekly_report);
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
        bind(root, R.id.profile_weekly_report, WeeklyReportActivity.class);
        root.findViewById(R.id.profile_achievement).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AchievementActivity.class)));
    }

    private void bindDataCenter(View root) {
        bind(root, R.id.profile_export, ExportActivity.class);
        bind(root, R.id.profile_restore, RestoreActivity.class);
        root.findViewById(R.id.profile_backup).setOnClickListener(v -> backupWithPermission());
    }

    private void bindSettings(View root) {
        bind(root, R.id.profile_notification, SettingsActivity.class);
        bind(root, R.id.profile_about, SettingsActivity.class);
        bind(root, R.id.profile_theme, SettingsActivity.class);
    }

    private void backupWithPermission() {
        if (needsLegacyStoragePermission()) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }
        backup();
    }

    private void backup() {
        Context appContext = requireContext().getApplicationContext();
        Toast.makeText(requireContext(), R.string.backup_running, Toast.LENGTH_SHORT).show();
        if (dataExecutor == null || dataExecutor.isShutdown()) {
            dataExecutor = Executors.newSingleThreadExecutor();
        }
        dataExecutor.execute(() -> {
            try {
                String path = new BackupManager(appContext).backupDatabase();
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), getString(R.string.backup_success_path, path), Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                String message = errorMessage(e, getString(R.string.backup_failed));
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), getString(R.string.backup_failed_reason, message), Toast.LENGTH_LONG).show());
            }
        });
    }

    private boolean needsLegacyStoragePermission() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    private String errorMessage(Exception e, String fallback) {
        return e.getMessage() == null || e.getMessage().length() == 0 ? fallback : e.getMessage();
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

        growthLevel.setText(getString(R.string.profile_growth_level, level, levelTitle(level)));
        growthScore.setText(getString(R.string.profile_growth_score, score));
        totalFocus.setText(DurationFormatter.format(totalSeconds));
        currentStreak.setText(getString(R.string.profile_streak_days, streak));
        achievementCount.setText(getString(R.string.profile_achievement_count, unlocked, achievements.size()));
        statFocus.setText(getString(R.string.profile_stat_focus, DurationFormatter.format(totalSeconds)));
        statSessions.setText(getString(R.string.profile_stat_sessions, sessions));
        statActive.setText(getString(R.string.profile_stat_active, activeRate));
        statCategories.setText(getString(R.string.profile_stat_categories, categories));

        renderSummary(statisticsRepository, focusRepository);
        renderAchievementSnapshot(achievementDao, unlocked, achievements.size());
    }

    private void renderSummary(StatisticsRepository statisticsRepository, FocusRepository focusRepository) {
        StatItem category = first(statisticsRepository.engine().categoryRanking(0));
        StatItem bestDay = bestDay(statisticsRepository.recentDailyDurations(30));
        FocusRecord longest = longestSession(focusRepository.getRecent(500));

        summaryCategory.setText(getString(R.string.profile_summary_category,
                category == null ? getString(R.string.common_no_data) : category.label + " · " + DurationFormatter.format(category.value)));
        summaryDay.setText(getString(R.string.profile_summary_day,
                bestDay == null ? getString(R.string.common_no_data) : bestDay.label + " · " + DurationFormatter.format(bestDay.value)));
        summaryLongest.setText(getString(R.string.profile_summary_longest,
                longest == null ? getString(R.string.common_no_data) : DurationFormatter.format(longest.duration) + " · " + TimeUtils.formatDateTime(longest.startTime)));
    }

    private void renderAchievementSnapshot(AchievementDao achievementDao, int unlocked, int total) {
        Achievement latest = achievementDao.getLatestUnlocked();
        achievementTitle.setText(R.string.profile_achievement_snapshot);
        if (latest == null) {
            achievementDesc.setText(getString(R.string.profile_unlocked_first_hint, unlocked, total));
            return;
        }
        achievementDesc.setText(getString(R.string.profile_latest_achievement, unlocked, total, latest.title));
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
        if (level >= 7) return getString(R.string.profile_level_7);
        if (level >= 6) return getString(R.string.profile_level_6);
        if (level >= 5) return getString(R.string.profile_level_5);
        if (level >= 4) return getString(R.string.profile_level_4);
        if (level >= 3) return getString(R.string.profile_level_3);
        if (level >= 2) return getString(R.string.profile_level_2);
        return getString(R.string.profile_level_1);
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
        animateIn(weeklyReportCard, 310);
        animateIn(achievementCard, 400);
        animateIn(managementCard, 490);
        animateIn(dataCard, 580);
        animateIn(settingsCard, 670);
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

    @Override
    public void onDestroyView() {
        if (dataExecutor != null) {
            dataExecutor.shutdownNow();
            dataExecutor = null;
        }
        super.onDestroyView();
    }
}
