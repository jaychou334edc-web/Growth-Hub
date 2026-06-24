package com.growthhub.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.growthhub.database.helper.GrowthHubDbHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BackupManager {
    private static final String DB_NAME = "growth_hub.db";
    private static final String BACKUP_FILE = "growthhub_backup.db";
    private static final String BACKUP_DIR = "GrowthHub/backup";
    private static final String MIME_DB = "application/octet-stream";
    private static final String[] REQUIRED_TABLES = {
            "category", "task", "tag", "task_tag", "focus_record",
            "countdown_event", "quote", "achievement"
    };

    private final Context context;

    public BackupManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public String backupDatabase() throws IOException {
        GrowthHubDbHelper.getInstance(context).close();
        File dbFile = context.getDatabasePath(DB_NAME);
        if (!dbFile.exists()) throw new IOException("数据库文件不存在");
        try (InputStream in = new FileInputStream(dbFile);
             OutputStream out = openBackupOutput()) {
            copy(in, out);
        }
        return "Documents/GrowthHub/backup/" + BACKUP_FILE;
    }

    public void restoreDatabase(Uri backupUri) throws IOException {
        File dbFile = context.getDatabasePath(DB_NAME);
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("无法创建数据库目录");
        }
        File tempFile = new File(parent, DB_NAME + ".restore_tmp");
        File oldFile = new File(parent, DB_NAME + ".restore_old");
        try (InputStream in = context.getContentResolver().openInputStream(backupUri);
             OutputStream out = new FileOutputStream(tempFile, false)) {
            if (in == null) throw new IOException("无法读取备份文件");
            copy(in, out);
        }
        validateBackup(tempFile);

        GrowthHubDbHelper.getInstance(context).close();
        deleteSidecars(dbFile);
        if (oldFile.exists() && !oldFile.delete()) {
            throw new IOException("无法清理旧恢复文件");
        }
        if (dbFile.exists() && !dbFile.renameTo(oldFile)) {
            throw new IOException("无法暂存当前数据库");
        }
        if (!tempFile.renameTo(dbFile)) {
            if (oldFile.exists()) {
                oldFile.renameTo(dbFile);
            }
            throw new IOException("无法替换数据库文件");
        }
        if (oldFile.exists()) {
            oldFile.delete();
        }
        GrowthHubDbHelper.getInstance(context).getWritableDatabase();
    }

    private OutputStream openBackupOutput() throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            String relativePath = Environment.DIRECTORY_DOCUMENTS + "/" + BACKUP_DIR + "/";
            resolver.delete(collection,
                    MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " + MediaStore.MediaColumns.RELATIVE_PATH + "=?",
                    new String[]{BACKUP_FILE, relativePath});
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, BACKUP_FILE);
            values.put(MediaStore.MediaColumns.MIME_TYPE, MIME_DB);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
            Uri uri = resolver.insert(collection, values);
            if (uri == null) throw new IOException("无法创建备份文件");
            OutputStream out = resolver.openOutputStream(uri);
            if (out == null) throw new IOException("无法打开备份文件");
            return out;
        }
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), BACKUP_DIR);
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("无法创建备份目录");
        return new FileOutputStream(new File(dir, BACKUP_FILE), false);
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }

    private void validateBackup(File file) throws IOException {
        validateSqliteHeader(file);
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            for (String table : REQUIRED_TABLES) {
                if (!hasTable(db, table)) {
                    throw new IOException("备份文件缺少表：" + table);
                }
            }
        } catch (SQLiteException e) {
            throw new IOException("备份文件不是有效的 Growth Hub 数据库", e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private void validateSqliteHeader(File file) throws IOException {
        byte[] header = new byte[16];
        try (InputStream in = new FileInputStream(file)) {
            int read = in.read(header);
            if (read != header.length) {
                throw new IOException("备份文件不完整");
            }
        }
        String signature = new String(header, StandardCharsets.US_ASCII);
        if (!"SQLite format 3\u0000".equals(signature)) {
            throw new IOException("备份文件格式无效");
        }
    }

    private boolean hasTable(SQLiteDatabase db, String table) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{table});
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    private void deleteSidecars(File dbFile) {
        deleteFile(new File(dbFile.getPath() + "-journal"));
        deleteFile(new File(dbFile.getPath() + "-wal"));
        deleteFile(new File(dbFile.getPath() + "-shm"));
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }
}
