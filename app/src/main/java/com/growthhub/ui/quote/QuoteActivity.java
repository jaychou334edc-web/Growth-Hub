package com.growthhub.ui.quote;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growthhub.R;
import com.growthhub.database.repository.QuoteRepository;
import com.growthhub.util.UiUtils;

public class QuoteActivity extends AppCompatActivity {
    private QuoteRepository repository;
    private TextView quote;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new QuoteRepository(this);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(root);
        root.addView(UiUtils.title(this, getString(R.string.quote_title)));
        quote = UiUtils.text(this, "", 18);
        root.addView(quote);
        EditText input = new EditText(this);
        input.setHint(R.string.quote_input_hint);
        root.addView(input);
        Button add = UiUtils.button(this, getString(R.string.quote_save));
        root.addView(add);
        Button refresh = UiUtils.button(this, getString(R.string.quote_random));
        root.addView(refresh);
        setContentView(scrollView);

        add.setOnClickListener(v -> {
            if (!input.getText().toString().trim().isEmpty()) {
                repository.addUserQuote(input.getText().toString().trim());
                input.setText("");
                render();
            }
        });
        refresh.setOnClickListener(v -> render());
        render();
    }

    private void render() {
        quote.setText(repository.randomQuote());
    }
}
