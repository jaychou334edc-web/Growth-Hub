package com.growthhub.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.growthhub.R;
import com.growthhub.database.entity.Category;
import com.growthhub.ui.management.ManagementAnimations;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private final List<Category> items = new ArrayList<>();

    public void submit(List<Category> categories) {
        items.clear();
        items.addAll(categories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_premium, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = items.get(position);
        holder.name.setText(category.name);
        String description = category.description == null || category.description.trim().isEmpty()
                ? holder.itemView.getContext().getString(R.string.category_card_description_empty)
                : category.description;
        holder.description.setText(description);
        ManagementAnimations.enter(holder.itemView, position * 45L);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView description;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.category_item_name);
            description = itemView.findViewById(R.id.category_item_description);
        }
    }
}
