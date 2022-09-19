package io.openim.android.ouicore.base;

import android.app.Application;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.openim.android.ouicore.entity.LoginCertificate;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseApp extends Application {

    private static BaseApp instance;
    public Realm realm;

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
    }
}
