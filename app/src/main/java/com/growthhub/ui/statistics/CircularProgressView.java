package com.growthhub.ui.statistics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.growthhub.R;

public class CircularProgressView extends View {
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private int progress;

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(dp(10));
        trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.stat_glass_strong));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(dp(10));
        progressPaint.setColor(ContextCompat.getColor(getContext(), R.color.stat_accent_green));

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.stat_text_primary));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(22));
        textPaint.setFakeBoldText(true);
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float inset = dp(12);
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
