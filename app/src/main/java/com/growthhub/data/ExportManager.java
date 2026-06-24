package com.growthhub.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.growthhub.constant.FocusMode;
import com.growthhub.constant.Mood;
import com.growthhub.constant.TaskStatus;
import com.growthhub.database.entity.CountdownEvent;
import com.growthhub.database.entity.FocusRecord;
import com.growthhub.database.entity.TaskItem;
import com.growthhub.database.repository.CountdownRepository;
import com.growthhub.database.repository.FocusRepository;
import com.growthhub.database.repository.TaskRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportManager {
    private static final String EXPORT_DIR = "GrowthHub";
    private static final String MIME_CSV = "text/csv";
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final Context context;
    private final TaskRepository taskRepository;
    private final FocusRepository focusRepository;
    private final CountdownRepository countdownRepository;

    public ExportManager(Context context) {
        this.context = context.getApplicationContext();
        taskRepository = new TaskRepository(context);
        focusRepository = new FocusRepository(context);
        countdownRepository = new CountdownRepository(context);
    }

    public String exportCsv() throws IOException {
        String fileName = "growthhub_export_" +
                new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".csv";
        String csv = buildCsv();
        try (OutputStream out = openOutput(fileName)) {
            out.write(UTF8_BOM);
            out.write(csv.getBytes(StandardCharsets.UTF_8));
        }
        return "Documents/GrowthHub/" + fileName;
    }

    private String buildCsv() {
        StringBuilder builder = new StringBuilder();
        appendTasks(builder, taskRepository.getAllTasksForStatisticsAwareViews());
        builder.append('\n');
        appendFocusRecords(builder, focusRepository.getRecent(100000));
        builder.append('\n');
        appendCountdowns(builder, countdownRepository.getAll());
        return builder.toString();
    }

    private void appendTasks(StringBuilder builder, List<TaskItem> tasks) {
        builder.append("任务\n");
        builder.append("id,category_id,title,description,status,create_time,update_time\n");
        for (TaskItem task : tasks) {
            row(builder,
                    String.valueOf(task.id),
                    String.valueOf(task.categoryId),
                    task.title,
                    task.description,
                    TaskStatus.label(task.status),
                    formatTime(task.createTime),
                    formatTime(task.updateTime));
        }
    }

    private void appendFocusRecords(StringBuilder builder, List<FocusRecord> records) {
        builder.append("专注记录\n");
        builder.append("id,task_id,start_time,end_time,duration_seconds,mode,mood,note,create_time\n");
        for (FocusRecord record : records) {
            row(builder,
                    String.valueOf(record.id),
                    String.valueOf(record.taskId),
                    formatTime(record.startTime),
                    formatTime(record.endTime),
                    String.valueOf(record.duration),
                    FocusMode.label(record.mode),
                    Mood.label(record.mood),
                    record.note,
                    formatTime(record.createTime));
        }
    }

    private void appendCountdowns(StringBuilder builder, List<CountdownEvent> events) {
        builder.append("倒数日\n");
        builder.append("id,title,target_date,description,remind_days_before,create_time\n");
        for (CountdownEvent event : events) {
            row(builder,
                    String.valueOf(event.id),
                    event.title,
                    formatDate(event.targetDate),
                    event.description,
                    String.valueOf(event.remindDaysBefore),
                    formatTime(event.createTime));
        }
    }

    private void row(StringBuilder builder, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) builder.append(',');
            builder.append(escape(values[i]));
        }
        builder.append('\n');
    }

    private String escape(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private String formatTime(long timestamp) {
        if (timestamp <= 0) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
    }

    private String formatDate(long timestamp) {
        if (timestamp <= 0) return "";
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(timestamp));
    }

    private OutputStream openOutput(String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            String relativePath = Environment.DIRECTORY_DOCUMENTS + "/" + EXPORT_DIR + "/";
            deleteExisting(resolver, collection, fileName, relativePath);
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, MIME_CSV);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
            Uri uri = resolver.insert(collection, values);
            if (uri == null) throw new IOException("无法创建导出文件");
            OutputStream out = resolver.openOutputStream(uri);
            if (out == null) throw new IOException("无法打开导出文件");
            return out;
        }
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), EXPORT_DIR);
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("无法创建导出目录");
        return new FileOutputStream(new File(dir, fileName), false);
    }

    private void deleteExisting(ContentResolver resolver, Uri collection, String fileName, String relativePath) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;
        resolver.delete(collection,
                MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " + MediaStore.MediaColumns.RELATIVE_PATH + "=?",
                new String[]{fileName, relativePath});
    }
}
