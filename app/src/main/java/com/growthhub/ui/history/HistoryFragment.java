package com.growthhub.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.growthhub.database.repository.FocusRepository;
import com.growthhub.ui.SimpleTextAdapter;
import com.growthhub.util.UiUtils;

public class HistoryFragment extends Fragment {
    private final SimpleTextAdapter adapter = new SimpleTextAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView(UiUtils.title(requireContext(), "专注历史"));
        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        root.addView(recyclerView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.submit(new FocusRepository(requireContext()).getHistoryText());
    }
}
