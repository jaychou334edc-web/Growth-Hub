package com.growthhub.database.repository;

import android.content.Context;

import com.growthhub.database.dao.QuoteDao;
import com.growthhub.database.entity.Quote;
import com.growthhub.database.helper.GrowthHubDbHelper;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

public class QuoteRepository {
    private final Context context;
    private final QuoteDao quoteDao;

    public QuoteRepository(Context context) {
        this.context = context.getApplicationContext();
        quoteDao = new QuoteDao(GrowthHubDbHelper.getInstance(context));
    }

    public long addUserQuote(String content) {
        return quoteDao.insertUserQuote(content);
    }

    public String randomQuote() {
        Quote userQuote = quoteDao.getRandomUserQuote();
        if (userQuote != null) return userQuote.content;
        try {
            InputStream in = context.getAssets().open("quotes.json");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            in.close();
            JSONArray array = new JSONArray(out.toString("UTF-8"));
            if (array.length() == 0) return "先开始，再调整。";
            return array.getString(new Random().nextInt(array.length()));
        } catch (Exception e) {
            return "先开始，再调整。";
        }
    }
}
