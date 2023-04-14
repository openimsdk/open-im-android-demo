package io.openim.android.ouicore.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.utils.L;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseApp extends Application {

    private static BaseApp instance;
    public Realm realm;
    private int mActivityCount;

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
//        activityLifecycleCallback();
    }

    private void activityLifecycleCallback() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                mActivityCount++;
                L.e("---------mActivityCount ++-----------"+mActivityCount);
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
                L.e("---------mActivityCount-----------"+mActivityCount);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    public boolean isBackground() {
        return mActivityCount == 0;
    }


}
