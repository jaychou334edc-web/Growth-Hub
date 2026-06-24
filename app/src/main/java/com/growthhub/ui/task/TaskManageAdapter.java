package com.growthhub.ui.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.growthhub.R;
import com.growthhub.constant.TaskStatus;
import com.growthhub.database.entity.TaskItem;
import com.growthhub.ui.management.ManagementAnimations;

import java.util.ArrayList;
import java.util.List;

public class TaskManageAdapter extends RecyclerView.Adapter<TaskManageAdapter.ViewHolder> {
    public interface Callback {
        void onComplete(TaskItem task);

        void onArchive(TaskItem task);
    }

    public static class DisplayItem {
        final TaskItem task;
        final String category;
        final String tags;

        public DisplayItem(TaskItem task, String category, String tags) {
            this.task = task;
            this.category = category;
            this.tags = tags;
        }
    }

    private final Callback callback;
    private final List<DisplayItem> items = new ArrayList<>();

    public TaskManageAdapter(Callback callback) {
        this.callback = callback;
    }

    public void submit(List<DisplayItem> tasks) {
        items.clear();
        items.addAll(tasks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_premium, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DisplayItem item = items.get(position);
        holder.title.setText(item.task.title);
        holder.category.setText(holder.itemView.getContext().getString(R.string.task_category_prefix, item.category));
        holder.tags.setText(holder.itemView.getContext().getString(R.string.task_tags_prefix, item.tags));
        holder.status.setText(holder.itemView.getContext().getString(R.string.task_status_prefix, TaskStatus.label(item.task.status)));
        holder.complete.setEnabled(item.task.status != TaskStatus.COMPLETED);
        holder.archive.setEnabled(item.task.status != TaskStatus.ARCHIVED);
        holder.complete.setOnClickListener(v -> callback.onComplete(item.task));
        holder.archive.setOnClickListener(v -> callback.onArchive(item.task));
        ManagementAnimations.enter(holder.itemView, position * 45L);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView category;
        final TextView tags;
        final TextView status;
        final MaterialButton complete;
        final MaterialButton archive;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.task_item_title);
            category = itemView.findViewById(R.id.task_item_category);
            tags = itemView.findViewById(R.id.task_item_tags);
            status = itemView.findViewById(R.id.task_item_status);
            complete = itemView.findViewById(R.id.task_item_complete);
            archive = itemView.findViewById(R.id.task_item_archive);
        }
    }
}
