package com.growthhub.ui.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.growthhub.R;
import com.growthhub.database.entity.Tag;
import com.growthhub.ui.management.ManagementAnimations;

import java.util.ArrayList;
import java.util.List;

public class TagPremiumAdapter extends RecyclerView.Adapter<TagPremiumAdapter.ViewHolder> {
    public interface Callback {
        void onDelete(Tag tag);
    }

    private final Callback callback;
    private final List<Tag> items = new ArrayList<>();
    private final int[] backgrounds = {
            R.drawable.bg_management_chip_green,
            R.drawable.bg_management_chip_blue,
            R.drawable.bg_management_chip_gold,
            R.drawable.bg_management_chip_coral
    };

    public TagPremiumAdapter(Callback callback) {
        this.callback = callback;
    }

    public void submit(List<Tag> tags) {
        items.clear();
        items.addAll(tags);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_premium, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tag tag = items.get(position);
        holder.name.setText("# " + tag.name);
        holder.name.setBackgroundResource(backgrounds[position % backgrounds.length]);
        holder.delete.setOnClickListener(v -> callback.onDelete(tag));
        ManagementAnimations.enter(holder.itemView, position * 45L);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final MaterialButton delete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tag_item_name);
            delete = itemView.findViewById(R.id.tag_item_delete);
        }
    }
}
