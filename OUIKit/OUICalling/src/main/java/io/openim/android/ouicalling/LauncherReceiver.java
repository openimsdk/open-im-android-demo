package io.openim.android.ouicalling;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import io.openim.android.ouicalling.service.AudioVideoService;
import io.openim.android.ouicore.utils.L;
import io.openim.keepalive.Alive;


public class LauncherReceiver extends BroadcastReceiver {
    public  static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            L.e(AudioVideoService.TAG, "-------检测到开机-------");
            Alive.restart(context);
        }
    }

}
