package io.openim.android.ouicore.base;

import android.app.Application;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaseApp extends Application {
    public static final HashMap<String,BaseViewModel> viewModels = new HashMap<>();

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
