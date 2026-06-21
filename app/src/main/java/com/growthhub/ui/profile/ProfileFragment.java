package com.growthhub.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.growthhub.ui.achievement.AchievementActivity;
import com.growthhub.ui.category.CategoryActivity;
import com.growthhub.ui.countdown.CountdownActivity;
import com.growthhub.ui.quote.QuoteActivity;
import com.growthhub.ui.task.TagActivity;
import com.growthhub.ui.task.TaskActivity;
import com.growthhub.util.UiUtils;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(root);
        root.addView(UiUtils.title(requireContext(), "我的"));
        addButton(root, "分类管理", CategoryActivity.class);
        addButton(root, "任务管理", TaskActivity.class);
        addButton(root, "标签管理", TagActivity.class);
        addButton(root, "倒数日管理", CountdownActivity.class);
        addButton(root, "语录管理", QuoteActivity.class);
        addButton(root, "成就中心", AchievementActivity.class);
        root.addView(UiUtils.text(requireContext(), "ContentProvider: content://growthhub/focus\n权限: com.growthhub.permission.READ_STATS", 14));
        return scrollView;
    }

    private void addButton(LinearLayout root, String label, Class<?> activityClass) {
        Button button = UiUtils.button(requireContext(), label);
        button.setOnClickListener(v -> startActivity(new Intent(requireContext(), activityClass)));
        root.addView(button);
    }
}
