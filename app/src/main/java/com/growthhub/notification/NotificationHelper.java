package com.growthhub.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.growthhub.MainActivity;
import com.growthhub.R;
import com.growthhub.constant.AppActions;
import com.growthhub.service.FocusService;
import com.growthhub.util.TimeUtils;

public class NotificationHelper {
    public static final String FOCUS_CHANNEL = "focus_channel";
    public static final String REMINDER_CHANNEL = "reminder_channel";
    public static final String ACHIEVEMENT_CHANNEL = "achievement_channel";
    public static final int FOCUS_NOTIFICATION_ID = 1001;

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public void ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(FOCUS_CHANNEL, "Focus Channel", NotificationManager.IMPORTANCE_LOW));
        manager.createNotificationChannel(new NotificationChannel(REMINDER_CHANNEL, "Reminder Channel", NotificationManager.IMPORTANCE_DEFAULT));
        manager.createNotificationChannel(new NotificationChannel(ACHIEVEMENT_CHANNEL, "Achievement Channel", NotificationManager.IMPORTANCE_DEFAULT));
    }

    public Notification buildFocusNotification(String taskTitle, long remainingSeconds, boolean countUp) {
        ensureChannels();
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent openIntent = PendingIntent.getActivity(context, 1, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stop = new Intent(context, FocusService.class);
        stop.setAction(AppActions.ACTION_FOCUS_STOP);
        PendingIntent stopIntent = PendingIntent.getService(context, 2, stop, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String timeText = countUp ? "已专注 " + TimeUtils.formatDuration(remainingSeconds) : "剩余 " + TimeUtils.formatDuration(remainingSeconds);
        return new NotificationCompat.Builder(context, FOCUS_CHANNEL)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle("正在专注：" + taskTitle)
                .setContentText(timeText)
                .setContentIntent(openIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_timer, "停止", stopIntent)
                .build();
    }

    public void notifyAchievement(String title, String description) {
        ensureChannels();
        Notification notification = new NotificationCompat.Builder(context, ACHIEVEMENT_CHANNEL)
                .setSmallIcon(R.drawable.ic_chart)
                .setContentTitle("成就解锁：" + title)
                .setContentText(description)
                .setAutoCancel(true)
                .build();
        context.getSystemService(NotificationManager.class).notify((int) System.currentTimeMillis(), notification);
    }

    public void notifyReminder(String title, String text) {
        ensureChannels();
        Notification notification = new NotificationCompat.Builder(context, REMINDER_CHANNEL)
                .setSmallIcon(R.drawable.ic_home)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .build();
        context.getSystemService(NotificationManager.class).notify((int) System.currentTimeMillis(), notification);
    }
}
