package io.openim.android.ouicore.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.openim.android.ouicore.utils.Common;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private float mDividerHeight = 1; //线的高度
    private Paint mPaint;           //画笔将自己做出来的分割线矩形画出颜色
    private float top = 0,  left = 0, right = 0;        //左右偏移量
    private List<Integer> notDrawIndex = new ArrayList<>();

    public SpacesItemDecoration() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);          //抗锯齿
        mPaint.setColor(Color.GRAY);        //默认颜色
        notDrawIndex.add(0); //第一个ItemView不需要绘制
    }

    //设置上下左右偏移
    public SpacesItemDecoration setMargin(float top,  float left, float right) {
        this.top = Common.dp2px(top);
        this.left = Common.dp2px(left);
        this.right = Common.dp2px(right);
        return this;
    }

    //设置颜色
    public SpacesItemDecoration setColor(int color) {
        mPaint.setColor(color);
        return this;
    }

    //设置分割线高度
    public SpacesItemDecoration setDividerHeight(float height) {
        this.mDividerHeight = height;
        return this;
    }

    //在这里就已经把宽度的偏移给做好了
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        //第一个ItemView不需要在上面绘制分割线
        if (parent.getChildAdapterPosition(view) != 0) {

            outRect.top = (int) mDividerHeight;//指相对itemView顶部的偏移量
        }
    }

    public void addNotDrawIndex(Integer... index) {
        notDrawIndex.addAll(Arrays.asList(index));
    }

    //这里主要是绘制颜色的
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        int childCount = parent.getChildCount();
//因为getItemOffsets是针对每一个ItemView，而onDraw方法是针对RecyclerView本身，所以需要循环遍历来设置
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            int index = parent.getChildAdapterPosition(view);

            //第一个ItemView不需要绘制
            if (notDrawIndex.contains(index)) {
                continue;//跳过本次循环体中尚未执行的语句，立即进行下一次的循环条件判断
            }
            float dividerTop = view.getTop() - mDividerHeight-top;
            float dividerLeft = parent.getPaddingLeft() + left;
            float dividerBottom = view.getTop()-top;
            float dividerRight = parent.getWidth() - parent.getPaddingRight() + right;
            c.drawRect(dividerLeft, dividerTop, dividerRight, dividerBottom, mPaint);
        }
    }
}
