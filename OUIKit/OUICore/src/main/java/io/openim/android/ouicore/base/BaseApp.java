package io.openim.android.ouicore.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.LanguageUtil;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseApp extends Application {

    private static BaseApp instance;
    public Realm realm;
    public State<Boolean> isAppBackground = new State<>(true);
    private int mActivityCount;

    public static BaseApp inst() {
        return instance;
    }

    public LoginCertificate loginCertificate;

    public <T extends BaseVM> T getVMByCache(Class<T> vm) {
        try {
            return Easy.find(vm);
        } catch (Exception ignored) {
        }
        return null;
    }

    public <T extends BaseViewModel> void putVM(T vm) {
        Easy.put(vm);
    }

    public void removeCacheVM(Class<? extends BaseViewModel> cl) {
        try {
            BaseViewModel vm = Easy.find(cl);
            vm.releaseRes();
        } catch (Exception ignored) {}
        Easy.delete(cl);
    }

    private void realmInit() {
        Realm.init(this);
        String realmName = "open_im_db";
        RealmConfiguration config = new RealmConfiguration.Builder().name(realmName).build();
        realm = Realm.getInstance(config);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        realmInit();
        activityLifecycleCallback();
    }

    private void activityLifecycleCallback() {
//        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
//            @Override
//            public void onResume(@NonNull LifecycleOwner owner) {
//                isAppBackground.setValue(false);
//            }
//
//            @Override
//            public void onStop(@NonNull LifecycleOwner owner) {
//                isAppBackground.setValue(true);
//            }
//        });

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                mActivityCount++;
                if (isAppBackground.val())
                    isAppBackground.setValue(false);
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActivityCount--;
                if (mActivityCount == 0) {
                    isAppBackground.setValue(true);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LanguageUtil.attachBaseContext(base));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LanguageUtil.attachBaseContext(this);
    }

}
