package io.openim.android.demo;

import android.content.Intent;
import android.content.pm.PackageInfo;

import androidx.multidex.MultiDex;


import com.alibaba.android.arouter.launcher.ARouter;
import com.cretin.www.cretinautoupdatelibrary.model.TypeConfig;
import com.cretin.www.cretinautoupdatelibrary.model.UpdateConfig;
import com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils;
import com.igexin.sdk.PushManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IM;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.net.RXRetrofit.HttpConfig;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.update.OkHttp3Connection;
import io.openim.android.ouicore.update.UpdateApp;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.UserLogic;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.sdk.listener.OnConnListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


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
        initPush();
        initIM();

        EmojiManager.install(new GoogleEmojiProvider());
        //音频播放
        SPlayer.init(this);
        SPlayer.instance().setCacheDirPath(Constant.AUDIO_DIR);
    }

    private void initFile() {
        buildDirectory(Constant.AUDIO_DIR);
        buildDirectory(Constant.VIDEO_DIR);
        buildDirectory(Constant.PICTURE_DIR);
        buildDirectory(Constant.File_DIR);
    }

    private boolean buildDirectory(String path) {
        File file = new File(path);
        if (file.exists())
            return true;
        return file.mkdirs();
    }

    private void initARouter() {
        ARouter.init(this);
//        if (L.isDebug){
//            ARouter.openLog();
//            ARouter.openDebug();
//        }
    }

    private void initBugly() {
        CrashReport.setAppChannel(this,Common.isApkDebug() ? "debug" : "release");
        CrashReport.initCrashReport(getApplicationContext(), "4d365d80d1", L.isDebug);

//        new UpdateApp().init(R.mipmap.ic_launcher).checkUpdate(BaseApp.inst());
    }


    private void initController() {
        Easy.installVM(UserLogic.class);
    }


    private void initPush() {
        PushManager.getInstance().initialize(this);
        PushManager.getInstance().setDebugLogger(this, s -> L.i("getui", s));
    }

    private void initNet() {
        N.init(new HttpConfig().setBaseUrl(Constant.getAppAuthUrl()).addInterceptor(chain -> {
            String token = "";
            try {
                token = BaseApp.inst().loginCertificate.chatToken;
            } catch (Exception ignored) {
            }
            Request request = chain.request().newBuilder().addHeader("token", token).addHeader(
                "operationID", System.currentTimeMillis() + "").build();
            Response response = chain.proceed(request);
            return response;
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
        CallingService callingService =
            (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
        if (null != callingService) callingService.stopAudioVideoService(BaseApp.inst());

        ActivityManager.finishAllExceptActivity();
        startActivity(new Intent(BaseApp.inst(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
