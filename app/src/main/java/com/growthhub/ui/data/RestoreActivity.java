package com.growthhub.ui.data;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growthhub.R;
import com.growthhub.data.BackupManager;
import com.growthhub.ui.management.ManagementAnimations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestoreActivity extends AppCompatActivity {
    private BackupManager backupManager;
    private TextView status;
    private ExecutorService executor;
    private final ActivityResultLauncher<String[]> backupPicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    confirmRestore(uri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backupManager = new BackupManager(this);
        executor = Executors.newSingleThreadExecutor();
        setContentView(R.layout.activity_restore);
        status = findViewById(R.id.restore_status);
        findViewById(R.id.restore_choose_button).setOnClickListener(v -> chooseBackupFile());
        animatePage();
    }

    private void chooseBackupFile() {
        backupPicker.launch(new String[]{"application/octet-stream", "application/vnd.sqlite3", "*/*"});
    }

    private void confirmRestore(Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.restore_confirm_title)
                .setMessage(R.string.restore_confirm_message)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.restore_confirm_action, (dialog, which) -> restore(uri))
                .show();
    }

    private void restore(Uri uri) {
        status.setText(R.string.restore_running);
        executor.execute(() -> {
            try {
                backupManager.restoreDatabase(uri);
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    status.setText(R.string.restore_success_desc);
                    Toast.makeText(this, R.string.restore_success, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                String message = errorMessage(e, getString(R.string.restore_failed));
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    status.setText(getString(R.string.restore_failed_reason, message));
                    Toast.makeText(this, R.string.restore_failed, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void animatePage() {
        ManagementAnimations.enterStaggered(40, 80,
                findViewById(R.id.restore_hero_card),
                findViewById(R.id.restore_warning_card),
                findViewById(R.id.restore_action_card));
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
