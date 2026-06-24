package com.growthhub.ui.task;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.growthhub.R;
import com.growthhub.constant.TaskStatus;
import com.growthhub.database.dao.TaskTagDao;
import com.growthhub.database.entity.Category;
import com.growthhub.database.entity.Tag;
import com.growthhub.database.entity.TaskItem;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.database.repository.TaskRepository;
import com.growthhub.ui.management.ManagementAnimations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskActivity extends AppCompatActivity {
    private TaskRepository repository;
    private TaskTagDao taskTagDao;
    private final List<Category> categories = new ArrayList<>();
    private TaskManageAdapter adapter;
    private Spinner categorySpinner;
    private EditText title;
    private EditText description;
    private EditText tags;
    private android.widget.TextView totalCount;
    private android.widget.TextView completedCount;
    private View emptyCard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new TaskRepository(this);
        taskTagDao = new TaskTagDao(GrowthHubDbHelper.getInstance(this));
        setContentView(R.layout.activity_task);
        categorySpinner = findViewById(R.id.task_category_spinner);
        title = findViewById(R.id.task_title_input);
        description = findViewById(R.id.task_description_input);
        tags = findViewById(R.id.task_tags_input);
        totalCount = findViewById(R.id.task_total_count);
        completedCount = findViewById(R.id.task_completed_count);
        emptyCard = findViewById(R.id.task_empty_card);
        adapter = new TaskManageAdapter(new TaskManageAdapter.Callback() {
            @Override
            public void onComplete(TaskItem task) {
                repository.completeTask(task.id);
                render();
            }

            @Override
            public void onArchive(TaskItem task) {
                repository.archiveTask(task.id);
                render();
            }
        });
        RecyclerView recycler = findViewById(R.id.task_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.task_add_button).setOnClickListener(v -> {
            String taskTitle = title.getText().toString().trim();
            String tagText = tags.getText().toString();
            if (categories.isEmpty()) {
                Toast.makeText(this, R.string.validation_task_category_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (taskTitle.isEmpty()) {
                title.setError(getString(R.string.validation_task_title_required));
                title.requestFocus();
                return;
            }
            if (!areTagIdsValid(tagText)) {
                tags.setError(getString(R.string.validation_tag_ids_invalid));
                tags.requestFocus();
                return;
            }
            Category category = categories.get(categorySpinner.getSelectedItemPosition());
            repository.createTask(category.id, taskTitle, description.getText().toString().trim(), parseTagIds(tagText));
            title.setText("");
            description.setText("");
            tags.setText("");
            Toast.makeText(this, R.string.task_created, Toast.LENGTH_SHORT).show();
            render();
        });
        animatePage();
        render();
    }

    private void render() {
        categories.clear();
        categories.addAll(repository.getCategories());
        List<String> labels = new ArrayList<>();
        for (Category category : categories) labels.add(category.name);
        categorySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));

        List<TaskItem> tasks = repository.getAllTasksForStatisticsAwareViews();
        totalCount.setText(String.valueOf(tasks.size()));
        completedCount.setText(getString(R.string.task_completed_count) + " · " + countCompleted(tasks));

        Map<Long, String> categoryNames = new HashMap<>();
        for (Category category : categories) categoryNames.put(category.id, category.name);
        Map<Long, String> tagNames = new HashMap<>();
        for (Tag tag : repository.getTags()) tagNames.put(tag.id, tag.name);

        List<TaskManageAdapter.DisplayItem> displayItems = new ArrayList<>();
        for (TaskItem task : tasks) {
            displayItems.add(new TaskManageAdapter.DisplayItem(
                    task,
                    categoryNames.getOrDefault(task.categoryId, getString(R.string.history_uncategorized)),
                    tagLabel(task.id, tagNames)
            ));
        }
        adapter.submit(displayItems);
        emptyCard.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean areTagIdsValid(String text) {
        if (text == null || text.trim().isEmpty()) return true;
        for (String part : text.split(",")) {
            if (part.trim().isEmpty()) return false;
            try {
                Long.parseLong(part.trim());
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        return true;
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

    private String tagLabel(long taskId, Map<Long, String> tagNames) {
        List<Long> ids = taskTagDao.getTagIds(taskId);
        if (ids.isEmpty()) return getString(R.string.task_tags_empty);
        List<String> labels = new ArrayList<>();
        for (Long id : ids) labels.add(tagNames.getOrDefault(id, "#" + id));
        return android.text.TextUtils.join(", ", labels);
    }

    private int countCompleted(List<TaskItem> tasks) {
        int count = 0;
        for (TaskItem task : tasks) {
            if (task.status == TaskStatus.COMPLETED) count++;
        }
        return count;
    }

    private void animatePage() {
        ManagementAnimations.enterStaggered(40, 80,
                findViewById(R.id.task_hero_card),
                findViewById(R.id.task_form_card));
    }
}
