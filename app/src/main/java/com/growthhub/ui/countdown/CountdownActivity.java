package com.growthhub.ui.countdown;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.growthhub.R;
import com.growthhub.database.entity.CountdownEvent;
import com.growthhub.database.repository.CountdownRepository;
import com.growthhub.ui.management.ManagementAnimations;
import com.growthhub.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CountdownActivity extends AppCompatActivity {
    private CountdownRepository repository;
    private CountdownPremiumAdapter adapter;
    private EditText title;
    private EditText date;
    private EditText description;
    private Spinner remind;
    private android.widget.TextView totalCount;
    private android.widget.TextView highlightDays;
    private android.widget.TextView highlightTitle;
    private View emptyCard;
    private List<Integer> days;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new CountdownRepository(this);
        setContentView(R.layout.activity_countdown);
        title = findViewById(R.id.countdown_title_input);
        date = findViewById(R.id.countdown_date_input);
        description = findViewById(R.id.countdown_description_input);
        remind = findViewById(R.id.countdown_remind_spinner);
        totalCount = findViewById(R.id.countdown_total_count);
        highlightDays = findViewById(R.id.countdown_highlight_days);
        highlightTitle = findViewById(R.id.countdown_highlight_title_text);
        emptyCard = findViewById(R.id.countdown_empty_card);
        days = Arrays.asList(0, 1, 3, 7);
        remind.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days));
        adapter = new CountdownPremiumAdapter(event -> {
            repository.delete(event.id);
            render();
        });
        RecyclerView recycler = findViewById(R.id.countdown_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.countdown_add_button).setOnClickListener(v -> {
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
        animatePage();
        render();
    }

    private void render() {
        List<CountdownEvent> events = repository.getAll();
        totalCount.setText(String.valueOf(events.size()));
        adapter.submit(events);
        emptyCard.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
        CountdownEvent next = nearest(events);
        if (next == null) {
            highlightDays.setText("--");
            highlightTitle.setText(R.string.countdown_no_highlight);
        } else {
            long left = Math.max(0, (next.targetDate - TimeUtils.startOfToday()) / (24L * 60L * 60L * 1000L));
            highlightDays.setText(getString(R.string.countdown_days_left, left));
            highlightTitle.setText(next.title);
        }
    }

    private CountdownEvent nearest(List<CountdownEvent> events) {
        CountdownEvent result = null;
        long today = TimeUtils.startOfToday();
        for (CountdownEvent event : events) {
            if (event.targetDate < today) continue;
            if (result == null || event.targetDate < result.targetDate) {
                result = event;
            }
        }
        return result;
    }

    private void animatePage() {
        ManagementAnimations.enterStaggered(40, 80,
                findViewById(R.id.countdown_hero_card),
                findViewById(R.id.countdown_highlight_card),
                findViewById(R.id.countdown_form_card));
    }
}
