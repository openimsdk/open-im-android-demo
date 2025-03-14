package io.openim.android.demo;

import android.content.Intent;

import androidx.multidex.MultiDex;

import com.alibaba.android.arouter.launcher.ARouter;
import com.tencent.bugly.crashreport.CrashReport;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import java.io.File;

import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.ouicore.BuildConfig;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IM;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.net.RXRetrofit.HttpConfig;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.vm.UserLogic;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.sdk.listener.OnConnListener;
import okhttp3.Request;


public class DemoApplication extends BaseApp {
    private static final String TAG = BaseApp.class.getSimpleName();

    @Override
    public void onCreate() {
        L.e(TAG, "-----onCreate------");
        super.onCreate();
        MultiDex.install(this);

        initFile();
        initARouter();
        initController();
        initNet();
        initBugly();
        initIM();

        EmojiManager.install(new GoogleEmojiProvider());
        //音频播放
        SPlayer.init(this);
        SPlayer.instance().setCacheDirPath(Constants.AUDIO_DIR);
    }

    private void initFile() {
        buildDirectory(Constants.AUDIO_DIR);
        buildDirectory(Constants.VIDEO_DIR);
        buildDirectory(Constants.PICTURE_DIR);
        buildDirectory(Constants.File_DIR);
    }

    private boolean buildDirectory(String path) {
        File file = new File(path);
        if (file.exists())
            return true;
        return file.mkdirs();
    }

    private void initARouter() {
        ARouter.init(this);
//        if (io.openim.android.demo.BuildConfig.DEBUG){
//            ARouter.openLog();
//            ARouter.openDebug();
//        }
    }

    private void initBugly() {
        CrashReport.setAppChannel(this, BuildConfig.DEBUG ? "debug" : "release");
        CrashReport.initCrashReport(getApplicationContext(), "4d365d80d1", L.isDebug);
    }


    private void initController() {
        Easy.installVM(UserLogic.class);
    }

    private void initNet() {
        N.init(new HttpConfig().setBaseUrl(Constants.getAppAuthUrl()).setDebug(BuildConfig.DEBUG)
            .addInterceptor(chain -> {
            String token = "";
            try {
                token = BaseApp.inst().loginCertificate.chatToken;
            } catch (Exception ignored) {}
            Request request = chain.request().newBuilder()
                .addHeader("token", token)
                .addHeader("operationID", String.valueOf(System.currentTimeMillis()))
                .build();
                return chain.proceed(request);
        }));
    }

    private void initIM() {
        IM.initSdk(this);
        listenerIMOffline();
    }

    private void listenerIMOffline() {
        IMEvent.getInstance().addConnListener(new OnConnListener() {
            @Override
            public void onConnectFailed(int code, String error) {

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

            @Override
            public void onUserTokenInvalid(String reason) {
                offline();
            }

        });
    }

    public void offline() {
        LoginCertificate.clear();

        ActivityManager.finishAllExceptActivity();
        startActivity(new Intent(BaseApp.inst(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//        if (dsConnectionStatus)
//            unbindService(serviceConnection);
    }
}
