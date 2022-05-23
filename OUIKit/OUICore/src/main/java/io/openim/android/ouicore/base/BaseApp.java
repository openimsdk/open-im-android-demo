package io.openim.android.ouicore.base;

import android.app.Application;

public class BaseApp extends Application {

    private static BaseApp instance;

    public static BaseApp instance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
