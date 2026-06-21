package com.growthhub.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.growthhub.database.entity.Quote;
import com.growthhub.database.helper.GrowthHubDbHelper;

public class QuoteDao {
    private final GrowthHubDbHelper helper;

    public QuoteDao(GrowthHubDbHelper helper) {
        this.helper = helper;
    }

    public long insertUserQuote(String content) {
        ContentValues values = new ContentValues();
        values.put("content", content);
        values.put("is_system", 0);
        values.put("create_time", System.currentTimeMillis());
        return helper.getWritableDatabase().insert("quote", null, values);
    }

    public Quote getRandomUserQuote() {
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT * FROM quote WHERE is_system=0 ORDER BY RANDOM() LIMIT 1", null);
        try {
            if (c.moveToFirst()) {
                Quote quote = new Quote();
                quote.id = c.getLong(c.getColumnIndexOrThrow("id"));
                quote.content = c.getString(c.getColumnIndexOrThrow("content"));
                quote.isSystem = c.getInt(c.getColumnIndexOrThrow("is_system"));
                quote.createTime = c.getLong(c.getColumnIndexOrThrow("create_time"));
                return quote;
            }
            return null;
        } finally {
            c.close();
        }
    }
}
