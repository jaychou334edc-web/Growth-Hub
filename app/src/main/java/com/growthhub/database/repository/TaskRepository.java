package com.growthhub.database.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.growthhub.constant.TaskStatus;
import com.growthhub.database.dao.CategoryDao;
import com.growthhub.database.dao.TagDao;
import com.growthhub.database.dao.TaskDao;
import com.growthhub.database.dao.TaskTagDao;
import com.growthhub.database.entity.Category;
import com.growthhub.database.entity.Tag;
import com.growthhub.database.entity.TaskItem;
import com.growthhub.database.helper.GrowthHubDbHelper;

import java.util.List;

public class TaskRepository {
    private final GrowthHubDbHelper helper;
    private final CategoryDao categoryDao;
    private final TaskDao taskDao;
    private final TagDao tagDao;
    private final TaskTagDao taskTagDao;

    public TaskRepository(Context context) {
        helper = GrowthHubDbHelper.getInstance(context);
        categoryDao = new CategoryDao(helper);
        taskDao = new TaskDao(helper);
        tagDao = new TagDao(helper);
        taskTagDao = new TaskTagDao(helper);
    }

    public long createCategory(String name, String description) {
        return categoryDao.insert(name, description);
    }

    public List<Category> getCategories() {
        return categoryDao.getAll();
    }

    public long createTag(String name) {
        return tagDao.insert(name);
    }

    public void deleteTag(long tagId) {
        tagDao.delete(tagId);
    }

    public List<Tag> getTags() {
        return tagDao.getAll();
    }

    public long createTask(long categoryId, String title, String description, List<Long> tagIds) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            long taskId = taskDao.insert(categoryId, title, description);
            if (tagIds != null) {
                for (Long tagId : tagIds) taskTagDao.bind(taskId, tagId);
            }
            db.setTransactionSuccessful();
            return taskId;
        } finally {
            db.endTransaction();
        }
    }

    public void completeTask(long taskId) {
        taskDao.updateStatus(taskId, TaskStatus.COMPLETED);
    }

    public void archiveTask(long taskId) {
        taskDao.updateStatus(taskId, TaskStatus.ARCHIVED);
    }

    public List<TaskItem> getAllTasksForStatisticsAwareViews() {
        return taskDao.getAll();
    }

    public List<TaskItem> getSelectableTasks() {
        return taskDao.getSelectable();
    }

    public TaskItem getTask(long id) {
        return taskDao.getById(id);
    }
}
