package io.openim.android.ouicore.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import io.openim.android.ouicore.R;

/**
 * 滑动的圆形按钮
 * 类似ios的按钮效果
 * 使用:
 * 直接将SlideButton放在布局里
 * 设置按钮的点击监听接口 setOnSlideButtonClickListener
 * <p>
 * Created by bear on 2017/3/17.
 */

public class SlideButton extends View {
    private static final String TAG = "SlideButton";
    private float mCircleX; //圆钮的圆心x坐标
    private float mCenterX; //控件中心x坐标
    private float mCenterY; //控件中心y坐标
    private float mCircleRadius; //圆的半径

    private float mMargin; //圆钮与背景的外边距
    private int mButtonColor; //圆的颜色
    private int mBgColor; //背景颜色 关闭颜色
    private int mBgColorOff; //背景颜色 关闭颜色
    private int mBgColorOn; //背景颜色  打开颜色
    private int mDuration; //滑动的时间 毫秒
    private float mRadiusPlus; //滑块移动到view的正中心的时候圆钮的宽度增加值,最多为圆钮半径的一半

    private boolean isChecked = false; //按钮的状态是否是打开 ,默认关闭

    private Paint mPaint; //画笔

    private OnSlideButtonClickListener onClickListener; //回调接口

    /**
     * 按钮的点击回调接口
     */
    public interface OnSlideButtonClickListener {
        /**
         * 当用户点击了按钮之后返回按钮的状态
         *
         * @param isChecked true：打开 false：关闭
         */
        void onClicked(boolean isChecked);
    }

    /**
     * 设置点击回调接口
     *
     * @param onClickListener OnSlideButtonClickListener
     */
    public void setOnSlideButtonClickListener(OnSlideButtonClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * 按钮的状态
     *
     * @return true：打开 flase ：关闭
     */
    public boolean isChecked() {
        return isChecked;
    }

    /**
     * 更改按钮的状态
     */
    public void setChecked() {
        setChecked(!isChecked);
    }

    /**
     * 更改按钮的状态
     *
     * @param isChecked 按钮的状态
     */
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;

        if (this.isChecked) {
            mCircleX = mCenterX * 2 - mMargin - mCircleRadius;
        } else {
            mCircleX = mMargin + mCircleRadius;
        }

        if (onClickListener != null)
            onClickListener.onClicked(this.isChecked);

        postInvalidate();
    }

    /**
     * 改变按钮的状态伴随动画
     *
     * @param isChecked 按钮的状态
     */
    public void setCheckedWithAnimation(boolean isChecked) {
        Log.i(TAG, "setCheckedWithAnimation: Checked：" + isChecked);
        this.isChecked = isChecked;
        if (onClickListener != null)
            onClickListener.onClicked(this.isChecked);

        startAnimation(this.isChecked);
    }

    public SlideButton(Context context) {
        this(context, null);
    }

