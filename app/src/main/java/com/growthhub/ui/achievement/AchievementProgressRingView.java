package com.growthhub.ui.achievement;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.growthhub.R;

public class AchievementProgressRingView extends View {
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private int progress;

    public AchievementProgressRingView(Context context) {
        super(context);
        init();
    }

    public AchievementProgressRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(dp(12));
        trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.achievement_glass_strong));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(dp(12));
        progressPaint.setColor(ContextCompat.getColor(getContext(), R.color.achievement_gold));

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.achievement_text_primary));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(24));
        textPaint.setFakeBoldText(true);
    }

    public void animateTo(int targetProgress) {
        int target = Math.max(0, Math.min(100, targetProgress));
        ValueAnimator animator = ValueAnimator.ofInt(progress, target);
        animator.setDuration(760);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            progress = (int) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float inset = dp(14);
        arcBounds.set(inset, inset, getWidth() - inset, getHeight() - inset);
        canvas.drawArc(arcBounds, -90, 360, false, trackPaint);
        canvas.drawArc(arcBounds, -90, progress * 3.6f, false, progressPaint);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = getHeight() / 2f - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(progress + "%", getWidth() / 2f, baseline, textPaint);
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
