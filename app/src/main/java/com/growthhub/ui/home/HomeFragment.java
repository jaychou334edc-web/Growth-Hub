package com.growthhub.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.growthhub.R;
import com.growthhub.database.dao.AchievementDao;
import com.growthhub.database.entity.Achievement;
import com.growthhub.database.entity.FocusRecord;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.database.repository.FocusRepository;
import com.growthhub.database.repository.QuoteRepository;
import com.growthhub.database.repository.StatisticsRepository;
import com.growthhub.util.TimeUtils;

import java.util.List;

public class HomeFragment extends Fragment {
    private TextView todayDuration;
    private TextView weekDuration;
    private TextView activeRate;
    private TextView quote;
    private TextView latestAchievement;
    private TextView recentFocus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        todayDuration = view.findViewById(R.id.home_today_duration);
        weekDuration = view.findViewById(R.id.home_week_duration);
        activeRate = view.findViewById(R.id.home_active_rate);
        quote = view.findViewById(R.id.home_quote);
        latestAchievement = view.findViewById(R.id.home_latest_achievement);
        recentFocus = view.findViewById(R.id.home_recent_focus);
        Button start = view.findViewById(R.id.home_start_focus);
        start.setOnClickListener(v -> {
            com.google.android.material.bottomnavigation.BottomNavigationView nav = requireActivity().findViewById(R.id.main_bottom_nav);
            nav.setSelectedItemId(R.id.nav_focus);
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        StatisticsRepository stats = new StatisticsRepository(requireContext());
        FocusRepository focus = new FocusRepository(requireContext());
        QuoteRepository quoteRepository = new QuoteRepository(requireContext());
        Achievement latest = new AchievementDao(GrowthHubDbHelper.getInstance(requireContext())).getLatestUnlocked();
        List<FocusRecord> recent = focus.getRecent(1);

        todayDuration.setText(TimeUtils.formatDuration(stats.engine().todayDuration()));
        weekDuration.setText(TimeUtils.formatDuration(stats.engine().weekDuration()));
        activeRate.setText(stats.engine().activeRate30DaysPercent() + "%");
        quote.setText(quoteRepository.randomQuote());
        latestAchievement.setText("最近成就：" + (latest == null ? "暂无" : latest.title));
        String recentText = recent.isEmpty() ? "暂无" : TimeUtils.formatDateTime(recent.get(0).startTime) + " " + TimeUtils.formatDuration(recent.get(0).duration);
        recentFocus.setText("最近专注：" + recentText);
    }
}
