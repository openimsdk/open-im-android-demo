package io.openim.android.ouicore.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * SharedPreferencesUtil
 */
public class SharedPreferencesUtil {
    private static SharedPreferencesUtil sharePrifaceUtil = null;
    private SharedPreferences sharedPreference = null;
    private SharedPreferences.Editor editor = null; // 创建一个接口对象

    @SuppressLint("CommitPrefEdits")
    private SharedPreferencesUtil(Context context) {
        /***
         * 传入上下文获取SharedPreferences对象的实例 参数分别为存储的文件名和存储模式
         */
        sharedPreference = context.getSharedPreferences("sharedPreference", Activity.MODE_PRIVATE);
        editor = sharedPreference.edit();// 获取接口对象的实例
    }

    /**
     * 获得实例
     *
     * @param context
     * @return
     */
    public synchronized static SharedPreferencesUtil get(Context context) {
        if (sharePrifaceUtil == null) {
            sharePrifaceUtil = new SharedPreferencesUtil(context);
        }
        return sharePrifaceUtil;
    }

    /**
     * 存放数据 instanceof判断其左边对象是否为其右边类的实例，返回boolean类型的数据。
     *
     * @param key
     * @param value
     */
    public void setCache(String key, Object value) {
        if (value instanceof String) {
            editor.putString(key, String.valueOf(value));
        } else if (value instanceof Integer) {
            editor.putInt(key, Integer.parseInt(value.toString()));
        } else if (value instanceof Float) {
            editor.putFloat(key, Float.parseFloat(value.toString()));
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, Boolean.parseBoolean(value.toString()));
        } else if (value instanceof Long) {
            editor.putLong(key, Long.parseLong(value.toString()));
        }
        SharedPreferencesCompat.apply(editor); // 执行
    }

    public String getString(String key) {
        return sharedPreference.getString(key, "");
    }

    public int getInteger(String key) {
        return sharedPreference.getInt(key, 0);
    }

    public boolean getBoolean(String key) {
        return sharedPreference.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreference.getBoolean(key, defValue);
    }

    public long getLong(String key) {
        try {
            return sharedPreference.getLong(key, 0);
        } catch (ClassCastException e) {
            // In previous versions values might be stored as Integer
            return sharedPreference.getInt(key, 0);
        }
    }

    public float getFloat(String key) {
        return sharedPreference.getFloat(key, 0);
    }

    public boolean isEmpty(String key) {
        return sharedPreference.contains(key);
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferencesCompat.apply(get(context).editor.remove(key));
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferencesCompat.apply(get(context).editor.clear());
    }

    /**
     * 返回所有的键值对
     *
     * @param context
     * @return
     */
    @SuppressWarnings("static-access")
    public static Map<String, ?> getAll(Context context) {
        return get(context).getAll(context);
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     */
    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException | InvocationTargetException |
                     IllegalAccessException e) {
                e.printStackTrace();
            }
            editor.commit();
        }
    }
}
