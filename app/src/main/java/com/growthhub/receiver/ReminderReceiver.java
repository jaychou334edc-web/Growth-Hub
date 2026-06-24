package com.growthhub.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.growthhub.constant.AppActions;
import com.growthhub.notification.NotificationHelper;
import com.growthhub.settings.SettingsRepository;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SettingsRepository.getInstance(context).notificationsEnabled()) {
            return;
        }
        String title = intent.getStringExtra(AppActions.EXTRA_EVENT_TITLE);
        new NotificationHelper(context).notifyReminder("倒数日提醒", title == null ? "有一个倒数日即将到来" : title);
    }
}
