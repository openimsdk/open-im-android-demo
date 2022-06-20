package io.openim.android.ouicore.utils;


import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * 去重点击
 */
public abstract class OnDedrepClickListener implements View.OnClickListener {
    private long mLastClickTime;
    private long timeInterval = 700;

    @Override
    public void onClick(View v) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - mLastClickTime < timeInterval)
            return;
        mLastClickTime = nowTime;
        click(v);
    }

    abstract void click(View v);
}
