package com.growthhub.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class UiUtils {
    private UiUtils() {
    }

    public static TextView title(Context context, String text) {
        TextView view = text(context, text, 22);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
    }

    public static TextView subtitle(Context context, String text) {
        TextView view = text(context, text, 18);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
    }

    public static TextView text(Context context, String text, int sp) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(0xFF17201D);
        view.setPadding(dp(context, 16), dp(context, 8), dp(context, 16), dp(context, 8));
        view.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    public static Button button(Context context, String text) {
        Button button = new Button(context);
        button.setText(text);
        button.setAllCaps(false);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return button;
    }

    public static int dp(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
