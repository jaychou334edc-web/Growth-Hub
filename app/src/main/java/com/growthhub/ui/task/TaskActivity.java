package com.growthhub.ui.task;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growthhub.R;
import com.growthhub.constant.TaskStatus;
import com.growthhub.database.entity.Category;
import com.growthhub.database.entity.TaskItem;
import com.growthhub.database.repository.TaskRepository;
import com.growthhub.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity {
    private TaskRepository repository;
    private final List<Category> categories = new ArrayList<>();
    private LinearLayout list;
    private Spinner categorySpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new TaskRepository(this);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(root);
        root.addView(UiUtils.title(this, getString(R.string.task_title)));

        categorySpinner = new Spinner(this);
        root.addView(categorySpinner);
        EditText title = new EditText(this);
        title.setHint(R.string.task_title_hint);
        root.addView(title);
        EditText description = new EditText(this);
        description.setHint(R.string.task_description_hint);
        root.addView(description);
        EditText tags = new EditText(this);
        tags.setHint(R.string.task_tags_hint);
        root.addView(tags);
        Button add = UiUtils.button(this, getString(R.string.task_add));
        root.addView(add);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        root.addView(list);
        setContentView(scrollView);

        add.setOnClickListener(v -> {
            if (categories.isEmpty() || title.getText().toString().trim().isEmpty()) return;
            Category category = categories.get(categorySpinner.getSelectedItemPosition());
            repository.createTask(category.id, title.getText().toString().trim(), description.getText().toString().trim(), parseTagIds(tags.getText().toString()));
            title.setText("");
            description.setText("");
            tags.setText("");
            Toast.makeText(this, R.string.task_created, Toast.LENGTH_SHORT).show();
            render();
        });
        render();
    }

    private void render() {
        categories.clear();
        categories.addAll(repository.getCategories());
        List<String> labels = new ArrayList<>();
        for (Category category : categories) labels.add(category.name);
        categorySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));

        list.removeAllViews();
        for (TaskItem task : repository.getAllTasksForStatisticsAwareViews()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.addView(UiUtils.text(this, "#" + task.id + " " + task.title + " [" + TaskStatus.label(task.status) + "]\n" + task.description, 15));
            Button complete = UiUtils.button(this, getString(R.string.task_complete));
            complete.setOnClickListener(v -> {
                repository.completeTask(task.id);
                render();
            });
            Button archive = UiUtils.button(this, getString(R.string.task_archive));
            archive.setOnClickListener(v -> {
                repository.archiveTask(task.id);
                render();
            });
            row.addView(complete);
            row.addView(archive);
            list.addView(row);
        }
    }

    private List<Long> parseTagIds(String text) {
        List<Long> ids = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return ids;
        for (String part : text.split(",")) {
            try {
                ids.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }
}
