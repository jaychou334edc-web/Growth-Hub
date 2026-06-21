package com.growthhub.ui.countdown;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growthhub.database.entity.CountdownEvent;
import com.growthhub.database.repository.CountdownRepository;
import com.growthhub.util.TimeUtils;
import com.growthhub.util.UiUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CountdownActivity extends AppCompatActivity {
    private CountdownRepository repository;
    private LinearLayout list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new CountdownRepository(this);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(root);
        root.addView(UiUtils.title(this, "倒数日"));

        EditText title = new EditText(this);
        title.setHint("标题，例如 距离考研");
        root.addView(title);
        EditText date = new EditText(this);
        date.setHint("目标日期 yyyy-MM-dd");
        root.addView(date);
        EditText description = new EditText(this);
        description.setHint("描述");
        root.addView(description);
        Spinner remind = new Spinner(this);
        List<Integer> days = Arrays.asList(0, 1, 3, 7);
        remind.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days));
        root.addView(remind);
        Button add = UiUtils.button(this, "新增倒数日");
        root.addView(add);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        root.addView(list);
        setContentView(scrollView);

        add.setOnClickListener(v -> {
            try {
                Date target = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date.getText().toString().trim());
                if (target == null) return;
                repository.create(title.getText().toString().trim(), target.getTime(), description.getText().toString().trim(), days.get(remind.getSelectedItemPosition()));
                title.setText("");
                date.setText("");
                description.setText("");
                render();
            } catch (Exception ignored) {
            }
        });
        render();
    }

    private void render() {
        list.removeAllViews();
        long now = System.currentTimeMillis();
        for (CountdownEvent event : repository.getAll()) {
            long daysLeft = (event.targetDate - now) / (24L * 60L * 60L * 1000L);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.addView(UiUtils.text(this, event.title + "\n目标：" + TimeUtils.formatDate(event.targetDate) +
                    "\n剩余：" + daysLeft + "天\n提醒：提前" + event.remindDaysBefore + "天", 15));
            Button delete = UiUtils.button(this, "删除");
            delete.setOnClickListener(v -> {
                repository.delete(event.id);
                render();
            });
            row.addView(delete);
            list.addView(row);
        }
    }
}