    public SlideButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideButton);
        mMargin = typedArray.getDimension(R.styleable.SlideButton_sMargin,
                dip2px(getContext(), 2));
        mBgColorOn = typedArray.getColor(R.styleable.SlideButton_sBackgroundColorOn,
                Color.GREEN);
        mBgColorOff = typedArray.getColor(R.styleable.SlideButton_sBackgroundColorOff,
                Color.GRAY);
        mButtonColor = typedArray.getColor(R.styleable.SlideButton_sButtonColor,
                Color.WHITE);
        typedArray.recycle();

        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mPaint = new Paint();
        //抗锯齿，平滑处理
        mPaint.setAntiAlias(true);
        //
        mPaint.setStyle(Paint.Style.FILL);

        //动画时间
        mDuration = 200;

        //背景默认颜色
        mBgColor = mBgColorOff;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /**
         * 计算中心的的坐标
         */
        mCenterX = w / 2.0f;
        mCenterY = h / 2.0f;

        //圆钮的半径取小值 需要减去外边距
        mCircleRadius = Math.min(mCenterX, mCenterY) - mMargin;
        //圆心的x坐标
        mCircleX = mMargin + mCircleRadius;
    }

    /**
     * 绘制圆角矩形
     *
     * @param canvas 画布
     * @param l      左边界
     * @param t      顶部
     * @param r      右边界
     * @param b      底部
     * @param radius 圆角半径
     * @param color  填充的颜色
     */
    private void drawRoundRect(Canvas canvas, float l, float t, float r, float b, float radius, int color) {
        RectF rectF = new RectF(l, t, r, b);
        drawRoundRect(canvas, rectF, radius, color);
    }

    /**
     * 绘制圆角矩形
     *
     * @param canvas 画布
     * @param rectF  绘制的区域
     * @param radius 圆角半径
     * @param color  填充的颜色
     */
    private void drawRoundRect(Canvas canvas, RectF rectF, float radius, int color) {
        mPaint.setColor(color);
        canvas.drawRoundRect(rectF, radius, radius, mPaint);
    }

    /**
     * 画圆钮
     */
    private void drawCircle(Canvas canvas) {

        //圆心与view中心的距离
        float distance = Math.abs(mCircleX - mCenterX);
        /**
         * 如果圆心与view的中心的距离小于圆的半径的一半
         * 则对中心的圆钮进行加宽
         */
        if (distance < mCircleRadius / 2) {
            //离中心点越近，增加的宽度越多
            mRadiusPlus = mCircleRadius / 2 - distance;
        } else
            mRadiusPlus = 0;
        //圆角矩形的区域
        RectF rectF = new RectF(mCircleX - mCircleRadius - mRadiusPlus, mCenterY - mCircleRadius,
                mCircleX + mCircleRadius + mRadiusPlus, mCenterY + mCircleRadius);

        //绘制
        drawRoundRect(canvas, rectF, mCircleRadius, mButtonColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         * 绘制圆角背景
         */
        if (mCircleX > mCenterX)
            mBgColor = mBgColorOn;
        else mBgColor = mBgColorOff;
        drawRoundRect(canvas, 0, 0, getWidth(), getHeight(), getHeight() / 2, mBgColor);

        /**
         * 绘制圆钮
         */
        //圆钮的圆心不能超过边界
        mCircleX = Math.min(mCircleX, mCenterX * 2 - mMargin - mCircleRadius);
        mCircleX = Math.max(mCircleX, mMargin + mCircleRadius);
        //画圆钮
        drawCircle(canvas);
    }

    /**
     * 移动圆钮的位置
     *
     * @param moveX 手指的x坐标
     */
    private void moveCircle(float moveX) {
        mCircleX = moveX;

        //刷新view
        postInvalidate();
    }

    /**
     * 开始运行动画；
     *
     * @param isButtonOn 按钮状态是否是打开 true：打开 false ：关
     */
    private void startAnimation(boolean isButtonOn) {
        float endX;
        if (isButtonOn) {
            endX = mCenterX * 2 - mMargin - mCircleRadius;
        } else {
            endX = mMargin + mCircleRadius;
        }

        // 轻触动画
        clearAnimation();
        //设置动画
        //"xsx"仅仅是随意定义的一个名字，我们的要的是它的更新接口返回值
        @SuppressLint("ObjectAnimatorBinding") ObjectAnimator animator = ObjectAnimator.ofFloat(this, "xsx", mCircleX, endX);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(mDuration);
        animator.start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCircleX = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
    }

    private float mDownX; //手指按下的坐标
    private boolean canMove = true; //是否可以滑动

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                /**
                 * 手指按下的时候在圆钮上则可以滑动，否则不可以
                 */
                if (mDownX >= mCircleX - mCircleRadius - mMargin * 2 &&
                        mDownX <= mCircleX + mCircleRadius + mMargin * 2)
                    canMove = true;
                else canMove = false;
                break;

            case MotionEvent.ACTION_MOVE:
                /**
                 * 圆钮跟随手指的坐标
                 */
                if (canMove)
                    moveCircle(event.getX());
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                float upX = event.getX();

                /**
                 * 轻触按钮切换状态
                 */
                if (Math.abs(upX - mDownX) < 5)
                    setCheckedWithAnimation(!isChecked);
                else {
                    /**
                     * 手指抬起的时候在按钮里面则可以进行按钮的状态改变等操作，否则取消刚才的动作
                     */
                    //如果圆心的x坐标超过view一半则属于打开状态
                    if (mCircleX > mCenterX)
                        isChecked = true;
                    else isChecked = false;

                    //开始动画
                    setCheckedWithAnimation(isChecked);
                }
                break;
        }
        return true;
    }

    /**
     * dp转px
     *
     * @param context
     * @param dip
     * @return
     */
    public int dip2px(Context context, float dip) {
        int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
        return value;
    }
}
