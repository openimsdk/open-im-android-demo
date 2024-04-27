package io.openim.android.ouicore.im;


import android.app.Application;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.InitConfig;

public class IM {
    public static void initSdk(Application app) {
        L.e("App", "---IM--initSdk");
        InitConfig initConfig = new InitConfig(Constants.getImApiUrl(),
            Constants.getImWsUrl(), getStorageDir());
        initConfig.isLogStandardOutput = true;
        initConfig.logLevel=5;

        ///IM 初始化
        OpenIMClient.getInstance().initSDK(app,
            initConfig, IMEvent.getInstance().connListener);

        IMEvent.getInstance().init();
    }

    //存储路径
    public static String getStorageDir() {
        return BaseApp.inst().getFilesDir().getAbsolutePath();
    }
}
