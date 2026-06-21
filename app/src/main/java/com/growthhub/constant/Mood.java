package com.growthhub.constant;

public final class Mood {
    public static final int GREAT = 0;
    public static final int GOOD = 1;
    public static final int NORMAL = 2;
    public static final int BAD = 3;

    private Mood() {
    }

    public static String label(int mood) {
        if (mood == GREAT) return "GREAT";
        if (mood == GOOD) return "GOOD";
        if (mood == BAD) return "BAD";
        return "NORMAL";
    }
}
