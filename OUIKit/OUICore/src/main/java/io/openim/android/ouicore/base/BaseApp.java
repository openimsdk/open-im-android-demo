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

import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.LanguageUtil;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseApp extends Application {

    private static BaseApp instance;
    public Realm realm;
    private boolean isAppBackground;

    public static BaseApp inst() {
        return instance;
    }

    public LoginCertificate loginCertificate;

    public static final HashMap<String, BaseViewModel> viewModels = new HashMap<>();

    public <T> T getVMByCache(Class<T> vm) {
        String key = vm.getCanonicalName();
        if (BaseApp.viewModels.containsKey(key)) {
            return (T) BaseApp.viewModels.get(key);
        }
        return null;
    }

    public <T extends BaseViewModel> void putVM(T vm) {
        String key = vm.getClass().getCanonicalName();
        if (!BaseApp.viewModels.containsKey(key)) {
            BaseApp.viewModels.put(key, vm);
        }
    }

    public void removeCacheVM(Class<?> cl) {
        String key = cl.getCanonicalName();
        BaseViewModel viewModel = BaseApp.viewModels.get(key);
        if (null != viewModel) {
            viewModel.releaseRes();
            BaseApp.viewModels.remove(key);
        }
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
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onResume(@NonNull LifecycleOwner owner) {
                isAppBackground = false;
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                isAppBackground = true;
            }
        });
    }

    public boolean isBackground() {
        return isAppBackground;
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
