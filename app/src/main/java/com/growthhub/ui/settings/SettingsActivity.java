package com.growthhub.ui.settings;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.growthhub.R;
import com.growthhub.settings.SettingsRepository;
import com.growthhub.ui.management.ManagementAnimations;

public class SettingsActivity extends AppCompatActivity {
    private SettingsRepository repository;
    private SwitchMaterial notificationSwitch;
    private boolean suppressNotificationChange;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                repository.setNotificationsEnabled(granted);
                suppressNotificationChange = true;
                notificationSwitch.setChecked(granted);
                suppressNotificationChange = false;
                Toast.makeText(this,
                        granted ? R.string.settings_saved : R.string.settings_notification_permission_denied,
                        Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = SettingsRepository.getInstance(this);
        setContentView(R.layout.activity_settings);
        bindSwitches();
        bindAbout();
        animatePage();
    }

    private void bindSwitches() {
        notificationSwitch = findViewById(R.id.settings_notification_switch);
        SwitchMaterial splash = findViewById(R.id.settings_splash_switch);
        SwitchMaterial theme = findViewById(R.id.settings_theme_switch);

        if (repository.notificationsEnabled() && needsNotificationPermission()) {
            repository.setNotificationsEnabled(false);
        }
        notificationSwitch.setChecked(repository.notificationsEnabled());
        splash.setChecked(repository.splashEnabled());
        theme.setChecked(true);
        theme.setEnabled(false);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressNotificationChange) {
                return;
            }
            if (isChecked && needsNotificationPermission()) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
            repository.setNotificationsEnabled(isChecked);
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        });
        splash.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repository.setSplashEnabled(isChecked);
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        });
    }

    private void bindAbout() {
        findViewById(R.id.settings_github_button).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.settings_github_url)));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.settings_github_open_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void animatePage() {
        ManagementAnimations.enterStaggered(40, 80,
                findViewById(R.id.settings_hero_card),
                findViewById(R.id.settings_notification_card),
                findViewById(R.id.settings_splash_card),
                findViewById(R.id.settings_theme_card),
                findViewById(R.id.settings_about_card));
    }

    private boolean needsNotificationPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED;
    }
}
