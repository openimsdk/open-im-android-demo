package io.openim.android.ouicore.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class ActivityManager {
    private static final Stack<Activity> activityStack = new Stack<>();

    /**
     * 添加Activity到堆栈
     */
    public static void push(Activity activity) {
        activityStack.push(activity);
    }

    public static void remove(Activity activity) {
        activityStack.remove(activity);
    }

    /**
     * 结束指定的Activity
     */
    public static void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public static void finishActivity(Class<?> cls) {
        Iterator<Activity> iterator = activityStack.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            if (activity.getClass().equals(cls)) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
                iterator.remove();
            }
        }
    }


    /**
     * 除传入的外 结束所有Activity
     */
    public static void finishAllExceptActivity(Class<?>... cls) {
        List<Activity> excepts = new ArrayList<>();
        t:
        for (Activity activity : activityStack) {
            if (activity != null) {
                for (Class<?> cl : cls) {
                    if (activity.getClass().equals(cl)) {
                        excepts.add(activity);
                        continue t;
                    }
                }
                activity.finish();
            }
        }
        activityStack.clear();
        if (!excepts.isEmpty()) activityStack.addAll(excepts);
    }

    /**
     * Finish all activities top of this activity include this in app activity's stack
     * @param clz target activities
     */
    public static void finishAllAfterCurrent(Class<?> clz) throws ClassCastException {
        Stack<Activity> newStack = (Stack<Activity>) activityStack.clone();
        for (int i = activityStack.size() - 1; i >= 0; i --) {
            Activity activity = activityStack.get(i);
            if (activity != null) {
                activity.finish();
                newStack.remove(activity);
                if (activity.getClass().equals(clz)) break;
            }
        }
        if (!newStack.isEmpty()) {
            activityStack.clear();
            activityStack.addAll(newStack);
        }
    }

    /**
     * 获取 activityStack 中Activity是否存在且没有Finishing
     *
     * @param cls
     * @return 存在返回Activity 实例，否则返回null
     */
    public static Activity isExist(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls) && !activity.isFinishing()) {
                return activity;
            }
        }
        return null;
    }

    @Nullable
    public static Activity hasSingleInstanceRunning(Context context) {
        try {
            for (Activity activity : activityStack) {
                int currentLaunchMode = context.getPackageManager()
                    .getActivityInfo(activity.getComponentName(), PackageManager.GET_META_DATA).launchMode;
                if (currentLaunchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
                    return activity;
                }
            }
        } catch (Exception e) {}
        return null;
    }

    public static Stack<Activity> getActivityStack() {
        return activityStack;
    }
}
