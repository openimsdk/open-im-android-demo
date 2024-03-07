package io.openim.android.ouicore.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ReplacementSpan;

import androidx.annotation.IntRange;

public class CustomPositionDrawableSpan extends ReplacementSpan {
    private final int offsetX;
    private final int offsetY;
    private final Drawable drawable;

    public CustomPositionDrawableSpan(Drawable drawable,  int offsetX, int offsetY) {
        this.drawable=drawable;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return drawable.getBounds().right + offsetX;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        canvas.save();
        canvas.translate(x + offsetX, y + offsetY);
        drawable.draw(canvas);
        canvas.restore();
    }

}
