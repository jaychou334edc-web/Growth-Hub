package com.growthhub;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.notification.NotificationHelper;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GrowthHubDbHelper.getInstance(this).getWritableDatabase();
        new NotificationHelper(this).ensureChannels();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
