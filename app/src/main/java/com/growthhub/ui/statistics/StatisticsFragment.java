package com.growthhub.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.growthhub.R;
import com.growthhub.database.dao.AchievementDao;
import com.growthhub.database.entity.Achievement;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.database.repository.StatisticsRepository;
import com.growthhub.model.StatItem;
import com.growthhub.util.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {
    private View content;
    private View heroCard;
    private View trendCard;
    private View heatmapCard;
    private View insightsGrid;
    private View achievementCard;
    private View distributionCard;
    private TextView totalFocus;
    private TextView weekTrend;
    private TextView currentStreak;
    private TextView activeRate;
    private TextView achievementCount;
    private TextView achievementPercent;
    private GridLayout heatmapGrid;
    private CircularProgressView achievementRing;
    private LineChart lineChart;
    private BarChart barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);
        content = root.findViewById(R.id.stat_content);
        heroCard = root.findViewById(R.id.stat_hero_card);
        trendCard = root.findViewById(R.id.stat_trend_card);
        heatmapCard = root.findViewById(R.id.stat_heatmap_card);
        insightsGrid = root.findViewById(R.id.stat_insights_grid);
        achievementCard = root.findViewById(R.id.stat_achievement_card);
        distributionCard = root.findViewById(R.id.stat_distribution_card);
        totalFocus = root.findViewById(R.id.stat_total_focus);
        weekTrend = root.findViewById(R.id.stat_week_trend);
        currentStreak = root.findViewById(R.id.stat_current_streak);
        activeRate = root.findViewById(R.id.stat_active_rate);
        achievementCount = root.findViewById(R.id.stat_achievement_count);
        achievementPercent = root.findViewById(R.id.stat_achievement_percent);
        heatmapGrid = root.findViewById(R.id.stat_heatmap_grid);
        achievementRing = root.findViewById(R.id.stat_achievement_ring);
        lineChart = root.findViewById(R.id.stat_line_chart);
        barChart = root.findViewById(R.id.stat_bar_chart);
        render(root);
        animatePage();
        return root;
    }

    private void render(View root) {
        StatisticsRepository repo = new StatisticsRepository(requireContext());
        List<StatItem> daily7 = repo.recentDailyDurations(7);
        List<StatItem> daily14 = repo.recentDailyDurations(14);
        List<StatItem> daily30 = repo.recentDailyDurations(30);
        List<StatItem> taskRanking = repo.engine().taskRanking(0);
        List<StatItem> categoryRanking = repo.engine().categoryRanking(0);
        List<StatItem> tagRanking = repo.engine().tagRanking(0);
        List<Achievement> achievements = new AchievementDao(GrowthHubDbHelper.getInstance(requireContext())).getAll();

        totalFocus.setText(formatHeroDuration(repo.engine().totalDuration()));
        currentStreak.setText("Streak · " + currentStreak(daily30) + " days");
        activeRate.setText("Active · " + repo.engine().activeRate30DaysPercent() + "%");
        weekTrend.setText(weeklyTrendText(daily14));

        styleLineChart(lineChart);
        lineChart.setData(lineData(daily7));
        lineChart.animateX(700);

        renderHeatmap(daily30);
        renderInsights(root, daily7, taskRanking, categoryRanking, tagRanking);
        renderAchievements(achievements);

        styleBarChart(barChart, categoryRanking);
        barChart.setData(barData(categoryRanking));
        barChart.animateY(700);
    }

    private void renderInsights(View root, List<StatItem> daily7, List<StatItem> taskRanking, List<StatItem> categoryRanking, List<StatItem> tagRanking) {
        StatItem topTask = first(taskRanking);
        StatItem topCategory = first(categoryRanking);
        StatItem topTag = first(tagRanking);
        StatItem bestDay = bestDay(daily7);
        bindInsight(root, R.id.stat_insight_task, "MOST FOCUSED TASK", topTask == null ? "No data" : topTask.label,
                topTask == null ? "Start a session" : TimeUtils.formatDuration(topTask.value));
        bindInsight(root, R.id.stat_insight_day, "MOST PRODUCTIVE DAY", bestDay == null ? "No data" : shortDate(bestDay.label),
                bestDay == null ? "No activity yet" : TimeUtils.formatDuration(bestDay.value));
        bindInsight(root, R.id.stat_insight_category, "TOP CATEGORY", topCategory == null ? "No data" : topCategory.label,
                topCategory == null ? "No category yet" : TimeUtils.formatDuration(topCategory.value));
        bindInsight(root, R.id.stat_insight_tag, "TOP TAG", topTag == null ? "No data" : topTag.label,
                topTag == null ? "No tag yet" : TimeUtils.formatDuration(topTag.value));
    }

    private void bindInsight(View root, int id, String title, String value, String metric) {
        View item = root.findViewById(id);
        ((TextView) item.findViewById(R.id.stat_insight_title)).setText(title);
        ((TextView) item.findViewById(R.id.stat_insight_value)).setText(value);
        ((TextView) item.findViewById(R.id.stat_insight_metric)).setText(metric);
    }

    private void renderAchievements(List<Achievement> achievements) {
        int unlocked = 0;
        for (Achievement achievement : achievements) {
            if (achievement.unlockTime > 0) unlocked++;
        }
        int total = achievements.size();
        int percent = total == 0 ? 0 : unlocked * 100 / total;
        achievementCount.setText(unlocked + " unlocked · " + total + " total");
        achievementPercent.setText(percent + "% complete");
        achievementRing.setProgress(percent);
    }

    private void renderHeatmap(List<StatItem> days) {
        heatmapGrid.removeAllViews();
        long max = 0;
        for (StatItem day : days) max = Math.max(max, day.value);
        for (int i = 0; i < days.size(); i++) {
            View cell = new View(requireContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dp(22);
            params.height = dp(22);
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            cell.setLayoutParams(params);
            cell.setBackgroundResource(heatDrawable(days.get(i).value, max));
            cell.setAlpha(0f);
            cell.animate().alpha(1f).setStartDelay(i * 18L).setDuration(220).start();
            heatmapGrid.addView(cell);
        }
    }

    private int heatDrawable(long value, long max) {
        if (value <= 0 || max <= 0) return R.drawable.bg_stat_heat_0;
        float ratio = value / (float) max;
        if (ratio < 0.25f) return R.drawable.bg_stat_heat_1;
        if (ratio < 0.50f) return R.drawable.bg_stat_heat_2;
        if (ratio < 0.75f) return R.drawable.bg_stat_heat_3;
        return R.drawable.bg_stat_heat_4;
    }

    private LineData lineData(List<StatItem> daily) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < daily.size(); i++) {
            entries.add(new Entry(i, daily.get(i).value / 60f));
        }
        LineDataSet set = new LineDataSet(entries, "Minutes");
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.18f);
        set.setColor(color(R.color.stat_accent_green));
        set.setCircleColor(color(R.color.stat_text_primary));
        set.setCircleRadius(4f);
        set.setLineWidth(3f);
        set.setDrawFilled(true);
        set.setFillColor(color(R.color.stat_accent_green));
        set.setFillAlpha(58);
        set.setDrawValues(false);
        LineData data = new LineData(set);
        data.setValueTextColor(color(R.color.stat_text_secondary));
        return data;
    }

    private BarData barData(List<StatItem> ranking) {
        List<BarEntry> entries = new ArrayList<>();
        int size = Math.min(5, ranking.size());
        for (int i = 0; i < size; i++) {
            entries.add(new BarEntry(i, ranking.get(i).value / 60f));
        }
        BarDataSet set = new BarDataSet(entries, "Minutes");
        set.setColor(color(R.color.stat_accent_blue));
        set.setValueTextColor(color(R.color.stat_text_secondary));
        set.setValueTextSize(10f);
        BarData data = new BarData(set);
        data.setBarWidth(0.58f);
        return data;
    }

    private void styleLineChart(LineChart chart) {
        chart.setNoDataText("No focus data yet");
        chart.setNoDataTextColor(color(R.color.stat_text_secondary));
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        styleAxis(chart.getAxisLeft());
        chart.getAxisRight().setEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(color(R.color.stat_text_muted));
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value + 1);
            }
        });
    }

    private void styleBarChart(BarChart chart, List<StatItem> ranking) {
        chart.setNoDataText("No distribution data yet");
        chart.setNoDataTextColor(color(R.color.stat_text_secondary));
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        styleAxis(chart.getAxisLeft());
        chart.getAxisRight().setEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(color(R.color.stat_text_muted));
        xAxis.setGranularity(1f);
        List<String> labels = new ArrayList<>();
        int size = Math.min(5, ranking.size());
        for (int i = 0; i < size; i++) labels.add(shortLabel(ranking.get(i).label));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
    }

    private void styleAxis(YAxis axis) {
        axis.setDrawAxisLine(false);
        axis.setGridColor(Color.argb(38, 255, 255, 255));
        axis.setTextColor(color(R.color.stat_text_muted));
        axis.setAxisMinimum(0f);
    }

    private String weeklyTrendText(List<StatItem> daily14) {
        if (daily14.size() < 14) return "No weekly trend yet";
        long previous = 0;
        long current = 0;
        for (int i = 0; i < daily14.size(); i++) {
            if (i < 7) previous += daily14.get(i).value;
            else current += daily14.get(i).value;
        }
        if (previous == 0 && current == 0) return "No weekly trend yet";
        if (previous == 0) return "↑ New focus week";
        int percent = (int) Math.round((current - previous) * 100.0 / previous);
        String arrow = percent >= 0 ? "↑ " : "↓ ";
        return arrow + Math.abs(percent) + "% vs last week";
    }

    private int currentStreak(List<StatItem> days) {
        int streak = 0;
        for (int i = days.size() - 1; i >= 0; i--) {
            if (days.get(i).value > 0) streak++;
            else if (streak > 0) break;
        }
        return streak;
    }

    private StatItem bestDay(List<StatItem> days) {
        StatItem best = null;
        for (StatItem day : days) {
            if (best == null || day.value > best.value) best = day;
        }
        return best == null || best.value == 0 ? null : best;
    }

    private StatItem first(List<StatItem> items) {
        return items.isEmpty() ? null : items.get(0);
    }

    private String formatHeroDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0) return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        if (minutes > 0) return minutes + "m";
        return seconds + "s";
    }

    private String shortDate(String label) {
        if (label == null || label.length() < 5) return "Today";
        return label.substring(Math.max(0, label.length() - 5));
    }

    private String shortLabel(String label) {
        if (label == null) return "";
        return label.length() > 4 ? label.substring(0, 4) : label;
    }

    private void animatePage() {
        content.setAlpha(0f);
        content.setTranslationY(18f);
        content.animate().alpha(1f).translationY(0f).setDuration(260).setInterpolator(new DecelerateInterpolator()).start();
        animateIn(heroCard, 40);
        animateIn(trendCard, 120);
        animateIn(heatmapCard, 200);
        animateIn(insightsGrid, 280);
        animateIn(achievementCard, 360);
        animateIn(distributionCard, 440);
    }

    private void animateIn(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(24f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(360)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private int color(int resId) {
        return ContextCompat.getColor(requireContext(), resId);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
