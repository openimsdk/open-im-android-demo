

package io.openim.android.ouicalling.resident;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import io.openim.android.ouicore.utils.L;
import me.weishu.reflection.Reflection;


public class Leoric {

    private static final String TAG = "Leoric";

    private LeoricConfigs mConfigurations;

    private Leoric(LeoricConfigs configurations) {
        this.mConfigurations = configurations;
    }

    public static void init(Context base, LeoricConfigs configurations) {
        Reflection.unseal(base);
        Leoric client = new Leoric(configurations);
        client.initDaemon(base);
    }


    private final String DAEMON_PERMITTING_SP_FILENAME = "d_permit";
    private final String DAEMON_PERMITTING_SP_KEY = "permitted";


    private static  BufferedReader mBufferedReader;

    private void initDaemon(Context base) {
        if (!isDaemonPermitting(base) || mConfigurations == null) {
            return;
        }
        String processName = getProcessName();
        String packageName = base.getPackageName();

        if (processName.startsWith(mConfigurations.PERSISTENT_CONFIG.processName)) {
            ILeoricProcess.Fetcher.fetchStrategy().onPersistentCreate(base, mConfigurations);
        } else if (processName.startsWith(mConfigurations.DAEMON_ASSISTANT_CONFIG.processName)) {
            ILeoricProcess.Fetcher.fetchStrategy().onDaemonAssistantCreate(base, mConfigurations);
        } else if (processName.startsWith(packageName)) {
            ILeoricProcess.Fetcher.fetchStrategy().onInit(base);
        }
        releaseIO();
    }


    public static  String getProcessName() {
        try {
            File file = new File("/proc/self/cmdline");
            mBufferedReader = new BufferedReader(new FileReader(file));
            return mBufferedReader.readLine().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void releaseIO() {
        if (mBufferedReader != null) {
            try {
                mBufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mBufferedReader = null;
        }
    }

    private boolean isDaemonPermitting(Context context) {
        SharedPreferences sp = context.getSharedPreferences(DAEMON_PERMITTING_SP_FILENAME, Context.MODE_PRIVATE);
        return sp.getBoolean(DAEMON_PERMITTING_SP_KEY, true);
    }

    protected boolean setDaemonPermiiting(Context context, boolean isPermitting) {
        SharedPreferences sp = context.getSharedPreferences(DAEMON_PERMITTING_SP_FILENAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(DAEMON_PERMITTING_SP_KEY, isPermitting);
        return editor.commit();
    }

}
