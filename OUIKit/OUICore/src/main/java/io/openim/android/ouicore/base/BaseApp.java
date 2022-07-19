package io.openim.android.ouicore.base;

import android.app.Application;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.openim.android.ouicore.entity.LoginCertificate;

public class BaseApp extends Application {

    private static BaseApp instance;

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

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
