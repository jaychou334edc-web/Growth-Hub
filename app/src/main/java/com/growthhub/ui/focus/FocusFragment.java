package com.growthhub.ui.focus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.growthhub.constant.AppActions;
import com.growthhub.constant.FocusMode;
import com.growthhub.constant.Mood;
import com.growthhub.R;
import com.growthhub.database.entity.TaskItem;
import com.growthhub.database.repository.TaskRepository;
import com.growthhub.service.FocusService;
import com.growthhub.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class FocusFragment extends Fragment {
    private final List<TaskItem> tasks = new ArrayList<>();
    private Spinner taskSpinner;
    private MaterialButtonToggleGroup modeGroup;
    private EditText minutesInput;
    private TextView timerText;
    private TextView statusText;
    private TextView timerCaption;
    private TextView taskName;
    private TextView taskMeta;
    private View timerCard;
    private View taskCard;
    private View emptyCard;
    private View controlsGroup;

    private final BroadcastReceiver focusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AppActions.ACTION_FOCUS_TICK.equals(intent.getAction())) {
                long remaining = intent.getLongExtra(AppActions.EXTRA_REMAINING, 0);
                timerText.setText(formatHeroDuration(remaining));
                statusText.setText(R.string.focus_running);
                timerCaption.setText(R.string.focus_stay_in_flow);
            } else if (AppActions.ACTION_FOCUS_FINISH.equals(intent.getAction())) {
                timerText.setText(R.string.focus_completed);
                statusText.setText(R.string.focus_record_created);
                timerCaption.setText(R.string.focus_session_complete);
                animateFinish();
                Toast.makeText(context, R.string.focus_record_created, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_focus, container, false);
        taskSpinner = root.findViewById(R.id.focus_task_spinner);
        modeGroup = root.findViewById(R.id.focus_mode_group);
        minutesInput = root.findViewById(R.id.focus_minutes_input);
        timerText = root.findViewById(R.id.focus_timer_text);
        statusText = root.findViewById(R.id.focus_status_text);
        timerCaption = root.findViewById(R.id.focus_timer_caption);
        taskName = root.findViewById(R.id.focus_task_name);
        taskMeta = root.findViewById(R.id.focus_task_meta);
        timerCard = root.findViewById(R.id.focus_timer_card);
        taskCard = root.findViewById(R.id.focus_task_card);
        emptyCard = root.findViewById(R.id.focus_empty_card);
        controlsGroup = root.findViewById(R.id.focus_controls_group);

        Button start = root.findViewById(R.id.focus_start);
        Button pause = root.findViewById(R.id.focus_pause);
        Button resume = root.findViewById(R.id.focus_resume);
        Button stop = root.findViewById(R.id.focus_stop);
        Button cancel = root.findViewById(R.id.focus_cancel);
        modeGroup.check(R.id.focus_mode_countdown);
        applyTimerSurfaceSize(root);

        start.setOnClickListener(v -> {
            animateStart();
            startFocus();
        });
        pause.setOnClickListener(v -> {
            statusText.setText(R.string.focus_paused);
            timerCaption.setText(R.string.focus_pause_caption);
            animatePause();
            sendAction(AppActions.ACTION_FOCUS_PAUSE);
        });
        resume.setOnClickListener(v -> {
            statusText.setText(R.string.focus_resume);
            timerCaption.setText(R.string.focus_resume_caption);
            animateStart();
            sendAction(AppActions.ACTION_FOCUS_RESUME);
        });
        stop.setOnClickListener(v -> showFinishDialog());
        cancel.setOnClickListener(v -> {
            statusText.setText(R.string.focus_canceled);
            timerCaption.setText(R.string.focus_ready_when_you_are);
            sendAction(AppActions.ACTION_FOCUS_CANCEL);
        });
        animateEntrance(root);
        return root;
    }

    private void applyTimerSurfaceSize(View root) {
        root.post(() -> {
            int screenWidth = root.getWidth();
            int target = Math.min(dp(304), Math.max(dp(260), screenWidth - dp(96)));
            ViewGroup.LayoutParams params = timerCard.getLayoutParams();
            params.width = target;
            params.height = target;
            timerCard.setLayoutParams(params);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppActions.ACTION_FOCUS_TICK);
        filter.addAction(AppActions.ACTION_FOCUS_FINISH);
        ContextCompat.registerReceiver(requireContext(), focusReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            requireContext().unregisterReceiver(focusReceiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void loadTasks() {
        tasks.clear();
        tasks.addAll(new TaskRepository(requireContext()).getSelectableTasks());
        List<String> labels = new ArrayList<>();
        for (TaskItem task : tasks) labels.add(task.title);
        taskSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels));
        boolean empty = tasks.isEmpty();
        taskCard.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyCard.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            emptyCard.setAlpha(1f);
            taskName.setText(R.string.common_not_selected_task);
            taskMeta.setText(R.string.common_create_task_first);
            statusText.setText(R.string.focus_waiting_task);
            return;
        }
        taskCard.setAlpha(1f);
        updateTaskSummary(tasks.get(taskSpinner.getSelectedItemPosition()));
        taskSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateTaskSummary(tasks.get(position));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void startFocus() {
        if (tasks.isEmpty()) {
            Toast.makeText(requireContext(), R.string.focus_create_task_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        TaskItem task = tasks.get(taskSpinner.getSelectedItemPosition());
        int mode = modeGroup.getCheckedButtonId() == R.id.focus_mode_countup ? FocusMode.COUNTUP : FocusMode.COUNTDOWN;
        long minutes = 25;
        try {
            minutes = Long.parseLong(minutesInput.getText().toString());
        } catch (NumberFormatException ignored) {
        }
        Intent intent = new Intent(requireContext(), FocusService.class);
        intent.setAction(AppActions.ACTION_FOCUS_START);
        intent.putExtra(AppActions.EXTRA_TASK_ID, task.id);
        intent.putExtra(AppActions.EXTRA_TASK_TITLE, task.title);
        intent.putExtra(AppActions.EXTRA_MODE, mode);
        intent.putExtra(AppActions.EXTRA_DURATION, Math.max(1, minutes) * 60);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent);
        } else {
            requireContext().startService(intent);
        }
        statusText.setText(R.string.focus_running);
        timerCaption.setText(mode == FocusMode.COUNTUP ? R.string.focus_counting_up : R.string.focus_counting_down);
    }

    private void sendAction(String action) {
        Intent intent = new Intent(requireContext(), FocusService.class);
        intent.setAction(action);
        requireContext().startService(intent);
    }

    private void showFinishDialog() {
        LinearLayout box = new LinearLayout(requireContext());
        box.setOrientation(LinearLayout.VERTICAL);
        Spinner moodSpinner = new Spinner(requireContext());
        moodSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        getString(R.string.focus_mood_great),
                        getString(R.string.focus_mood_good),
                        getString(R.string.focus_mood_normal),
                        getString(R.string.focus_mood_bad)
                }));
        moodSpinner.setSelection(2);
        TextInputLayout noteLayout = new TextInputLayout(requireContext());
        noteLayout.setHint(getString(R.string.focus_note_hint));
        TextInputEditText note = new TextInputEditText(requireContext());
        noteLayout.addView(note);
        box.addView(moodSpinner);
        box.addView(noteLayout);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.focus_finish_session)
                .setView(box)
                .setNegativeButton(R.string.common_skip, (dialog, which) -> stopWithMood(Mood.NORMAL, ""))
                .setPositiveButton(R.string.common_save, (dialog, which) -> {
                    int selected = moodSpinner.getSelectedItemPosition();
                    int mood = selected == 0 ? Mood.GREAT : selected == 1 ? Mood.GOOD : selected == 3 ? Mood.BAD : Mood.NORMAL;
                    stopWithMood(mood, note.getText().toString().trim());
                })
                .show();
    }

    private void stopWithMood(int mood, String note) {
        Intent intent = new Intent(requireContext(), FocusService.class);
        intent.setAction(AppActions.ACTION_FOCUS_STOP);
        intent.putExtra(AppActions.EXTRA_MOOD, mood);
        intent.putExtra(AppActions.EXTRA_NOTE, note);
        requireContext().startService(intent);
    }

    private void updateTaskSummary(TaskItem task) {
        taskName.setText(task.title);
        String description = task.description == null || task.description.trim().isEmpty()
                ? getString(R.string.common_task_id_category_id, task.id, task.categoryId)
                : task.description + " · " + getString(R.string.common_category_id, task.categoryId);
        taskMeta.setText(description);
    }

    private String formatHeroDuration(long seconds) {
        long safe = Math.max(0, seconds);
        long hours = safe / 3600;
        long minutes = (safe % 3600) / 60;
        long remainingSeconds = safe % 60;
        if (hours > 0) {
            return String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds);
        }
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds);
    }

    private void animateEntrance(View root) {
        animateIn(timerCard, 0);
        animateIn(taskCard, 90);
        animateIn(root.findViewById(R.id.focus_mode_group), 160);
        animateIn(controlsGroup, 230);
    }

    private void animateIn(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(28f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(360)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateStart() {
        timerCard.animate()
                .scaleX(1.035f)
                .scaleY(1.035f)
                .setDuration(160)
                .withEndAction(() -> timerCard.animate().scaleX(1f).scaleY(1f).setDuration(240).start())
                .start();
    }

    private void animatePause() {
        timerText.animate().alpha(0.55f).setDuration(160)
                .withEndAction(() -> timerText.animate().alpha(1f).setDuration(260).start())
                .start();
    }

    private void animateFinish() {
        timerText.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(180)
                .withEndAction(() -> timerText.animate().scaleX(1f).scaleY(1f).setDuration(260).start())
                .start();
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}
