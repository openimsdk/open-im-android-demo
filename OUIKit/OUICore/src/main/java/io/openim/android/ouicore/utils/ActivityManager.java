package io.openim.android.ouicore.utils;

import android.app.Activity;

import java.util.ArrayList;
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
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 除传入的外 结束所有Activity
     */
    public static void finishAllExceptActivity(Class<?>... cls) {
        List<Activity> excepts = new ArrayList<>();
        t:for (Activity activity : activityStack) {
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
}
