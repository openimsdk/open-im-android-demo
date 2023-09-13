package io.openim.android.ouicore.utils;

import android.app.Activity;

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
        if (!excepts.isEmpty())
            activityStack.addAll(excepts);
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

    public static Stack<Activity> getActivityStack() {
        return activityStack;
    }
}
