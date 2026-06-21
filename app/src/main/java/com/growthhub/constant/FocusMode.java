package com.growthhub.constant;

public final class FocusMode {
    public static final int COUNTDOWN = 0;
    public static final int COUNTUP = 1;

    private FocusMode() {
    }

    public static String label(int mode) {
        return mode == COUNTUP ? "正计时" : "倒计时";
    }
}
