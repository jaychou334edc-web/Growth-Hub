package com.growthhub.ui.quote;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growthhub.R;
import com.growthhub.database.helper.GrowthHubDbHelper;
import com.growthhub.database.repository.QuoteRepository;
import com.growthhub.ui.management.ManagementAnimations;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class QuoteActivity extends AppCompatActivity {
    private QuoteRepository repository;
    private TextView quote;
    private TextView totalCount;
    private EditText input;
    private View emptyCard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new QuoteRepository(this);
        setContentView(R.layout.activity_quote);
        quote = findViewById(R.id.quote_text);
        totalCount = findViewById(R.id.quote_total_count);
        input = findViewById(R.id.quote_input);
        emptyCard = findViewById(R.id.quote_empty_card);

        findViewById(R.id.quote_add_button).setOnClickListener(v -> {
            if (!input.getText().toString().trim().isEmpty()) {
                repository.addUserQuote(input.getText().toString().trim());
                input.setText("");
                render();
            }
        });
        findViewById(R.id.quote_refresh_button).setOnClickListener(v -> render());
        animatePage();
        render();
    }

    private void render() {
        quote.setText(repository.randomQuote());
        int userQuoteCount = userQuoteCount();
        totalCount.setText(String.valueOf(userQuoteCount + systemQuoteCount()));
        emptyCard.setVisibility(userQuoteCount == 0 ? View.VISIBLE : View.GONE);
    }

    private int userQuoteCount() {
        Cursor cursor = GrowthHubDbHelper.getInstance(this)
                .getReadableDatabase()
                .rawQuery("SELECT COUNT(*) FROM quote WHERE is_system=0", null);
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    private int systemQuoteCount() {
        try {
            InputStream in = getAssets().open("quotes.json");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            return new JSONArray(out.toString("UTF-8")).length();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void animatePage() {
        ManagementAnimations.enterStaggered(40, 80,
                findViewById(R.id.quote_hero_card),
                findViewById(R.id.quote_card),
                findViewById(R.id.quote_form_card));
    }
}
