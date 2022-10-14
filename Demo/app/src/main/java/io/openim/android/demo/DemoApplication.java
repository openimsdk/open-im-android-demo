package io.openim.android.demo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.multidex.MultiDex;


import com.alibaba.android.arouter.launcher.ARouter;

import java.util.List;

import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IM;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.sdk.listener.OnConnListener;
import io.realm.Realm;
import io.realm.RealmConfiguration;


public class DemoApplication extends BaseApp {
    private static final String TAG = BaseApp.class.getSimpleName();
    public Realm realm;

    @Override
    protected void attachBaseContext(Context base) {
        L.e(TAG, "-----attachBaseContext");
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        L.e(TAG, "-----onCreate");
        super.onCreate();
//      if (!isMainProcess()) return;

        MultiDex.install(this);
        //im 初始化
        IM.initSdk();
        listenerIMOffline();
        //音频播放
        SPlayer.init(this);
        SPlayer.instance().setCacheDirPath(Constant.AUDIODIR);
        //ARouter init
        ARouter.init(this);
        ARouter.openLog();
        ARouter.openDebug();
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

            private void offline() {
                LoginCertificate.clear();
                CallingService callingService = (CallingService) ARouter.getInstance()
                    .build(Routes.Service.CALLING).navigation();
                if (null != callingService)
                    callingService.stopAudioVideoService(BaseApp.inst());
                BaseApp.inst().startActivity(new Intent(BaseApp.inst(), LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
    }


    private boolean isMainProcess() {
        ActivityManager am = ((ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE));
        String mainProcessName = this.getPackageName();
        int myPid = android.os.Process.myPid();

        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        if (processInfos == null) {
            L.i(TAG, "isMainProcess get getRunningAppProcesses null");
            List<ActivityManager.RunningServiceInfo> processList = am.getRunningServices(Integer.MAX_VALUE);
            if (processList == null) {
                L.i(TAG, "isMainProcess get getRunningServices null");
                return false;
            }
            for (ActivityManager.RunningServiceInfo rsi : processList) {
                if (rsi.pid == myPid && mainProcessName.equals(rsi.service.getPackageName())) {
                    return true;
                }
            }
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

}
