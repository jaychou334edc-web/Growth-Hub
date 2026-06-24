package com.growthhub.ui.category;

import android.os.Bundle;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.growthhub.R;
import com.growthhub.database.entity.Category;
import com.growthhub.database.repository.TaskRepository;
import com.growthhub.ui.management.ManagementAnimations;

import java.util.List;

public class CategoryActivity extends AppCompatActivity {
    private TaskRepository repository;
    private final CategoryAdapter adapter = new CategoryAdapter();
    private EditText name;
    private EditText description;
    private View emptyCard;
    private android.widget.TextView totalCount;
    private android.widget.TextView activeCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new TaskRepository(this);
        setContentView(R.layout.activity_category);
        name = findViewById(R.id.category_name_input);
        description = findViewById(R.id.category_description_input);
        emptyCard = findViewById(R.id.category_empty_card);
        totalCount = findViewById(R.id.category_total_count);
        activeCount = findViewById(R.id.category_active_count);
        RecyclerView recycler = findViewById(R.id.category_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.category_add_button).setOnClickListener(v -> {
            if (name.getText().toString().trim().isEmpty()) return;
            repository.createCategory(name.getText().toString().trim(), description.getText().toString().trim());
            name.setText("");
            description.setText("");
            Toast.makeText(this, R.string.category_created, Toast.LENGTH_SHORT).show();
            renderList();
        });
        animatePage();
        renderList();
    }

    private void renderList() {
        List<Category> categories = repository.getCategories();
        totalCount.setText(String.valueOf(categories.size()));
        activeCount.setText(getString(R.string.category_active_count) + " · " + categories.size());
        adapter.submit(categories);
        emptyCard.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void animatePage() {
        ManagementAnimations.enterStaggered(40, 80,
                findViewById(R.id.category_hero_card),
                findViewById(R.id.category_form_card));
    }
}
