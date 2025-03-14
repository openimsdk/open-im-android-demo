package io.openim.android.ouicore.widget.zoom;

import android.content.Context;
import android.view.GestureDetector;

/**
 * Description :滑动手势的管理类
 */

class ScrollGestureManager extends GestureDetector {

    ScrollGestureManager(Context context, ScrollGestureListener scrollGestureListener) {
        super(context, scrollGestureListener);
    }

}
