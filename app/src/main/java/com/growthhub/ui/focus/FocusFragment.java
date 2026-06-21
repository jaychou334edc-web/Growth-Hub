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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.growthhub.constant.AppActions;
import com.growthhub.constant.FocusMode;
import com.growthhub.constant.Mood;
import com.growthhub.database.entity.TaskItem;
import com.growthhub.database.repository.TaskRepository;
import com.growthhub.service.FocusService;
import com.growthhub.util.TimeUtils;
import com.growthhub.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class FocusFragment extends Fragment {
    private final List<TaskItem> tasks = new ArrayList<>();
    private Spinner taskSpinner;
    private RadioGroup modeGroup;
    private EditText minutesInput;
    private TextView timerText;

    private final BroadcastReceiver focusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AppActions.ACTION_FOCUS_TICK.equals(intent.getAction())) {
                long remaining = intent.getLongExtra(AppActions.EXTRA_REMAINING, 0);
                timerText.setText(TimeUtils.formatDuration(remaining));
            } else if (AppActions.ACTION_FOCUS_FINISH.equals(intent.getAction())) {
                timerText.setText("专注完成");
                Toast.makeText(context, "专注记录已生成", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(root);

        root.addView(UiUtils.title(requireContext(), "专注中心"));
        taskSpinner = new Spinner(requireContext());
        root.addView(taskSpinner);

        modeGroup = new RadioGroup(requireContext());
        RadioButton countdown = new RadioButton(requireContext());
        countdown.setText("倒计时");
        countdown.setId(View.generateViewId());
        RadioButton countup = new RadioButton(requireContext());
        countup.setText("正计时");
        countup.setId(View.generateViewId());
        modeGroup.addView(countdown);
        modeGroup.addView(countup);
        modeGroup.check(countdown.getId());
        root.addView(modeGroup);

        minutesInput = new EditText(requireContext());
        minutesInput.setHint("倒计时分钟，例如 25");
        minutesInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        minutesInput.setText("25");
        root.addView(minutesInput);

        timerText = UiUtils.title(requireContext(), "00秒");
        root.addView(timerText);

        Button start = UiUtils.button(requireContext(), "开始");
        Button pause = UiUtils.button(requireContext(), "暂停");
        Button resume = UiUtils.button(requireContext(), "恢复");
        Button stop = UiUtils.button(requireContext(), "结束并保存");
        Button cancel = UiUtils.button(requireContext(), "放弃");
        root.addView(start);
        root.addView(pause);
        root.addView(resume);
        root.addView(stop);
        root.addView(cancel);

        start.setOnClickListener(v -> startFocus());
        pause.setOnClickListener(v -> sendAction(AppActions.ACTION_FOCUS_PAUSE));
        resume.setOnClickListener(v -> sendAction(AppActions.ACTION_FOCUS_RESUME));
        stop.setOnClickListener(v -> showFinishDialog());
        cancel.setOnClickListener(v -> sendAction(AppActions.ACTION_FOCUS_CANCEL));
        return scrollView;
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
    }

    private void startFocus() {
        if (tasks.isEmpty()) {
            Toast.makeText(requireContext(), "请先在“我的-任务管理”中创建任务", Toast.LENGTH_SHORT).show();
            return;
        }
        TaskItem task = tasks.get(taskSpinner.getSelectedItemPosition());
        int checkedIndex = modeGroup.indexOfChild(modeGroup.findViewById(modeGroup.getCheckedRadioButtonId()));
        int mode = checkedIndex == 1 ? FocusMode.COUNTUP : FocusMode.COUNTDOWN;
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
                new String[]{"GREAT", "GOOD", "NORMAL", "BAD"}));
        moodSpinner.setSelection(2);
        EditText note = new EditText(requireContext());
        note.setHint("专注备注，可为空");
        box.addView(moodSpinner);
        box.addView(note);
        new AlertDialog.Builder(requireContext())
                .setTitle("结束专注")
                .setView(box)
                .setNegativeButton("跳过", (dialog, which) -> stopWithMood(Mood.NORMAL, ""))
                .setPositiveButton("保存", (dialog, which) -> {
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
}
