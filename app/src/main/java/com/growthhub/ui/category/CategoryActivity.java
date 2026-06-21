package com.growthhub.ui.category;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growthhub.database.entity.Category;
import com.growthhub.database.repository.TaskRepository;
import com.growthhub.util.UiUtils;

public class CategoryActivity extends AppCompatActivity {
    private TaskRepository repository;
    private LinearLayout list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new TaskRepository(this);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(root);

        root.addView(UiUtils.title(this, "分类管理"));
        EditText name = new EditText(this);
        name.setHint("分类名称");
        root.addView(name);
        EditText description = new EditText(this);
        description.setHint("描述");
        root.addView(description);
        Button add = UiUtils.button(this, "新增分类");
        root.addView(add);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        root.addView(list);
        setContentView(scrollView);

        add.setOnClickListener(v -> {
            if (name.getText().toString().trim().isEmpty()) return;
            repository.createCategory(name.getText().toString().trim(), description.getText().toString().trim());
            name.setText("");
            description.setText("");
            Toast.makeText(this, "已新增", Toast.LENGTH_SHORT).show();
            renderList();
        });
        renderList();
    }

    private void renderList() {
        list.removeAllViews();
        for (Category category : repository.getCategories()) {
            list.addView(UiUtils.text(this, "#" + category.id + " " + category.name + "\n" + category.description, 15));
        }
    }
}
