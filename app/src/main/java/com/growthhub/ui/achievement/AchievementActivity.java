package com.growthhub.ui.achievement;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growthhub.database.dao.AchievementDao;
import com.growthhub.database.entity.Achievement;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.util.TimeUtils;
import com.growthhub.util.UiUtils;

public class AchievementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(root);
        root.addView(UiUtils.title(this, "成就中心"));
        AchievementDao dao = new AchievementDao(GrowthHubDbHelper.getInstance(this));
        for (Achievement achievement : dao.getAll()) {
            String state = achievement.unlockTime > 0 ? "已解锁 " + TimeUtils.formatDateTime(achievement.unlockTime) : "未解锁";
            root.addView(UiUtils.text(this, achievement.title + "\n" + achievement.description + "\n" + state, 15));
        }
        setContentView(scrollView);
    }
}
