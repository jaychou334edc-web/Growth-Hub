package com.growthhub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growthhub.settings.SettingsRepository;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.notification.NotificationHelper;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GrowthHubDbHelper.getInstance(this).getWritableDatabase();
        new NotificationHelper(this).ensureChannels();
        if (SettingsRepository.getInstance(this).splashEnabled()) {
            setContentView(R.layout.activity_splash);
            new Handler(Looper.getMainLooper()).postDelayed(this::openMain, 2000);
            return;
        }
        openMain();
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
