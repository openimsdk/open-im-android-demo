package io.openim.android.ouicore.widget.zoom;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by 王德惠
 * Description :放大缩放手势的点击器
 */

public class ScaleGestureManager extends ScaleGestureDetector {

    ScaleGestureManager(Context context, ScaleGestureListener scaleGestureListener) {
        super(context, scaleGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

}
