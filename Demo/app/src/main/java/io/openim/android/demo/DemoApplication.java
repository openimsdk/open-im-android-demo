package io.openim.android.demo;

import android.app.ActivityManager;
import android.content.Context;

import androidx.multidex.MultiDex;


import com.alibaba.android.arouter.launcher.ARouter;

import java.util.List;

import io.openim.android.demo.utils.Constant;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.im.IM;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.net.RXRetrofit.HttpConfig;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;


public class DemoApplication extends BaseApp {
    private static final String TAG = BaseApp.class.getSimpleName();


    @Override
    public void onCreate() {
        super.onCreate();
        if (!isMainProcess()) return;

        MultiDex.install(this);
        //im 初始化
        IM.initSdk(this);

        //ARouter init
        ARouter.openLog();
        ARouter.openDebug();
        ARouter.init(this);
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
