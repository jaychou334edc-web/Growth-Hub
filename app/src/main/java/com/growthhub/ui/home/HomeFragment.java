package com.growthhub.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.growthhub.R;
import com.growthhub.constant.TaskStatus;
import com.growthhub.database.dao.AchievementDao;
import com.growthhub.database.entity.Achievement;
import com.growthhub.database.entity.CountdownEvent;
import com.growthhub.database.entity.FocusRecord;
import com.growthhub.database.entity.TaskItem;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.database.repository.CountdownRepository;
import com.growthhub.database.repository.FocusRepository;
import com.growthhub.database.repository.QuoteRepository;
import com.growthhub.database.repository.StatisticsRepository;
import com.growthhub.database.repository.TaskRepository;
import com.growthhub.model.StatItem;
import com.growthhub.ui.countdown.CountdownActivity;
import com.growthhub.ui.task.TaskActivity;
import com.growthhub.util.TimeUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private TextView greeting;
    private TextView todayDuration;
    private TextView streak;
    private TextView quote;
    private TextView latestAchievement;
    private TextView latestAchievementDesc;
    private TextView countdownTitle;
    private TextView countdownDays;
    private TextView countdownMeta;
    private LinearLayout recentFocusList;
    private View content;
    private View heroCard;
    private View actionsRow;
    private View kpiGrid;
    private View achievementCard;
    private View recentFocusCard;
    private View countdownCard;
    private View quoteCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        content = view.findViewById(R.id.home_content);
        heroCard = view.findViewById(R.id.home_hero_card);
        actionsRow = view.findViewById(R.id.home_actions_row);
        kpiGrid = view.findViewById(R.id.home_kpi_grid);
        achievementCard = view.findViewById(R.id.home_achievement_card);
        recentFocusCard = view.findViewById(R.id.home_recent_focus_card);
        countdownCard = view.findViewById(R.id.home_countdown_card);
        quoteCard = view.findViewById(R.id.home_quote_card);
        greeting = view.findViewById(R.id.home_greeting);
        todayDuration = view.findViewById(R.id.home_today_duration);
        streak = view.findViewById(R.id.home_streak);
        quote = view.findViewById(R.id.home_quote);
        latestAchievement = view.findViewById(R.id.home_latest_achievement);
        latestAchievementDesc = view.findViewById(R.id.home_latest_achievement_desc);
        countdownTitle = view.findViewById(R.id.home_countdown_title);
        countdownDays = view.findViewById(R.id.home_countdown_days);
        countdownMeta = view.findViewById(R.id.home_countdown_meta);
        recentFocusList = view.findViewById(R.id.home_recent_focus_list);

        view.findViewById(R.id.home_action_focus).setOnClickListener(v -> showTab(R.id.nav_focus));
        view.findViewById(R.id.home_action_task).setOnClickListener(v -> startActivity(new Intent(requireContext(), TaskActivity.class)));
        view.findViewById(R.id.home_action_countdown).setOnClickListener(v -> startActivity(new Intent(requireContext(), CountdownActivity.class)));
        view.findViewById(R.id.home_action_statistics).setOnClickListener(v -> showTab(R.id.nav_statistics));
        animatePage();
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
        TaskRepository taskRepository = new TaskRepository(requireContext());
        QuoteRepository quoteRepository = new QuoteRepository(requireContext());
        CountdownRepository countdownRepository = new CountdownRepository(requireContext());
        AchievementDao achievementDao = new AchievementDao(GrowthHubDbHelper.getInstance(requireContext()));

        long todaySeconds = stats.engine().todayDuration();
        List<FocusRecord> recent = focus.getRecent(5);
        List<TaskItem> tasks = taskRepository.getAllTasksForStatisticsAwareViews();
        List<CountdownEvent> countdowns = countdownRepository.getAll();
        List<Achievement> achievements = achievementDao.getAll();
        Achievement latest = achievementDao.getLatestUnlocked();

        greeting.setText(greetingText());
        todayDuration.setText(formatDashboardDuration(todaySeconds));
        streak.setText("Current streak · " + currentStreak(stats.recentDailyDurations(30)) + " days");
        quote.setText(quoteRepository.randomQuote());
        renderKpi(R.id.home_kpi_focus, "FOCUS TODAY", TimeUtils.formatDuration(todaySeconds));
        renderKpi(R.id.home_kpi_tasks, "COMPLETED TASKS", String.valueOf(countCompletedTasks(tasks)));
        renderKpi(R.id.home_kpi_activity, "ACTIVITY RATE", stats.engine().activeRate30DaysPercent() + "%");
        renderKpi(R.id.home_kpi_achievements, "ACHIEVEMENTS", countUnlocked(achievements) + "/" + achievements.size());
        renderAchievement(latest);
        renderRecentFocus(recent, taskRepository);
        renderCountdown(countdowns);
    }

    private void showTab(int itemId) {
        BottomNavigationView nav = requireActivity().findViewById(R.id.main_bottom_nav);
        nav.setSelectedItemId(itemId);
    }

    private void renderKpi(int includeId, String label, String value) {
        View item = requireView().findViewById(includeId);
        ((TextView) item.findViewById(R.id.home_kpi_label)).setText(label);
        ((TextView) item.findViewById(R.id.home_kpi_value)).setText(value);
    }

    private void renderAchievement(Achievement latest) {
        if (latest == null) {
            latestAchievement.setText("No achievement yet");
            latestAchievementDesc.setText("Complete a focus session to unlock your first badge.");
            return;
        }
        latestAchievement.setText(latest.title);
        latestAchievementDesc.setText(latest.description == null ? "Recently unlocked" : latest.description);
    }

    private void renderRecentFocus(List<FocusRecord> records, TaskRepository taskRepository) {
        recentFocusList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        if (records.isEmpty()) {
            addTimelineItem(inflater, "No focus sessions yet", "Start a session to build your timeline.");
            return;
        }
        for (FocusRecord record : records) {
            TaskItem task = taskRepository.getTask(record.taskId);
            String title = task == null ? "Focus Session" : task.title;
            String meta = TimeUtils.formatDuration(record.duration) + " · " + TimeUtils.formatDateTime(record.startTime);
            addTimelineItem(inflater, title, meta);
        }
    }

    private void addTimelineItem(LayoutInflater inflater, String title, String meta) {
        View item = inflater.inflate(R.layout.item_home_timeline, recentFocusList, false);
        ((TextView) item.findViewById(R.id.home_timeline_title)).setText(title);
        ((TextView) item.findViewById(R.id.home_timeline_meta)).setText(meta);
        recentFocusList.addView(item);
    }

    private void renderCountdown(List<CountdownEvent> events) {
        CountdownEvent next = null;
        long now = System.currentTimeMillis();
        for (CountdownEvent event : events) {
            if (event.targetDate >= now) {
                next = event;
                break;
            }
        }
        if (next == null) {
            countdownTitle.setText("Countdown Highlight");
            countdownDays.setText("--");
            countdownMeta.setText("No upcoming event. Add one to keep a target in sight.");
            return;
        }
        long days = Math.max(0, (next.targetDate - TimeUtils.startOfToday()) / (24L * 60L * 60L * 1000L));
        countdownTitle.setText(next.title);
        countdownDays.setText(String.valueOf(days));
        countdownMeta.setText("days left · " + TimeUtils.formatDate(next.targetDate));
    }

    private int countCompletedTasks(List<TaskItem> tasks) {
        int count = 0;
        for (TaskItem task : tasks) {
            if (task.status == TaskStatus.COMPLETED) count++;
        }
        return count;
    }

    private int countUnlocked(List<Achievement> achievements) {
        int count = 0;
        for (Achievement achievement : achievements) {
            if (achievement.unlockTime > 0) count++;
        }
        return count;
    }

    private int currentStreak(List<StatItem> days) {
        int streakDays = 0;
        for (int i = days.size() - 1; i >= 0; i--) {
            if (days.get(i).value > 0) {
                streakDays++;
            } else if (streakDays > 0) {
                break;
            }
        }
        return streakDays;
    }

    private String greetingText() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning";
        if (hour < 18) return "Good Afternoon";
        return "Good Evening";
    }

    private String formatDashboardDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0 && minutes > 0) {
            return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        }
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh", hours);
        }
        if (minutes > 0) {
            return String.format(Locale.getDefault(), "%dm", minutes);
        }
        return seconds + "s";
    }

    private void animatePage() {
        content.setAlpha(0f);
        content.setTranslationY(18f);
        content.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(260)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        animateIn(heroCard, 40);
        animateIn(actionsRow, 110);
        animateIn(kpiGrid, 180);
        animateIn(achievementCard, 250);
        animateIn(recentFocusCard, 320);
        animateIn(countdownCard, 390);
        animateIn(quoteCard, 460);
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
}
