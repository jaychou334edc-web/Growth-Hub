package com.growthhub.constant;

public final class TaskStatus {
    public static final int ACTIVE = 0;
    public static final int COMPLETED = 1;
    public static final int ARCHIVED = 2;

    private TaskStatus() {
    }

    public static String label(int status) {
        if (status == COMPLETED) return "已完成";
        if (status == ARCHIVED) return "已归档";
        return "进行中";
    }
}
