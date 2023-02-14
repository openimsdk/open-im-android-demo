package io.openim.android.ouicore.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import java.lang.ref.SoftReference;

/**
 * 沉侵式状态栏
 */
public class SinkHelper {
    private static SinkHelper sinkHelper = null;
    private static SoftReference<Activity> soft;

    public static SinkHelper get(Activity activity) {
        if (sinkHelper == null) sinkHelper = new SinkHelper();
        soft = new SoftReference<>(activity);
        return sinkHelper;
    }

    /***
     * 开启透明效果(沉侵式)
     *
     * @param view 可传空 为空表示没有title 可现实图片或视频沉侵式效果
     */
    @SuppressLint("InlinedApi")
    public void setTranslucentStatus(View view) {
        // 透明状态栏 在xml 中设置android:statusBarColor transparent
//        soft.get().getWindow().addFlags(
//            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // 透明导航栏 在某些使用屏幕按钮的手机上，可能会影响操作
//            soft.get(). getWindow().addFlags(
//            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        if (view != null) {
            int statusBarHeight = getStatusBarHeight(soft.get().getBaseContext());
            view.setPadding(0, statusBarHeight, 0, 0);
        }
    }

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
            "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
