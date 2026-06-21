package com.growthhub.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.growthhub.ui.achievement.AchievementActivity;
import com.growthhub.ui.category.CategoryActivity;
import com.growthhub.ui.countdown.CountdownActivity;
import com.growthhub.ui.quote.QuoteActivity;
import com.growthhub.ui.task.TagActivity;
import com.growthhub.ui.task.TaskActivity;
import com.growthhub.R;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        bind(root, R.id.profile_category, CategoryActivity.class);
        bind(root, R.id.profile_task, TaskActivity.class);
        bind(root, R.id.profile_tag, TagActivity.class);
        bind(root, R.id.profile_countdown, CountdownActivity.class);
        bind(root, R.id.profile_quote, QuoteActivity.class);
        bind(root, R.id.profile_achievement, AchievementActivity.class);
        return root;
    }

    private void bind(View root, int id, Class<?> activityClass) {
        Button button = root.findViewById(id);
        button.setOnClickListener(v -> startActivity(new Intent(requireContext(), activityClass)));
    }
}
