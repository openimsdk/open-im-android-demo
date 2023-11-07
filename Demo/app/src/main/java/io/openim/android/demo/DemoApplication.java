package io.openim.android.demo;

import android.content.Intent;

import androidx.multidex.MultiDex;


import com.alibaba.android.arouter.launcher.ARouter;
import com.igexin.sdk.IUserLoggerInterface;
import com.igexin.sdk.PushManager;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import java.io.IOException;
import java.util.List;

import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IM;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.net.RXRetrofit.HttpConfig;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.LanguageUtil;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.UserLogic;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.sdk.listener.OnConnListener;
import io.realm.Realm;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class DemoApplication extends BaseApp {
    private static final String TAG = BaseApp.class.getSimpleName();


    @Override
    public void onCreate() {
        L.e("App", "-----onCreate");
        super.onCreate();
        initController();

        MultiDex.install(this);
        //ARouter init
        ARouter.init(this);
//        ARouter.openimLog();
//        ARouter.openDebug();

        initPush();
        //net init
        initNet();

        //im 初始化
        initIM();

        //音频播放
        SPlayer.init(this);
        SPlayer.instance().setCacheDirPath(Constant.AUDIO_DIR);

        EmojiManager.install(new GoogleEmojiProvider());
    }


    private void initController() {
        Easy.installVM(UserLogic.class);
    }


    private void initPush() {
        PushManager.getInstance().initialize(this);
        PushManager.getInstance().setDebugLogger(this, s -> L.i("getui", s));
    }

    private void initNet() {
        N.init(new HttpConfig().setBaseUrl(Constant.getAppAuthUrl())
            .addInterceptor(chain -> {
                String token = "";
                try {
                    token = BaseApp.inst().loginCertificate.chatToken;
                } catch (Exception ignored) {
                }
                return chain.proceed(chain.request().newBuilder().addHeader("token",
                    token).addHeader("operationID",
                    System.currentTimeMillis() + "").build());
            }));
    }

    private void initIM() {
        IM.initSdk(this);
        listenerIMOffline();
    }

    private void listenerIMOffline() {
        IMEvent.getInstance().addConnListener(new OnConnListener() {
            @Override
            public void onConnectFailed(long code, String error) {

            }

            @Override
            public void onConnectSuccess() {

            }

            @Override
            public void onConnecting() {

            }

            @Override
            public void onKickedOffline() {
                offline();
            }

            @Override
            public void onUserTokenExpired() {
                offline();
            }


        });
    }
    public void offline() {
        LoginCertificate.clear();
        CallingService callingService = (CallingService) ARouter.getInstance()
            .build(Routes.Service.CALLING).navigation();
        if (null != callingService)
            callingService.stopAudioVideoService(BaseApp.inst());

        ActivityManager.finishAllExceptActivity();
        startActivity(new Intent(BaseApp.inst(), LoginActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
