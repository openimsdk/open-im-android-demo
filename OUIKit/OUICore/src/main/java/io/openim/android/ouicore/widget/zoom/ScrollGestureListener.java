package io.openim.android.ouicore.widget.zoom;

import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Description :滑动手势的监听器
 */

public class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener {

    private float scale = 1;
    private View targetView;
    private ViewGroup viewGroup;
    private float distanceXTemp = 0;
    private float distanceYTemp = 0;

    private float viewWidthReal = 0;
    private float viewHeightReal = 0;

    private float viewWidthRealTemp = 0;
    private float viewHeightRealTemp = 0;

    private boolean isCalculate = false;
    private int viewWidthNormal = 0;
    private int viewHeightNormal = 0;
    private int groupWidth = 0;
    private int groupHeight = 0;
    private float maxTranslationLeft = 0;
    private float maxTranslationTop = 0;
    private float maxTranslationRight = 0;
    private float maxTranslationBottom = 0;

    private boolean isFullGroup = false;

    ScrollGestureListener(View targetView, ViewGroup viewGroup) {
        this.targetView = targetView;
        this.viewGroup = viewGroup;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        distanceX = -distanceX;
        distanceY = -distanceY;

        if (isFullGroup || scale > 1) {
            if (viewWidthReal > groupWidth) {
                translationXOnScrollEvent(distanceX);
            }
            if (viewHeightReal > groupHeight) {
                translationYOnScrollEvent(distanceY);
            }
        } else {
            translationXOnScrollEvent(distanceX);
            translationYOnScrollEvent(distanceY);
        }

        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    private void translationXOnScrollEvent(float distanceX) {
        //最大移动距离全部为正数，所以需要通过判断distanceX的正负，来判断是向左移动还是向右移动，
        // 然后通过取distanceX的绝对值来和相应移动方向的最大移动距离比较
        if ((distanceX < 0 && Math.abs(distanceXTemp + distanceX) < maxTranslationLeft)
                || (distanceX > 0 && distanceXTemp + distanceX < maxTranslationRight)) {
            distanceXTemp += distanceX;
            targetView.setTranslationX(distanceXTemp);
            //如果超出边界，就移动到最大距离，防止边界有剩余量
        } else if ((distanceX < 0 && Math.abs(distanceXTemp + distanceX) > maxTranslationLeft)) {
            distanceXTemp = -maxTranslationLeft;
            targetView.setTranslationX(-maxTranslationLeft);
        } else if ((distanceX > 0 && distanceXTemp + distanceX > maxTranslationRight)) {
            distanceXTemp = maxTranslationRight;
            targetView.setTranslationX(maxTranslationRight);
        }
    }

    private void translationYOnScrollEvent(float distanceY) {
        if ((distanceY < 0 && Math.abs(distanceYTemp + distanceY) < maxTranslationTop)
                || (distanceY > 0 && distanceYTemp + distanceY < maxTranslationBottom)) {
            distanceYTemp += distanceY;
            targetView.setTranslationY(distanceYTemp);
            //如果超出边界，就移动到最大距离，防止边界有剩余量
        } else if ((distanceY < 0 && Math.abs(distanceYTemp + distanceY) > maxTranslationTop)) {
            distanceYTemp = -maxTranslationTop;
            targetView.setTranslationY(-maxTranslationTop);
        } else if ((distanceY > 0 && distanceYTemp + distanceY > maxTranslationBottom)) {
            distanceYTemp = maxTranslationBottom;
            targetView.setTranslationY(maxTranslationBottom);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        //计算能移动的最大距离
        if (!isCalculate) {
            isCalculate = true;
            maxTranslationLeft = targetView.getLeft();
            maxTranslationTop = targetView.getTop();
            maxTranslationRight = viewGroup.getWidth() - targetView.getRight();
            maxTranslationBottom = viewGroup.getHeight() - targetView.getBottom();
            viewWidthNormal = targetView.getWidth();
            viewHeightNormal = targetView.getHeight();
            viewWidthRealTemp = viewWidthNormal;
            viewHeightRealTemp = viewHeightNormal;
            viewWidthReal = viewWidthNormal;
            viewHeightReal = viewHeightNormal;
            groupWidth = viewGroup.getWidth();
            groupHeight = viewGroup.getHeight();
        }
        return true;
    }

    void setScale(float scale) {

        viewWidthReal = viewWidthNormal * scale;
        viewHeightReal = viewHeightNormal * scale;
        //如果view比group小
        if (viewWidthReal < groupWidth) {
            if (isFullGroup) {
                distanceXTemp = 0;
                targetView.setTranslationX(0);
            }
            maxTranslationLeft = targetView.getLeft() - (viewWidthReal - viewWidthNormal) / 2;
            maxTranslationRight = (viewGroup.getWidth() - targetView.getRight()) - (viewWidthReal - viewWidthNormal) / 2;
            //如果移动距离超过最大可移动距离
            if (scale > this.scale && distanceXTemp < 0 && -distanceXTemp > maxTranslationLeft) {
                float translate = (viewWidthReal - viewWidthRealTemp) / 2;
                targetView.setTranslationX(targetView.getTranslationX() + translate);
                distanceXTemp = distanceXTemp + translate;
            } else if (scale > this.scale && distanceXTemp > 0 && distanceXTemp > maxTranslationRight) {
                float translate = (viewWidthReal - viewWidthRealTemp) / 2;
                targetView.setTranslationX(targetView.getTranslationX() - translate);
                distanceXTemp = distanceXTemp - translate;
            }

        } else {
            maxTranslationLeft = (viewWidthReal - viewWidthNormal) / 2 - (viewGroup.getWidth() - targetView.getRight());
            maxTranslationRight = (viewWidthReal - viewWidthNormal) / 2 - targetView.getLeft();
            if (scale < this.scale && distanceXTemp < 0 && -distanceXTemp > maxTranslationLeft) {
                float translate = (viewWidthRealTemp - viewWidthReal) / 2;
                targetView.setTranslationX(targetView.getTranslationX() + translate);
                distanceXTemp = distanceXTemp + translate;
            } else if (scale < this.scale && distanceXTemp > 0 && distanceXTemp > maxTranslationRight) {
                float translate = (viewWidthRealTemp - viewWidthReal) / 2;
                targetView.setTranslationX(targetView.getTranslationX() - translate);
                distanceXTemp = distanceXTemp - translate;
            }
        }

        if (viewHeightReal < groupHeight) {
            maxTranslationTop = targetView.getTop() - (viewHeightReal - viewHeightNormal) / 2;
            maxTranslationBottom = (viewGroup.getHeight() - targetView.getBottom()) - (viewHeightReal - viewHeightNormal) / 2;
            if (isFullGroup) {
                distanceYTemp = 0;
                targetView.setTranslationY(0);
            }
            //如果移动距离超过最大可移动距离
            if (scale > this.scale && distanceYTemp < 0 && -distanceYTemp > maxTranslationTop) {
                float translate = (viewHeightReal - viewHeightRealTemp) / 2;
                targetView.setTranslationY(targetView.getTranslationY() + translate);
                distanceYTemp = distanceYTemp + translate;
            } else if (scale > this.scale && distanceYTemp > 0 && distanceYTemp > maxTranslationBottom) {
                float translate = (viewHeightReal - viewHeightRealTemp) / 2;
                targetView.setTranslationY(targetView.getTranslationY() - translate);
                distanceYTemp = distanceYTemp - translate;
            }
        } else {
            maxTranslationTop = (viewHeightReal - viewHeightNormal) / 2 - (viewGroup.getHeight() - targetView.getBottom());
            maxTranslationBottom = (viewHeightReal - viewHeightNormal) / 2 - targetView.getTop();
            if (scale < this.scale && distanceYTemp < 0 && -distanceYTemp > maxTranslationTop) {
                float translate = (viewHeightRealTemp - viewHeightReal) / 2;
                targetView.setTranslationY(targetView.getTranslationY() + translate);
                distanceYTemp = distanceYTemp + translate;
            } else if (scale < this.scale && distanceYTemp > 0 && distanceYTemp > maxTranslationBottom) {
                float translate = (viewHeightRealTemp - viewHeightReal) / 2;
                targetView.setTranslationY(targetView.getTranslationY() - translate);
                distanceYTemp = distanceYTemp - translate;
            }
        }
        viewWidthRealTemp = viewWidthReal;
        viewHeightRealTemp = viewHeightReal;
        this.scale = scale;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        float left = viewWidthReal > groupWidth ? 0 : (targetView.getLeft() - ((viewWidthReal - viewWidthNormal) / 2));
        float top = viewHeightReal > groupHeight ? 0 : (targetView.getTop() - ((viewHeightReal - viewHeightNormal) / 2));
        float right = viewWidthReal > groupWidth ? groupWidth : viewGroup.getWidth() - ((viewGroup.getWidth() - targetView.getRight()) - (viewWidthReal - viewWidthNormal) / 2);
        float bottom = viewHeightReal > groupHeight ? groupHeight : viewGroup.getHeight() - ((viewGroup.getHeight() - targetView.getBottom()) - (viewHeightReal - viewHeightNormal) / 2);
        RectF rectF = new RectF(left, top, right, bottom);
        if (rectF.contains(e.getX(), e.getY())) {
            targetView.performClick();
        }
        return super.onSingleTapUp(e);
    }

    public boolean isFullGroup() {
        return isFullGroup;
    }

    void setFullGroup(boolean fullGroup) {
        isFullGroup = fullGroup;
    }
}
