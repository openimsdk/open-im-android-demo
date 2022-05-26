package io.openim.android.ouiconversation.utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;

public class FloatBtnUtil {
 
    private static  int height = 0;
    private Activity mcontext;
    private ViewTreeObserver.OnGlobalLayoutListener listener;
    private View root;
    private ViewTreeObserver.OnGlobalLayoutListener mListener;
    private int distanceY;
    private ViewTreeObserver mTreeObserver;
    private ValueAnimator mValueAnimator;
 
    public FloatBtnUtil(Activity mcontext){
        this.mcontext = mcontext;
        if (height == 0){
            Display defaultDisplay = mcontext.getWindowManager().getDefaultDisplay();
            Point point = new Point();
            defaultDisplay.getSize(point);
            height = point.y;
        }
    }
 
    /**
     * @param root 视图根节点
     * @param floatview 需要显示在键盘上的View组件
     */
    public void setFloatView(View root,View floatview){
        this.root = root;	
        listener = () -> {
            Rect r = new Rect();
            mcontext.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
            int heightDifference = height - (r.bottom - r.top);
            boolean isKeyboardShowing = heightDifference > height / 3;
            if(isKeyboardShowing){
                floatview.setVisibility(View.VISIBLE);
                root.scrollTo(0, heightDifference+floatview.getHeight());
            }else{
                root.scrollTo(0, 0);
                floatview.setVisibility(View.GONE);
            }
        };
        root.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }
 
    public void clearFloatView(){
        if (listener != null && root != null) {
            root.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }
 
}