package com.growthhub.ui.task;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growthhub.database.entity.Tag;
import com.growthhub.database.repository.TaskRepository;
import com.growthhub.util.UiUtils;

public class TagActivity extends AppCompatActivity {
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
        root.addView(UiUtils.title(this, "标签管理"));
        EditText name = new EditText(this);
        name.setHint("标签名称");
        root.addView(name);
        Button add = UiUtils.button(this, "新增标签");
        root.addView(add);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        root.addView(list);
        setContentView(scrollView);

        add.setOnClickListener(v -> {
            if (!name.getText().toString().trim().isEmpty()) {
                repository.createTag(name.getText().toString().trim());
                name.setText("");
                renderList();
            }
        });
        renderList();
    }

    private void renderList() {
        list.removeAllViews();
        for (Tag tag : repository.getTags()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.addView(UiUtils.text(this, "#" + tag.id + " " + tag.name, 15));
            Button delete = UiUtils.button(this, "删除该标签");
            delete.setOnClickListener(v -> {
                repository.deleteTag(tag.id);
                renderList();
            });
            row.addView(delete);
            list.addView(row);
        }
    }
}
