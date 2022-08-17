package io.openim.android.ouicalling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouicore.utils.Routes;

public class JumpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        ARouter.getInstance().build(Routes.Main.SPLASH).navigation();
    }
}
