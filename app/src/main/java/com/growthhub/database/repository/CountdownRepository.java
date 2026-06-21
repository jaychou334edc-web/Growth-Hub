package com.growthhub.database.repository;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.growthhub.constant.AppActions;
import com.growthhub.database.dao.CountdownDao;
import com.growthhub.database.entity.CountdownEvent;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.receiver.ReminderReceiver;

import java.util.List;

public class CountdownRepository {
    private final Context context;
    private final CountdownDao countdownDao;

    public CountdownRepository(Context context) {
        this.context = context.getApplicationContext();
        countdownDao = new CountdownDao(GrowthHubDbHelper.getInstance(context));
    }

    public long create(String title, long targetDate, String description, int remindDaysBefore) {
        long id = countdownDao.insert(title, targetDate, description, remindDaysBefore);
        if (id > 0) schedule(id, title, targetDate, remindDaysBefore);
        return id;
    }

    public List<CountdownEvent> getAll() {
        return countdownDao.getAll();
    }

    public void delete(long id) {
        countdownDao.delete(id);
    }

    private void schedule(long id, String title, long targetDate, int remindDaysBefore) {
        long triggerAt = targetDate - remindDaysBefore * 24L * 60L * 60L * 1000L;
        if (triggerAt < System.currentTimeMillis()) return;
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(AppActions.ACTION_COUNTDOWN_REMINDER);
        intent.putExtra(AppActions.EXTRA_EVENT_TITLE, title);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        try {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } catch (SecurityException e) {
            manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }
}
