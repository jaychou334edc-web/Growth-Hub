package com.growthhub.ui.task;

import android.os.Bundle;
import android.widget.EditText;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.growthhub.R;
import com.growthhub.database.entity.Tag;
import com.growthhub.database.repository.TaskRepository;
import com.growthhub.ui.management.ManagementAnimations;

import java.util.List;

public class TagActivity extends AppCompatActivity {
    private TaskRepository repository;
    private TagPremiumAdapter adapter;
    private EditText name;
    private android.widget.TextView totalCount;
    private View emptyCard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new TaskRepository(this);
        setContentView(R.layout.activity_tag);
        name = findViewById(R.id.tag_name_input);
        totalCount = findViewById(R.id.tag_total_count);
        emptyCard = findViewById(R.id.tag_empty_card);
        adapter = new TagPremiumAdapter(tag -> {
            repository.deleteTag(tag.id);
            renderList();
        });
        RecyclerView recycler = findViewById(R.id.tag_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.tag_add_button).setOnClickListener(v -> {
            String tagName = name.getText().toString().trim();
            if (tagName.isEmpty()) {
                name.setError(getString(R.string.validation_tag_name_required));
                name.requestFocus();
                return;
            }
            repository.createTag(tagName);
            name.setText("");
            renderList();
        });
        animatePage();
        renderList();
    }

    private void renderList() {
        List<Tag> tags = repository.getTags();
        totalCount.setText(String.valueOf(tags.size()));
        adapter.submit(tags);
        emptyCard.setVisibility(tags.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void animatePage() {
        ManagementAnimations.enterStaggered(40, 80,
                findViewById(R.id.tag_hero_card),
                findViewById(R.id.tag_form_card));
    }
}
