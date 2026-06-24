package com.growthhub.ui.achievement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.growthhub.R;
import com.growthhub.database.entity.Achievement;
import com.growthhub.util.TimeUtils;

import java.util.List;
public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {
    private final List<DisplayItem> items;

    public AchievementAdapter(List<DisplayItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(dp(holder.itemView.getContext(), 18));
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(position * 55L)
                .setDuration(320)
                .start();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DisplayItem {
        final Achievement achievement;
        final String tier;
        final int tierColor;
        final int progressPercent;
        final String progressText;
        final boolean unlocked;
        final boolean nearlyUnlocked;

        DisplayItem(Achievement achievement, String tier, int tierColor, int progressPercent,
                    String progressText, boolean unlocked, boolean nearlyUnlocked) {
            this.achievement = achievement;
            this.tier = tier;
            this.tierColor = tierColor;
            this.progressPercent = progressPercent;
            this.progressText = progressText;
            this.unlocked = unlocked;
            this.nearlyUnlocked = nearlyUnlocked;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final View body;
        private final ImageView icon;
        private final TextView tier;
        private final TextView title;
        private final TextView description;
        private final TextView state;
        private final LinearProgressIndicator progress;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            body = itemView.findViewById(R.id.achievement_card_body);
            icon = itemView.findViewById(R.id.achievement_card_icon);
            tier = itemView.findViewById(R.id.achievement_card_tier);
            title = itemView.findViewById(R.id.achievement_card_title);
            description = itemView.findViewById(R.id.achievement_card_description);
            state = itemView.findViewById(R.id.achievement_card_state);
            progress = itemView.findViewById(R.id.achievement_card_progress);
        }

        void bind(DisplayItem item) {
            Context context = itemView.getContext();
            title.setText(item.achievement.title);
            description.setText(item.achievement.description);
            tier.setText(tierLabel(context, item.tier));
            tier.setTextColor(ContextCompat.getColor(context, item.tierColor));
            progress.setProgressCompat(item.progressPercent, true);

            if (item.unlocked) {
                body.setBackgroundResource(R.drawable.bg_achievement_gold_card);
                icon.setImageResource(R.drawable.ic_achievement_award);
                icon.setColorFilter(ContextCompat.getColor(context, R.color.achievement_gold));
                progress.setIndicatorColor(ContextCompat.getColor(context, R.color.achievement_gold));
                state.setText(context.getString(R.string.achievement_state_unlocked, TimeUtils.formatDateTime(item.achievement.unlockTime)));
                state.setTextColor(ContextCompat.getColor(context, R.color.achievement_gold));
            } else if (item.nearlyUnlocked) {
                body.setBackgroundResource(R.drawable.bg_achievement_nearly_card);
                icon.setImageResource(R.drawable.ic_achievement_spark);
                icon.setColorFilter(ContextCompat.getColor(context, R.color.achievement_success));
                progress.setIndicatorColor(ContextCompat.getColor(context, R.color.achievement_success));
                state.setText(context.getString(R.string.achievement_state_nearly, item.progressText));
                state.setTextColor(ContextCompat.getColor(context, R.color.achievement_success));
            } else {
                body.setBackgroundResource(R.drawable.bg_achievement_locked_card);
                icon.setImageResource(R.drawable.ic_achievement_lock);
                icon.setColorFilter(ContextCompat.getColor(context, R.color.achievement_locked));
                progress.setIndicatorColor(ContextCompat.getColor(context, R.color.achievement_locked));
                state.setText(context.getString(R.string.achievement_state_locked, item.progressText));
                state.setTextColor(ContextCompat.getColor(context, R.color.achievement_text_muted));
            }
        }
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    private static String tierLabel(Context context, String tier) {
        if ("Legend".equals(tier)) return context.getString(R.string.achievement_tier_legend);
        if ("Gold".equals(tier)) return context.getString(R.string.achievement_tier_gold);
        if ("Silver".equals(tier)) return context.getString(R.string.achievement_tier_silver);
        return context.getString(R.string.achievement_tier_bronze);
    }
}
