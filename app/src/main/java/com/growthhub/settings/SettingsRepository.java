package com.growthhub.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsRepository {
    private static final String PREFS = "growth_hub_settings";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_SPLASH = "splash_enabled";
    private static volatile SettingsRepository instance;

    private final SharedPreferences preferences;

    public static SettingsRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (SettingsRepository.class) {
                if (instance == null) {
                    instance = new SettingsRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private SettingsRepository(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean notificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public boolean splashEnabled() {
        return preferences.getBoolean(KEY_SPLASH, true);
    }

    public void setSplashEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SPLASH, enabled).apply();
    }
}
