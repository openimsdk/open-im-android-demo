package io.openim.android.ouicore.widget.zoom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * Description :手势管理类
 */

public class GestureViewManager {

    private ScaleGestureManager scaleGestureManager;
    private ScrollGestureManager scrollGestureManager;
    private ScaleGestureListener scaleGestureListener;
    private ScrollGestureListener scrollGestureListener;
    private View targetView;
    private ViewGroup viewGroup;
    private boolean isScaleEnd = true;

    private OnScaleListener onScaleListener;

    private boolean isFullGroup = false;

    public static GestureViewManager bind(Context context, ViewGroup viewGroup, View targetView) {
        return new GestureViewManager(context, viewGroup, targetView);
    }

    private GestureViewManager(Context context, ViewGroup viewGroup, View targetView) {
        this.targetView = targetView;
        this.viewGroup = viewGroup;
        scaleGestureListener = new ScaleGestureListener(targetView, viewGroup);
        scrollGestureListener = new ScrollGestureListener(targetView, viewGroup);
        scaleGestureManager = new ScaleGestureManager(context, scaleGestureListener);
        scrollGestureManager = new ScrollGestureManager(context, scrollGestureListener);
        targetView.setClickable(false);
        viewGroup.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() == 1 && isScaleEnd) {
                    return scrollGestureManager.onTouchEvent(event);
                } else if (event.getPointerCount() == 2 || !isScaleEnd) {
                    isScaleEnd = event.getAction() == MotionEvent.ACTION_UP;
                    if (isScaleEnd) {
                        scaleGestureListener.onActionUp();
                    }
                    scrollGestureListener.setScale(scaleGestureListener.getScale());
                    if (onScaleListener != null) {
                        onScaleListener.onScale(scaleGestureListener.getScale());
                    }
                    return scaleGestureManager.onTouchEvent(event);
                }
                return false;
            }
        });
    }

    private void fullGroup() {
        targetView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        targetView.getViewTreeObserver().removeOnPreDrawListener(this);
                        float viewWidth = targetView.getWidth();
                        float viewHeight = targetView.getHeight();
                        float groupWidth = viewGroup.getWidth();
                        float groupHeight = viewGroup.getHeight();
                        ViewGroup.LayoutParams layoutParams = targetView.getLayoutParams();
                        float widthFactor = groupWidth / viewWidth;
                        float heightFactor = groupHeight / viewHeight;
                        if (viewWidth < groupWidth && widthFactor * viewHeight <= groupHeight) {
                            layoutParams.width = (int) groupWidth;
                            layoutParams.height = (int) (widthFactor * viewHeight);
                        } else if (viewHeight < groupHeight && heightFactor * viewWidth <= groupWidth) {
                            layoutParams.height = (int) groupHeight;
                            layoutParams.width = (int) (heightFactor * viewWidth);
                        }
                        targetView.setLayoutParams(layoutParams);
                        return true;
                    }
                });
    }

    public boolean isFullGroup() {
        return isFullGroup;
    }

    public void setFullGroup(boolean fullGroup) {
        isFullGroup = fullGroup;
        scaleGestureListener.setFullGroup(fullGroup);
        scrollGestureListener.setFullGroup(fullGroup);
        fullGroup();
    }

    public void setOnScaleListener(OnScaleListener onScaleListener) {
        this.onScaleListener = onScaleListener;
    }

    public interface OnScaleListener {
        void onScale(float scale);
    }
}
