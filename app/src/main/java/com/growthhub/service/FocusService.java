package com.growthhub.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.growthhub.constant.AppActions;
import com.growthhub.constant.FocusMode;
import com.growthhub.constant.FocusState;
import com.growthhub.constant.Mood;
import com.growthhub.database.entity.FocusRecord;
import com.growthhub.database.repository.FocusRepository;
import com.growthhub.notification.NotificationHelper;

public class FocusService extends Service {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private NotificationHelper notificationHelper;
    private FocusRepository focusRepository;

    private int state = FocusState.IDLE;
    private long taskId;
    private String taskTitle = "未命名任务";
    private int mode = FocusMode.COUNTDOWN;
    private long plannedSeconds;
    private long elapsedSeconds;
    private long startTime;
    private long lastTickAt;
    private int finishMood = Mood.NORMAL;
    private String finishNote = "";

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (state != FocusState.RUNNING) return;
            long now = System.currentTimeMillis();
            long delta = Math.max(1, (now - lastTickAt) / 1000);
            elapsedSeconds += delta;
            lastTickAt = now;

            long shown = mode == FocusMode.COUNTUP ? elapsedSeconds : Math.max(0, plannedSeconds - elapsedSeconds);
            updateForeground(shown);
            broadcastTick(shown);

            if (mode == FocusMode.COUNTDOWN && shown <= 0) {
                finishFocus();
                return;
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        notificationHelper = new NotificationHelper(this);
        focusRepository = new FocusRepository(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? AppActions.ACTION_FOCUS_START : intent.getAction();
        if (AppActions.ACTION_FOCUS_PAUSE.equals(action)) {
            pauseFocus();
        } else if (AppActions.ACTION_FOCUS_RESUME.equals(action)) {
            resumeFocus();
        } else if (AppActions.ACTION_FOCUS_STOP.equals(action)) {
            if (intent != null) {
                finishMood = intent.getIntExtra(AppActions.EXTRA_MOOD, Mood.NORMAL);
                finishNote = intent.getStringExtra(AppActions.EXTRA_NOTE);
                if (finishNote == null) finishNote = "";
            }
            finishFocus();
        } else if (AppActions.ACTION_FOCUS_CANCEL.equals(action)) {
            cancelFocus();
        } else {
            startFocus(intent);
        }
        return START_STICKY;
    }

    private void startFocus(Intent intent) {
        taskId = intent.getLongExtra(AppActions.EXTRA_TASK_ID, 0);
        taskTitle = intent.getStringExtra(AppActions.EXTRA_TASK_TITLE);
        if (taskTitle == null || taskTitle.length() == 0) taskTitle = "未命名任务";
        mode = intent.getIntExtra(AppActions.EXTRA_MODE, FocusMode.COUNTDOWN);
        plannedSeconds = intent.getLongExtra(AppActions.EXTRA_DURATION, 25 * 60);
        elapsedSeconds = 0;
        startTime = System.currentTimeMillis();
        lastTickAt = startTime;
        finishMood = Mood.NORMAL;
        finishNote = "";
        state = FocusState.RUNNING;

        long shown = mode == FocusMode.COUNTUP ? 0 : plannedSeconds;
        Notification notification = notificationHelper.buildFocusNotification(taskTitle, shown, mode == FocusMode.COUNTUP);
        startForeground(NotificationHelper.FOCUS_NOTIFICATION_ID, notification);
        handler.removeCallbacks(ticker);
        handler.postDelayed(ticker, 1000);
    }

    private void pauseFocus() {
        if (state != FocusState.RUNNING) return;
        state = FocusState.PAUSED;
        handler.removeCallbacks(ticker);
    }

    private void resumeFocus() {
        if (state != FocusState.PAUSED) return;
        state = FocusState.RUNNING;
        lastTickAt = System.currentTimeMillis();
        handler.postDelayed(ticker, 1000);
    }

    private void finishFocus() {
        if (state == FocusState.FINISHED || state == FocusState.CANCELLED || state == FocusState.IDLE) {
            stopSelf();
            return;
        }
        state = FocusState.FINISHED;
        handler.removeCallbacks(ticker);
        long end = System.currentTimeMillis();
        long duration = Math.max(1, elapsedSeconds);

        FocusRecord record = new FocusRecord();
        record.taskId = taskId;
        record.startTime = startTime;
        record.endTime = end;
        record.duration = duration;
        record.mode = mode;
        record.mood = finishMood;
        record.note = finishNote;
        record.createTime = end;
        focusRepository.addFocusRecord(record);

        Intent finish = new Intent(AppActions.ACTION_FOCUS_FINISH);
        finish.setPackage(getPackageName());
        finish.putExtra(AppActions.EXTRA_TASK_ID, taskId);
        finish.putExtra(AppActions.EXTRA_DURATION, duration);
        sendBroadcast(finish);
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void cancelFocus() {
        state = FocusState.CANCELLED;
        handler.removeCallbacks(ticker);
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void updateForeground(long shownSeconds) {
        Notification notification = notificationHelper.buildFocusNotification(taskTitle, shownSeconds, mode == FocusMode.COUNTUP);
        startForeground(NotificationHelper.FOCUS_NOTIFICATION_ID, notification);
    }

    private void broadcastTick(long shownSeconds) {
        Intent tick = new Intent(AppActions.ACTION_FOCUS_TICK);
        tick.setPackage(getPackageName());
        tick.putExtra(AppActions.EXTRA_REMAINING, shownSeconds);
        tick.putExtra(AppActions.EXTRA_MODE, mode);
        sendBroadcast(tick);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
