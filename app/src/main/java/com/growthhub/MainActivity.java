package com.growthhub;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.growthhub.ui.focus.FocusFragment;
import com.growthhub.ui.history.HistoryFragment;
import com.growthhub.ui.home.HomeFragment;
import com.growthhub.ui.profile.ProfileFragment;
import com.growthhub.ui.statistics.StatisticsFragment;

public class MainActivity extends AppCompatActivity {
    private int containerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNotificationPermissionIfNeeded();

        containerId = View.generateViewId();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF7F8F5);

        FrameLayout container = new FrameLayout(this);
        container.setId(containerId);
        root.addView(container, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        BottomNavigationView nav = new BottomNavigationView(this);
        nav.setId(R.id.main_bottom_nav);
        nav.inflateMenu(R.menu.main_bottom_nav);
        root.addView(nav, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        setContentView(root);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_focus) return show(new FocusFragment());
            if (id == R.id.nav_history) return show(new HistoryFragment());
            if (id == R.id.nav_statistics) return show(new StatisticsFragment());
            if (id == R.id.nav_profile) return show(new ProfileFragment());
            return show(new HomeFragment());
        });
        if (savedInstanceState == null) nav.setSelectedItemId(R.id.nav_home);
    }

    private boolean show(@NonNull Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(containerId, fragment).commit();
        return true;
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
        }
    }
}
