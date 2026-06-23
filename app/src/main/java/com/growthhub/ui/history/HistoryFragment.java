package com.growthhub.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.growthhub.R;
import com.growthhub.constant.Mood;
import com.growthhub.database.entity.Category;
import com.growthhub.database.entity.FocusRecord;
import com.growthhub.database.entity.TaskItem;
import com.growthhub.database.repository.FocusRepository;
import com.growthhub.database.repository.TaskRepository;
import com.growthhub.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryFragment extends Fragment {
    private final HistoryTimelineAdapter adapter = new HistoryTimelineAdapter();
    private final List<HistoryTimelineAdapter.DisplayItem> allItems = new ArrayList<>();
    private View content;
    private View heroCard;
    private View filterCard;
    private View timelineTitle;
    private RecyclerView recyclerView;
    private View emptyCard;
    private TextView totalFocus;
    private TextView totalSessions;
    private TextView longestSession;
    private ChipGroup filterGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        content = root.findViewById(R.id.history_content);
        heroCard = root.findViewById(R.id.history_hero_card);
        filterCard = root.findViewById(R.id.history_filter_card);
        timelineTitle = root.findViewById(R.id.history_timeline_title);
        recyclerView = root.findViewById(R.id.history_recycler);
        emptyCard = root.findViewById(R.id.history_empty_card);
        totalFocus = root.findViewById(R.id.history_total_focus);
        totalSessions = root.findViewById(R.id.history_total_sessions);
        longestSession = root.findViewById(R.id.history_longest_session);
        filterGroup = root.findViewById(R.id.history_filter_group);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) applyFilter(checkedIds.get(0));
        });
        root.findViewById(R.id.history_empty_cta).setOnClickListener(v -> showFocusTab());
        animatePage();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
        applyFilter(filterGroup.getCheckedChipId());
    }

    private void loadHistory() {
        FocusRepository focusRepository = new FocusRepository(requireContext());
        TaskRepository taskRepository = new TaskRepository(requireContext());
        List<FocusRecord> records = focusRepository.getRecent(500);
        Map<Long, TaskItem> tasks = taskMap(taskRepository.getAllTasksForStatisticsAwareViews());
        Map<Long, String> categories = categoryMap(taskRepository.getCategories());

        allItems.clear();
        long total = 0;
        long longest = 0;
        for (FocusRecord record : records) {
            total += record.duration;
            longest = Math.max(longest, record.duration);
            TaskItem task = tasks.get(record.taskId);
            String taskName = task == null ? "Focus Session" : task.title;
            String category = task == null ? "Uncategorized" : categories.getOrDefault(task.categoryId, "Uncategorized");
            allItems.add(new HistoryTimelineAdapter.DisplayItem(
                    record.startTime,
                    taskName,
                    category,
                    formatCompactDuration(record.duration),
                    TimeUtils.formatDateTime(record.startTime),
                    Mood.label(record.mood),
                    notePreview(record.note)
            ));
        }
        totalFocus.setText(formatHeroDuration(total));
        totalSessions.setText(records.size() + " sessions");
        longestSession.setText("Longest · " + formatCompactDuration(longest));
    }

    private void applyFilter(int checkedId) {
        long start = filterStart(checkedId);
        List<HistoryTimelineAdapter.DisplayItem> filtered = new ArrayList<>();
        for (HistoryTimelineAdapter.DisplayItem item : allItems) {
            if (start == 0 || item.startTime >= start) filtered.add(item);
        }
        adapter.submit(filtered);
        boolean empty = filtered.isEmpty();
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyCard.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (!empty) animateIn(recyclerView, 80);
        else animateIn(emptyCard, 80);
    }

    private long filterStart(int checkedId) {
        if (checkedId == R.id.history_filter_today) return TimeUtils.startOfToday();
        if (checkedId == R.id.history_filter_week) return TimeUtils.startOfWeek();
        if (checkedId == R.id.history_filter_month) return TimeUtils.startOfMonth();
        return 0;
    }

    private Map<Long, TaskItem> taskMap(List<TaskItem> tasks) {
        Map<Long, TaskItem> result = new HashMap<>();
        for (TaskItem task : tasks) result.put(task.id, task);
        return result;
    }

    private Map<Long, String> categoryMap(List<Category> categories) {
        Map<Long, String> result = new HashMap<>();
        for (Category category : categories) result.put(category.id, category.name);
        return result;
    }

    private String notePreview(String note) {
        if (note == null) return "";
        String trimmed = note.trim();
        if (trimmed.length() <= 72) return trimmed;
        return trimmed.substring(0, 72) + "...";
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

    private void showFocusTab() {
        BottomNavigationView nav = requireActivity().findViewById(R.id.main_bottom_nav);
        nav.setSelectedItemId(R.id.nav_focus);
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
        animateIn(filterCard, 130);
        animateIn(timelineTitle, 210);
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
