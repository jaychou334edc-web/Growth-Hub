package com.growthhub.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.growthhub.database.repository.StatisticsRepository;

public class StatsProvider extends ContentProvider {
    public static final Uri URI = Uri.parse("content://growthhub/focus");

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        StatisticsRepository repository = new StatisticsRepository(getContext());
        MatrixCursor cursor = new MatrixCursor(new String[]{"today_duration", "total_duration", "active_rate"});
        cursor.addRow(new Object[]{
                repository.engine().todayDuration(),
                repository.engine().totalDuration(),
                repository.engine().activeRate30DaysPercent()
        });
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.item/vnd.growthhub.focus";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }
}
