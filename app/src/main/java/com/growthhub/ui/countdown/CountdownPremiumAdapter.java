package com.growthhub.ui.countdown;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.growthhub.R;
import com.growthhub.database.entity.CountdownEvent;
import com.growthhub.ui.management.ManagementAnimations;
import com.growthhub.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class CountdownPremiumAdapter extends RecyclerView.Adapter<CountdownPremiumAdapter.ViewHolder> {
    public interface Callback {
        void onDelete(CountdownEvent event);
    }

    private final Callback callback;
    private final List<CountdownEvent> items = new ArrayList<>();

    public CountdownPremiumAdapter(Callback callback) {
        this.callback = callback;
    }

    public void submit(List<CountdownEvent> events) {
        items.clear();
        items.addAll(events);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_countdown_premium, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CountdownEvent event = items.get(position);
        long days = Math.max(0, (event.targetDate - TimeUtils.startOfToday()) / (24L * 60L * 60L * 1000L));
        holder.title.setText(event.title);
        holder.days.setText(holder.itemView.getContext().getString(R.string.countdown_days_left, days));
        holder.target.setText(holder.itemView.getContext().getString(R.string.countdown_target_date, TimeUtils.formatDate(event.targetDate)));
        holder.remind.setText(holder.itemView.getContext().getString(R.string.countdown_remind_days, event.remindDaysBefore));
        holder.delete.setOnClickListener(v -> callback.onDelete(event));
        ManagementAnimations.enter(holder.itemView, position * 45L);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView days;
        final TextView target;
        final TextView remind;
        final MaterialButton delete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.countdown_item_title);
            days = itemView.findViewById(R.id.countdown_item_days);
            target = itemView.findViewById(R.id.countdown_item_target);
            remind = itemView.findViewById(R.id.countdown_item_remind);
            delete = itemView.findViewById(R.id.countdown_item_delete);
        }
    }
}
