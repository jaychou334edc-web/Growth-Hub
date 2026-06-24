package com.growthhub.util;

import java.util.Locale;

public final class DurationFormatter {
    private DurationFormatter() {
    }

    public static String format(long seconds) {
        long safe = Math.max(0, seconds);
        long hours = safe / 3600;
        long minutes = (safe % 3600) / 60;
        if (hours > 0 && minutes > 0) {
            return String.format(Locale.getDefault(), "%1$dh %2$dmin", hours, minutes);
        }
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%1$dh", hours);
        }
        if (minutes > 0) {
            return String.format(Locale.getDefault(), "%1$dmin", minutes);
        }
        return String.format(Locale.getDefault(), "%1$ds", safe);
    }
}
