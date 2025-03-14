package io.openim.android.ouicore.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.ColorInt;

import java.lang.ref.SoftReference;

import io.openim.android.ouicore.base.BaseApp;

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
//         透明状态栏 在xml 中设置android:statusBarColor transparent
//        soft.get().getWindow().addFlags(
//            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

//         透明导航栏 在某些使用屏幕按钮的手机上，可能会影响操作
//        soft.get().getWindow().addFlags(
//            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        if (view != null) {
            int statusBarHeight = getStatusBarHeight();
            view.setPadding(0, statusBarHeight, 0, 0);
        }
    }


    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    public static int getStatusBarHeight() {
        Resources resources = BaseApp.inst().getResources();
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen",
            "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取底部navigationBar高度
     *
     * @return
     */
    public static int getNavigationBarHeight() {
        Resources resources = BaseApp.inst().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public static void setSystemBarTintDrawable(Activity activity, Drawable tintDrawable) {
        SystemBarUtil mTintManager = new SystemBarUtil(activity);
        if (tintDrawable != null) {
            mTintManager.setStatusBarTintEnabled(true);
            mTintManager.setTintDrawable(tintDrawable);
        } else {
            mTintManager.setStatusBarTintEnabled(false);
            mTintManager.setTintDrawable(null);
        }
    }
    private static void setStatusBarColor(Activity activity,@ColorInt int color) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //注意要清除 FLAG_TRANSLUCENT_STATUS flag
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        activity. getWindow().setStatusBarColor(color);
    }

}
