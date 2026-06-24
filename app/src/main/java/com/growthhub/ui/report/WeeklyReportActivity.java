package com.growthhub.ui.report;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.growthhub.R;
import com.growthhub.database.entity.Achievement;
import com.growthhub.report.WeeklyReportGenerator;
import com.growthhub.report.WeeklyReportGenerator.WeeklyReport;
import com.growthhub.ui.management.ManagementAnimations;
import com.growthhub.util.DurationFormatter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeeklyReportActivity extends AppCompatActivity {
    private WeeklyReportGenerator generator;
    private WeeklyReport report;
    private ExecutorService executor;
    private TextView heroDuration;
    private TextView heroChange;
    private TextView summary;
    private TextView insights;
    private TextView achievements;
    private TextView growthLevel;
    private TextView growthScore;
    private TextView narrative;
    private TextView exportStatus;
    private final ActivityResultLauncher<String> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    exportReport();
                } else {
                    exportStatus.setText(R.string.storage_permission_denied);
                    Toast.makeText(this, R.string.storage_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        generator = new WeeklyReportGenerator(this);
        executor = Executors.newSingleThreadExecutor();
        setContentView(R.layout.activity_weekly_report);
        bindViews();
        render();
        findViewById(R.id.weekly_report_export_button).setOnClickListener(v -> exportReportWithPermission());
        animatePage();
    }

    private void bindViews() {
        heroDuration = findViewById(R.id.weekly_report_duration);
        heroChange = findViewById(R.id.weekly_report_change);
        summary = findViewById(R.id.weekly_report_summary);
        insights = findViewById(R.id.weekly_report_insights);
        achievements = findViewById(R.id.weekly_report_achievements);
        growthLevel = findViewById(R.id.weekly_report_growth_level);
        growthScore = findViewById(R.id.weekly_report_growth_score);
        narrative = findViewById(R.id.weekly_report_narrative);
        exportStatus = findViewById(R.id.weekly_report_export_status);
    }

    private void render() {
        report = generator.generate();
        heroDuration.setText(DurationFormatter.format(report.weekSeconds));
        heroChange.setText(getString(report.changePercent >= 0
                        ? R.string.weekly_report_change_up
                        : R.string.weekly_report_change_down,
                Math.abs(report.changePercent)));
        summary.setText(getString(R.string.weekly_report_summary_body,
                report.focusCount,
                DurationFormatter.format(report.weekSeconds),
                DurationFormatter.format(report.averageSeconds),
                report.activeDays,
                report.activeRate));
        insights.setText(getString(R.string.weekly_report_insights_body,
                report.topCategory + " · " + DurationFormatter.format(report.topCategorySeconds),
                report.topTask + " · " + DurationFormatter.format(report.topTaskSeconds),
                report.bestDay + " · " + DurationFormatter.format(report.bestDaySeconds),
                DurationFormatter.format(report.longestSeconds)));
        achievements.setText(achievementText(report));
        growthLevel.setText(report.growthLevel.title);
        growthScore.setText(getString(R.string.weekly_report_growth_score_value, report.growthScore));
        narrative.setText(report.narrative);
    }

    private String achievementText(WeeklyReport report) {
        if (report.weekAchievements.isEmpty()) {
            return getString(R.string.weekly_report_no_achievement);
        }
        StringBuilder builder = new StringBuilder();
        for (Achievement achievement : report.weekAchievements) {
            if (builder.length() > 0) builder.append('\n');
            builder.append("• ").append(achievement.title).append(" · ").append(achievement.description);
        }
        return builder.toString();
    }

    private void exportReportWithPermission() {
        if (needsLegacyStoragePermission()) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }
        exportReport();
    }

    private void exportReport() {
        exportStatus.setText(R.string.weekly_report_export_running);
        executor.execute(() -> {
            try {
                WeeklyReport currentReport = report == null ? generator.generate() : report;
                String path = generator.exportTxt(currentReport);
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    exportStatus.setText(getString(R.string.weekly_report_export_success_path, path));
                    Toast.makeText(this, R.string.weekly_report_export_success, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                String message = errorMessage(e, getString(R.string.weekly_report_export_failed));
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    exportStatus.setText(getString(R.string.weekly_report_export_failed_reason, message));
                    Toast.makeText(this, R.string.weekly_report_export_failed, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private boolean needsLegacyStoragePermission() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    private String errorMessage(Exception e, String fallback) {
        return e.getMessage() == null || e.getMessage().length() == 0 ? fallback : e.getMessage();
    }

    private void animatePage() {
        ManagementAnimations.enterStaggered(40, 80,
                findViewById(R.id.weekly_report_hero_card),
                findViewById(R.id.weekly_report_summary_card),
                findViewById(R.id.weekly_report_insights_card),
                findViewById(R.id.weekly_report_achievements_card),
                findViewById(R.id.weekly_report_growth_card),
                findViewById(R.id.weekly_report_narrative_card),
                findViewById(R.id.weekly_report_export_card));
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
