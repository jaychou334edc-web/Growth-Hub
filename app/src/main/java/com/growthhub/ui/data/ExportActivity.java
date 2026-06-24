package com.growthhub.ui.data;

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
import com.growthhub.data.ExportManager;
import com.growthhub.ui.management.ManagementAnimations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportActivity extends AppCompatActivity {
    private TextView status;
    private ExportManager exportManager;
    private ExecutorService executor;
    private final ActivityResultLauncher<String> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    exportData();
                } else {
                    status.setText(R.string.storage_permission_denied);
                    Toast.makeText(this, R.string.storage_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exportManager = new ExportManager(this);
        executor = Executors.newSingleThreadExecutor();
        setContentView(R.layout.activity_export);
        status = findViewById(R.id.export_status);
        findViewById(R.id.export_button).setOnClickListener(v -> exportData());
        animatePage();
    }

    private void exportData() {
        if (needsLegacyStoragePermission()) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }
        status.setText(R.string.export_running);
        executor.execute(() -> {
            try {
                String path = exportManager.exportCsv();
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    status.setText(getString(R.string.export_success_path, path));
                    Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                String message = errorMessage(e, getString(R.string.export_failed));
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    status.setText(getString(R.string.export_failed_reason, message));
                    Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void animatePage() {
        ManagementAnimations.enterStaggered(40, 80,
                findViewById(R.id.export_hero_card),
                findViewById(R.id.export_scope_card),
                findViewById(R.id.export_action_card));
    }

    private boolean needsLegacyStoragePermission() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    private String errorMessage(Exception e, String fallback) {
        return e.getMessage() == null || e.getMessage().length() == 0 ? fallback : e.getMessage();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
