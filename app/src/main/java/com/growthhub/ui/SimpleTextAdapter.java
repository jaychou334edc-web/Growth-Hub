package com.growthhub.ui;

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.growthhub.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.Holder> {
    private final List<String> items = new ArrayList<>();

    public void submit(List<String> next) {
        items.clear();
        items.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView view = UiUtils.text(parent.getContext(), "", 15);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.textView.setText(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView textView;

        Holder(TextView itemView) {
            super(itemView);
            textView = itemView;
        }
    }
}
