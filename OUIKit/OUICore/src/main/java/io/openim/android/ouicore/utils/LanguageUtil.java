package io.openim.android.ouicore.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;


import androidx.annotation.RequiresApi;

import org.intellij.lang.annotations.Language;

import java.util.HashMap;
import java.util.Locale;

import io.openim.android.ouicore.R;

/**
 *多语言适配方案，适配各种版本，核心未替换上下文Context中的Local
 */
public class LanguageUtil {

    private static final String TAG = "LanguageUtil";

    /**
     * 默认支持的语言
     */
    private static final HashMap<String, Locale> supportLanguage = new HashMap<String, Locale>(4) {{
        put(Locale.CHINA.getLanguage(), Locale.CHINA);
        put(Locale.ENGLISH.getLanguage(), Locale.ENGLISH);
    }};

    /**
     * 应用多语言切换，重写BaseActivity中的attachBaseContext即可
     * 采用本地SP存储的语言
     *
     * @param context 上下文
     * @return context
     */
    public static Context attachBaseContext(Context context) {
        String language=SharedPreferencesUtil.get(context).getString(Constant.K_LANGUAGE_SP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return createConfigurationContext(context, language);
        } else {
            return updateConfiguration(context, language);
        }
    }

    /**
     * 应用多语言切换，重写BaseActivity中的attachBaseContext即可
     *
     * @param context  上下文
     * @param language 语言
     * @return context
     */
    public static Context attachBaseContext(Context context, String language) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return createConfigurationContext(context, language);
        } else {
            return updateConfiguration(context, language);
        }
    }

    /**
     * 获取Local,根据language
     *
     * @param language 语言
     * @return Locale
     */
    private static Locale getLanguageLocale(String language) {
        if (supportLanguage.containsKey(language)) {
            return supportLanguage.get(language);
        } else {
            Locale systemLocal = getSystemLocal();
            for (String languageKey : supportLanguage.keySet()) {
                if (TextUtils.equals(supportLanguage.get(languageKey).getLanguage(),
                    systemLocal.getLanguage())) {
                    return systemLocal;
                }
            }
        }
        return Locale.CHINA;
    }

    /**
     * 获取当前的Local，默认中文
     *
     * @param context context
     * @return Locale
     */
    public static Locale getCurrentLocale(Context context) {
        String language=SharedPreferencesUtil.get(context).getString(Constant.K_LANGUAGE_SP);
        if (supportLanguage.containsKey(language)) {
            return supportLanguage.get(language);
        } else {
            Locale systemLocal = getSystemLocal();
            for (String languageKey : supportLanguage.keySet()) {
                if (TextUtils.equals(supportLanguage.get(languageKey)
                    .getLanguage(), systemLocal.getLanguage())) {
                    return systemLocal;
                }
            }
        }
        return Locale.CHINA;
    }

    /**
     * 获取系统的Local
     *
     * @return Locale
     */
    private static Locale getSystemLocal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            return Locale.getDefault();
        }
    }

    /**
     * Android 7.1 以下通过 updateConfiguration
     *
     * @param context  context
     * @param language 语言
     * @return Context
     */
    private static Context updateConfiguration(Context context, String language) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getLanguageLocale(language);
        L.e(TAG, "updateLocalApiLow==== " + locale.getLanguage());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // apply locale
            configuration.setLocales(new LocaleList(locale));
        } else {
            // updateConfiguration
            configuration.locale = locale;
            DisplayMetrics dm = resources.getDisplayMetrics();
            resources.updateConfiguration(configuration, dm);
        }
        return context;
    }

    /**
     * Android 7.1以上通过createConfigurationContext
     * N增加了通过config.setLocales去修改多语言
     *
     * @param context  上下文
     * @param language 语言
     * @return context
     */
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private static Context createConfigurationContext(Context context, String language) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getLanguageLocale(language);
        L.d(TAG, "current Language locale = " + locale);
        LocaleList localeList = new LocaleList(locale);
        configuration.setLocales(localeList);
        return context.createConfigurationContext(configuration);
    }

    /**
     * 切换语言
     *
     * @param language 语言
     * @param activity 当前界面
     * @param cls      跳转的界面
     */
    public static void switchLanguage(String language, Activity activity, Class<?> cls) {
        switchLanguage(language, activity, cls,null);
    }

    /**
     * 切换语言，携带传递数据
     *
     * @param language 语言
     * @param activity 当前界面
     * @param cls      跳转的界面
     */
    public static void switchLanguage(String language, Activity activity, Class<?> cls, Bundle bundle) {
        SharedPreferencesUtil.get(activity).setCache(Constant.K_LANGUAGE_SP,language);
        Intent intent = new Intent(activity, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
    }

    /**
     * 获取新语言的 Context,修复了androidx.appCompact 1.2.0的问题
     *
     * @param newBase newBase
     * @return Context
     */
    public static Context getNewLocalContext(Context newBase) {
        try {
            // 多语言适配
            Context context = LanguageUtil.attachBaseContext(newBase);
            // 兼容appcompat 1.2.0后切换语言失效问题
            final Configuration configuration = context.getResources().getConfiguration();
            return new ContextThemeWrapper(context, androidx.appcompat.R.style.Theme_AppCompat_Empty) {
                @Override
                public void applyOverrideConfiguration(Configuration overrideConfiguration) {
                    if (overrideConfiguration != null) {
                        overrideConfiguration.setTo(configuration);
                    }
                    super.applyOverrideConfiguration(overrideConfiguration);
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newBase;
    }

    /**
     *  更新Application的Resource local，应用不重启的情况才调用，因为部分会用到application中的context
     *  切记不能走新api createConfigurationContext，亲测
     * @param context context
     * @param newLanguage newLanguage
     */
    public static void updateApplicationLocale(Context context, String newLanguage) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getLanguageLocale(newLanguage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // apply locale
            configuration.setLocales(new LocaleList(locale));
        } else {
            configuration.setLocale(locale);
        }
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);
    }
}
