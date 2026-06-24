package com.growthhub.ui.management;

import android.view.View;
import android.view.animation.DecelerateInterpolator;

public final class ManagementAnimations {
    private ManagementAnimations() {
    }

    public static void enter(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(dp(view, 22));
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(340)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public static void enterStaggered(long firstDelay, long stepDelay, View... views) {
        for (int i = 0; i < views.length; i++) {
            enter(views[i], firstDelay + i * stepDelay);
        }
    }

    public static int dp(View view, int value) {
        return (int) (value * view.getResources().getDisplayMetrics().density);
    }
}
