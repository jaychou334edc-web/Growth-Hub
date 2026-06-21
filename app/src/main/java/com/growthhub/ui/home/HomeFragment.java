package com.growthhub.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

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
import com.growthhub.util.UiUtils;

import java.util.List;

public class HomeFragment extends Fragment {
    private LinearLayout content;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(requireContext());
        content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(content);
        return scrollView;
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        content.removeAllViews();
        StatisticsRepository stats = new StatisticsRepository(requireContext());
        FocusRepository focus = new FocusRepository(requireContext());
        QuoteRepository quoteRepository = new QuoteRepository(requireContext());
        Achievement latest = new AchievementDao(GrowthHubDbHelper.getInstance(requireContext())).getLatestUnlocked();
        List<FocusRecord> recent = focus.getRecent(1);

        content.addView(UiUtils.title(requireContext(), "Growth Hub"));
        content.addView(UiUtils.text(requireContext(), "今日专注：" + TimeUtils.formatDuration(stats.engine().todayDuration()), 18));
        content.addView(UiUtils.text(requireContext(), "本周专注：" + TimeUtils.formatDuration(stats.engine().weekDuration()), 18));
        content.addView(UiUtils.text(requireContext(), "近30天活跃率：" + stats.engine().activeRate30DaysPercent() + "%", 18));
        content.addView(UiUtils.text(requireContext(), "励志语录：" + quoteRepository.randomQuote(), 16));
        content.addView(UiUtils.text(requireContext(), "最近成就：" + (latest == null ? "暂无" : latest.title), 16));
        String recentText = recent.isEmpty() ? "暂无" : TimeUtils.formatDateTime(recent.get(0).startTime) + " " + TimeUtils.formatDuration(recent.get(0).duration);
        content.addView(UiUtils.text(requireContext(), "最近专注：" + recentText, 16));

        Button start = UiUtils.button(requireContext(), "开始专注");
        start.setOnClickListener(v -> {
            com.google.android.material.bottomnavigation.BottomNavigationView nav = requireActivity().findViewById(R.id.main_bottom_nav);
            nav.setSelectedItemId(R.id.nav_focus);
        });
        content.addView(start);
    }
}
