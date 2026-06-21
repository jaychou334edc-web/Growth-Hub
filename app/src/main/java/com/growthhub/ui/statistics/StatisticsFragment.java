package com.growthhub.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.growthhub.R;
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
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);

        StatisticsRepository repo = new StatisticsRepository(requireContext());
        ((TextView) root.findViewById(R.id.stat_today)).setText("今日\n" + TimeUtils.formatDuration(repo.engine().todayDuration()));
        ((TextView) root.findViewById(R.id.stat_week)).setText("本周\n" + TimeUtils.formatDuration(repo.engine().weekDuration()));
        ((TextView) root.findViewById(R.id.stat_month)).setText("本月\n" + TimeUtils.formatDuration(repo.engine().monthDuration()));
        ((TextView) root.findViewById(R.id.stat_total)).setText("累计\n" + TimeUtils.formatDuration(repo.engine().totalDuration()));
        ((TextView) root.findViewById(R.id.stat_count)).setText("次数\n" + repo.engine().totalCount());
        ((TextView) root.findViewById(R.id.stat_average)).setText("平均\n" + TimeUtils.formatDuration(repo.engine().averageDuration()));
        ((TextView) root.findViewById(R.id.stat_active_rate)).setText("近30天活跃率：" + repo.engine().activeRate30DaysPercent() + "%");
        ((TextView) root.findViewById(R.id.stat_month_rate)).setText("本月专注率：" + repo.engine().monthFocusRatePercent() + "%");

        List<StatItem> daily = repo.recentDailyDurations(7);
        LineChart lineChart = root.findViewById(R.id.stat_line_chart);
        lineChart.setData(lineData(daily));
        lineChart.getDescription().setText("近7天折线图");

        List<StatItem> ranking = repo.engine().taskRanking(0);
        BarChart barChart = root.findViewById(R.id.stat_bar_chart);
        barChart.setData(barData(ranking));
        barChart.getDescription().setText("任务柱状图");

        fillRanking(root.findViewById(R.id.stat_task_ranking), ranking);
        fillRanking(root.findViewById(R.id.stat_category_ranking), repo.engine().categoryRanking(0));
        fillRanking(root.findViewById(R.id.stat_tag_ranking), repo.engine().tagRanking(0));
        return root;
    }

    private void fillRanking(LinearLayout container, List<StatItem> items) {
        container.removeAllViews();
        if (items.isEmpty()) {
            container.addView(UiUtils.text(requireContext(), "暂无数据", 15));
            return;
        }
        for (StatItem item : items) {
            container.addView(UiUtils.text(requireContext(), item.label + "：" + TimeUtils.formatDuration(item.value), 15));
        }
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
