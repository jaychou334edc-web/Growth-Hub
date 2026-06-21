package com.growthhub.ui.achievement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.growthhub.R;
import com.growthhub.constant.AchievementCondition;
import com.growthhub.database.dao.AchievementDao;
import com.growthhub.database.entity.Achievement;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.database.repository.StatisticsRepository;
import com.growthhub.statistics.StatisticsEngine;
import com.growthhub.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AchievementActivity extends AppCompatActivity {
    private View content;
    private View heroCard;
    private View nextCard;
    private View categoriesTitle;
    private View categoriesScroller;
    private View gridTitle;
    private View recentCard;
    private TextView unlockedCount;
    private TextView progressLabel;
    private TextView raritySummary;
    private TextView rareUnlocked;
    private TextView nextTitle;
    private TextView nextProgress;
    private AchievementProgressRingView progressRing;
    private LinearProgressIndicator nextBar;
    private LinearLayout categoriesRow;
    private LinearLayout recentList;
    private RecyclerView grid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);
        bindViews();
        render();
        animatePage();
    }

    private void bindViews() {
        content = findViewById(R.id.achievement_content);
        heroCard = findViewById(R.id.achievement_hero_card);
        nextCard = findViewById(R.id.achievement_next_card);
        categoriesTitle = findViewById(R.id.achievement_categories_title);
        categoriesScroller = findViewById(R.id.achievement_categories_scroller);
        gridTitle = findViewById(R.id.achievement_grid_title);
        recentCard = findViewById(R.id.achievement_recent_card);
        unlockedCount = findViewById(R.id.achievement_unlocked_count);
        progressLabel = findViewById(R.id.achievement_progress_label);
        raritySummary = findViewById(R.id.achievement_rarity_summary);
        rareUnlocked = findViewById(R.id.achievement_rare_unlocked);
        nextTitle = findViewById(R.id.achievement_next_title);
        nextProgress = findViewById(R.id.achievement_next_progress);
        progressRing = findViewById(R.id.achievement_progress_ring);
        nextBar = findViewById(R.id.achievement_next_bar);
        categoriesRow = findViewById(R.id.achievement_categories_row);
        recentList = findViewById(R.id.achievement_recent_list);
        grid = findViewById(R.id.achievement_grid);
        findViewById(R.id.achievement_back).setOnClickListener(v -> finish());
    }

    private void render() {
        AchievementDao dao = new AchievementDao(GrowthHubDbHelper.getInstance(this));
        StatisticsEngine stats = new StatisticsRepository(this).engine();
        List<Achievement> achievements = dao.getAll();
        List<AchievementAdapter.DisplayItem> displayItems = buildDisplayItems(achievements, stats);

        renderHero(achievements, displayItems);
        renderNextTarget(displayItems);
        renderCategories(displayItems);
        renderGrid(displayItems);
        renderRecentUnlocks(achievements);
    }

    private List<AchievementAdapter.DisplayItem> buildDisplayItems(List<Achievement> achievements, StatisticsEngine stats) {
        List<AchievementAdapter.DisplayItem> items = new ArrayList<>();
        long totalDuration = stats.totalDuration();
        int totalCount = stats.totalCount();
        int activeRate = stats.activeRate30DaysPercent();

        for (Achievement achievement : achievements) {
            long current = currentValue(achievement, totalDuration, totalCount, activeRate);
            int progress = progressPercent(current, achievement.conditionValue);
            boolean unlocked = achievement.unlockTime > 0;
            items.add(new AchievementAdapter.DisplayItem(
                    achievement,
                    tierName(achievement),
                    tierColor(achievement),
                    unlocked ? 100 : progress,
                    progressText(achievement, current),
                    unlocked,
                    !unlocked && progress >= 80
            ));
        }
        return items;
    }

    private void renderHero(List<Achievement> achievements, List<AchievementAdapter.DisplayItem> displayItems) {
        int unlocked = 0;
        int bronze = 0;
        int silver = 0;
        int gold = 0;
        int legend = 0;
        AchievementAdapter.DisplayItem rarestUnlocked = null;

        for (AchievementAdapter.DisplayItem item : displayItems) {
            if ("Bronze".equals(item.tier)) bronze++;
            else if ("Silver".equals(item.tier)) silver++;
            else if ("Gold".equals(item.tier)) gold++;
            else legend++;

            if (item.unlocked) {
                unlocked++;
                if (rarestUnlocked == null || tierRank(item.tier) > tierRank(rarestUnlocked.tier)) {
                    rarestUnlocked = item;
                }
            }
        }

        int total = achievements.size();
        int percent = total == 0 ? 0 : unlocked * 100 / total;
        unlockedCount.setText(String.format(Locale.getDefault(), "%d / %d", unlocked, total));
        progressLabel.setText(percent + "% complete");
        raritySummary.setText(String.format(Locale.getDefault(),
                "Bronze %d · Silver %d · Gold %d · Legend %d", bronze, silver, gold, legend));
        if (rarestUnlocked == null) {
            rareUnlocked.setText("Rarest unlocked: Complete your first focus session to claim a badge.");
        } else {
            rareUnlocked.setText("Rarest unlocked: " + rarestUnlocked.tier + " · " + rarestUnlocked.achievement.title);
        }
        progressRing.animateTo(percent);
    }

    private void renderNextTarget(List<AchievementAdapter.DisplayItem> items) {
        AchievementAdapter.DisplayItem next = null;
        for (AchievementAdapter.DisplayItem item : items) {
            if (item.unlocked) continue;
            if (next == null || item.progressPercent > next.progressPercent) {
                next = item;
            }
        }

        if (next == null) {
            nextTitle.setText("All awards unlocked");
            nextProgress.setText("Every current achievement has been claimed.");
            nextBar.setProgressCompat(100, true);
            return;
        }

        nextTitle.setText(next.achievement.title);
        nextProgress.setText(next.progressText);
        nextBar.setProgressCompat(next.progressPercent, true);
    }

    private void renderCategories(List<AchievementAdapter.DisplayItem> items) {
        categoriesRow.removeAllViews();
        addCategory("Focus Time", countByType(items, AchievementCondition.TOTAL_SECONDS));
        addCategory("Consistency", countByType(items, AchievementCondition.ACTIVE_RATE_PERCENT));
        addCategory("Task Completion", 0);
        addCategory("Special Milestones", countByType(items, AchievementCondition.FIRST_FOCUS));
    }

    private void addCategory(String title, int count) {
        TextView chip = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(74));
        params.setMargins(0, 0, dp(10), 0);
        chip.setLayoutParams(params);
        chip.setBackgroundResource(R.drawable.bg_achievement_category_chip);
        chip.setGravity(android.view.Gravity.CENTER_VERTICAL);
        chip.setMinWidth(dp(154));
        chip.setPadding(dp(16), 0, dp(16), 0);
        chip.setText(title + "\n" + count + " awards");
        chip.setTextColor(ContextCompat.getColor(this, R.color.achievement_text_primary));
        chip.setTextSize(14);
        chip.setTypeface(chip.getTypeface(), android.graphics.Typeface.BOLD);
        categoriesRow.addView(chip);
    }

    private void renderGrid(List<AchievementAdapter.DisplayItem> items) {
        grid.setLayoutManager(new GridLayoutManager(this, 2));
        grid.setAdapter(new AchievementAdapter(items));
    }

    private void renderRecentUnlocks(List<Achievement> achievements) {
        recentList.removeAllViews();
        List<Achievement> unlocked = new ArrayList<>();
        for (Achievement achievement : achievements) {
            if (achievement.unlockTime > 0) unlocked.add(achievement);
        }
        Collections.sort(unlocked, (left, right) -> Long.compare(right.unlockTime, left.unlockTime));
        LayoutInflater inflater = LayoutInflater.from(this);
        if (unlocked.isEmpty()) {
            addTimelineItem(inflater, "No unlocks yet", "Finish a focus session to start your awards timeline.");
            return;
        }
        int limit = Math.min(5, unlocked.size());
        for (int i = 0; i < limit; i++) {
            Achievement achievement = unlocked.get(i);
            addTimelineItem(inflater, achievement.title, TimeUtils.formatDateTime(achievement.unlockTime));
        }
    }

    private void addTimelineItem(LayoutInflater inflater, String title, String meta) {
        View item = inflater.inflate(R.layout.item_achievement_timeline, recentList, false);
        ((TextView) item.findViewById(R.id.achievement_timeline_title)).setText(title);
        ((TextView) item.findViewById(R.id.achievement_timeline_meta)).setText(meta);
        recentList.addView(item);
    }

    private int countByType(List<AchievementAdapter.DisplayItem> items, int type) {
        int count = 0;
        for (AchievementAdapter.DisplayItem item : items) {
            if (item.achievement.conditionType == type) count++;
        }
        return count;
    }

    private long currentValue(Achievement achievement, long totalDuration, int totalCount, int activeRate) {
        if (achievement.conditionType == AchievementCondition.FIRST_FOCUS) return totalCount;
        if (achievement.conditionType == AchievementCondition.TOTAL_SECONDS) return totalDuration;
        if (achievement.conditionType == AchievementCondition.ACTIVE_RATE_PERCENT) return activeRate;
        return 0;
    }

    private int progressPercent(long current, long target) {
        if (target <= 0) return 0;
        return (int) Math.max(0, Math.min(100, current * 100 / target));
    }

    private String progressText(Achievement achievement, long current) {
        long target = achievement.conditionValue;
        long remaining = Math.max(0, target - current);
        if (achievement.conditionType == AchievementCondition.TOTAL_SECONDS) {
            return formatHours(current) + " / " + formatHours(target) + " · " + formatHours(remaining) + " remaining";
        }
        if (achievement.conditionType == AchievementCondition.ACTIVE_RATE_PERCENT) {
            return current + "% / " + target + "% · " + remaining + "% remaining";
        }
        return current + " / " + target + " sessions · " + remaining + " remaining";
    }

    private String tierName(Achievement achievement) {
        if (achievement.conditionType == AchievementCondition.ACTIVE_RATE_PERCENT) {
            return achievement.conditionValue >= 90 ? "Legend" : "Gold";
        }
        if (achievement.conditionType == AchievementCondition.TOTAL_SECONDS) {
            long hours = achievement.conditionValue / 3600;
            if (hours >= 100) return "Legend";
            if (hours >= 50) return "Gold";
            if (hours >= 10) return "Silver";
        }
        return "Bronze";
    }

    private int tierColor(Achievement achievement) {
        String tier = tierName(achievement);
        if ("Legend".equals(tier)) return R.color.achievement_legend;
        if ("Gold".equals(tier)) return R.color.achievement_gold;
        if ("Silver".equals(tier)) return R.color.achievement_silver;
        return R.color.achievement_bronze;
    }

    private int tierRank(String tier) {
        if ("Legend".equals(tier)) return 4;
        if ("Gold".equals(tier)) return 3;
        if ("Silver".equals(tier)) return 2;
        return 1;
    }

    private String formatHours(long seconds) {
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
        animateIn(nextCard, 130);
        animateIn(categoriesTitle, 210);
        animateIn(categoriesScroller, 250);
        animateIn(gridTitle, 310);
        animateIn(grid, 350);
        animateIn(recentCard, 430);
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
