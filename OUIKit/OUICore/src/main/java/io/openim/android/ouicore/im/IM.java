package io.openim.android.ouicore.im;


import android.content.Context;

import io.openim.android.ouicore.net.RXRetrofit.HttpConfig;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.sdk.OpenIMClient;

public class IM {
    public static void initSdk(Context context) {
        String storageDir = context.getFilesDir().getAbsolutePath();

        N.init(new HttpConfig().setBaseUrl(Constant.APP_AUTH_URL));
        ///IM 初始化
        OpenIMClient.getInstance().initSDK(Constant.IM_API_URL, Constant.IM_WS_URL, storageDir, 6, "cos",
                IMEvent.getInstance().connListener);
        IMEvent.getInstance().init();
    }
}
