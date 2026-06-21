package com.growthhub.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.growthhub.database.repository.StatisticsRepository;
import com.growthhub.model.StatItem;
import com.growthhub.util.TimeUtils;
import com.growthhub.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(root);

        StatisticsRepository repo = new StatisticsRepository(requireContext());
        root.addView(UiUtils.title(requireContext(), "统计中心"));
        root.addView(UiUtils.text(requireContext(), "今日：" + TimeUtils.formatDuration(repo.engine().todayDuration()), 16));
        root.addView(UiUtils.text(requireContext(), "本周：" + TimeUtils.formatDuration(repo.engine().weekDuration()), 16));
        root.addView(UiUtils.text(requireContext(), "本月：" + TimeUtils.formatDuration(repo.engine().monthDuration()), 16));
        root.addView(UiUtils.text(requireContext(), "累计：" + TimeUtils.formatDuration(repo.engine().totalDuration()), 16));
        root.addView(UiUtils.text(requireContext(), "次数：" + repo.engine().totalCount(), 16));
        root.addView(UiUtils.text(requireContext(), "平均：" + TimeUtils.formatDuration(repo.engine().averageDuration()), 16));
        root.addView(UiUtils.text(requireContext(), "近30天活跃率：" + repo.engine().activeRate30DaysPercent() + "%", 16));
        root.addView(UiUtils.text(requireContext(), "本月专注率：" + repo.engine().monthFocusRatePercent() + "%", 16));

        List<StatItem> daily = repo.recentDailyDurations(7);
        LineChart lineChart = new LineChart(requireContext());
        lineChart.setMinimumHeight(UiUtils.dp(requireContext(), 220));
        lineChart.setData(lineData(daily));
        lineChart.getDescription().setText("近7天折线图");
        root.addView(lineChart);

        List<StatItem> ranking = repo.engine().taskRanking(0);
        BarChart barChart = new BarChart(requireContext());
        barChart.setMinimumHeight(UiUtils.dp(requireContext(), 220));
        barChart.setData(barData(ranking));
        barChart.getDescription().setText("任务柱状图");
        root.addView(barChart);

        root.addView(UiUtils.subtitle(requireContext(), "任务排行"));
        for (StatItem item : ranking) {
            root.addView(UiUtils.text(requireContext(), item.label + "：" + TimeUtils.formatDuration(item.value), 15));
        }
        root.addView(UiUtils.subtitle(requireContext(), "分类排行"));
        for (StatItem item : repo.engine().categoryRanking(0)) {
            root.addView(UiUtils.text(requireContext(), item.label + "：" + TimeUtils.formatDuration(item.value), 15));
        }
        root.addView(UiUtils.subtitle(requireContext(), "标签排行"));
        for (StatItem item : repo.engine().tagRanking(0)) {
            root.addView(UiUtils.text(requireContext(), item.label + "：" + TimeUtils.formatDuration(item.value), 15));
        }
        return scrollView;
    }

    private LineData lineData(List<StatItem> daily) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < daily.size(); i++) entries.add(new Entry(i, daily.get(i).value / 60f));
        LineDataSet set = new LineDataSet(entries, "分钟");
        set.setColor(Color.rgb(46, 125, 104));
        set.setCircleColor(Color.rgb(216, 154, 58));
        return new LineData(set);
    }

    private BarData barData(List<StatItem> ranking) {
        List<BarEntry> entries = new ArrayList<>();
        int size = Math.min(5, ranking.size());
        for (int i = 0; i < size; i++) entries.add(new BarEntry(i, ranking.get(i).value / 60f));
        BarDataSet set = new BarDataSet(entries, "分钟");
        set.setColor(Color.rgb(46, 125, 104));
        return new BarData(set);
    }
}
