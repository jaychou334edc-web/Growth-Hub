package com.growthhub.ui.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.growthhub.R;

import java.util.ArrayList;
import java.util.List;

public class HistoryTimelineAdapter extends RecyclerView.Adapter<HistoryTimelineAdapter.ViewHolder> {
    private final List<DisplayItem> items = new ArrayList<>();

    public void submit(List<DisplayItem> nextItems) {
        items.clear();
        items.addAll(nextItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_timeline, parent, false);
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
                .setStartDelay(position * 45L)
                .setDuration(300)
                .start();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DisplayItem {
        final long startTime;
        final String taskName;
        final String category;
        final String duration;
        final String dateTime;
        final String mood;
        final String note;

        DisplayItem(long startTime, String taskName, String category, String duration,
                    String dateTime, String mood, String note) {
            this.startTime = startTime;
            this.taskName = taskName;
            this.category = category;
            this.duration = duration;
            this.dateTime = dateTime;
            this.mood = mood;
            this.note = note;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView task;
        private final TextView meta;
        private final TextView duration;
        private final TextView mood;
        private final TextView note;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            task = itemView.findViewById(R.id.history_item_task);
            meta = itemView.findViewById(R.id.history_item_meta);
            duration = itemView.findViewById(R.id.history_item_duration);
            mood = itemView.findViewById(R.id.history_item_mood);
            note = itemView.findViewById(R.id.history_item_note);
        }

        void bind(DisplayItem item) {
            task.setText(item.taskName);
            meta.setText(item.category + " · " + item.dateTime);
            duration.setText(item.duration);
            mood.setText(item.mood);
            if (item.note == null || item.note.length() == 0) {
                note.setVisibility(View.GONE);
            } else {
                note.setVisibility(View.VISIBLE);
                note.setText(item.note);
            }
        }
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}
