package com.growthhub.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.growthhub.database.repository.FocusRepository;
import com.growthhub.R;
import com.growthhub.ui.SimpleTextAdapter;

import java.util.List;

public class HistoryFragment extends Fragment {
    private final SimpleTextAdapter adapter = new SimpleTextAdapter();
    private RecyclerView recyclerView;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        recyclerView = root.findViewById(R.id.history_recycler);
        emptyView = root.findViewById(R.id.history_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        List<String> items = new FocusRepository(requireContext()).getHistoryText();
        adapter.submit(items);
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
