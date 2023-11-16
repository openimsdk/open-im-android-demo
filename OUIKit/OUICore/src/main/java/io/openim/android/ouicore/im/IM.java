package io.openim.android.ouicore.im;


import android.app.Application;
import android.content.Intent;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.net.RXRetrofit.HttpConfig;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnConnListener;
import io.openim.android.sdk.models.InitConfig;

public class IM {
    public static void initSdk(Application app) {
        L.e("App", "---IM--initSdk");
        InitConfig initConfig = new InitConfig(Constant.getImApiUrl(),
            Constant.getImWsUrl(), getStorageDir());
        initConfig.isLogStandardOutput = false;
        initConfig.logLevel=0;

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
