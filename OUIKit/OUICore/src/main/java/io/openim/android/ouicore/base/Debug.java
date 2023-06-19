package io.openim.android.ouicore.base;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IM;
import io.openim.android.ouicore.net.RXRetrofit.HttpConfig;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;

public class Debug extends BaseApp {
    @Override
    public void onCreate() {
        super.onCreate();

        BaseApp.inst().loginCertificate = new LoginCertificate();
        BaseApp.inst().loginCertificate.imToken ="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJVSUQiOiI1OTAxNTAzNjciLCJQbGF0Zm9ybSI6IkFuZHJvaWQiLCJleHAiOjE2ODYwMzc5OTIsIm5iZiI6MTY3ODI2MTY5MiwiaWF0IjoxNjc4MjYxOTkyfQ.Kj81DFlvuhJP4CMxiTvFSGytSCSUPDjeO1Za8uQvRzU";
        BaseApp.inst().loginCertificate.chatToken="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJVc2VySUQiOiI1OTAxNTAzNjciLCJVc2VyVHlwZSI6MCwiZXhwIjoxNjg2MDM3OTkyLCJuYmYiOjE2NzgyNjE2OTIsImlhdCI6MTY3ODI2MTk5Mn0.hHAJGYsbd6q2O15c0ftsqIzWHCMr6f4Z63ePTRoj-5c";
        BaseApp.inst().loginCertificate.nickname = "Oliver";
        BaseApp.inst().loginCertificate.userID = "590150367";
        BaseApp.inst().loginCertificate.faceURL = "http://img.touxiangwu" +
            ".com/zb_users/upload/2022/11/202211071667789271294192.jpg";

        N.init(new HttpConfig().setBaseUrl(Constant.getAppAuthUrl())
            .addInterceptor(chain -> {
                String token = "";
                try {
                    token = BaseApp.inst().loginCertificate.chatToken;
                } catch (Exception ignored) {
                }
                return chain.proceed(chain.request().newBuilder().addHeader("token",
                    token).build());
            }));

        IM.initSdk(this);
        OpenIMClient.getInstance().login(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
            }

            @Override
            public void onSuccess(String data) {
            }
        }, BaseApp.inst().loginCertificate.userID, BaseApp.inst().loginCertificate.imToken);

        //音频播放
        SPlayer.init(this);
        SPlayer.instance().setCacheDirPath(Constant.AUDIO_DIR);
    }
}
