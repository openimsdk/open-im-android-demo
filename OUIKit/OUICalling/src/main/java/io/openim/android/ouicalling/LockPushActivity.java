package io.openim.android.ouicalling;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.WindowManager;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouicalling.databinding.DialogGroupCallBinding;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;

/**
 * 锁屏弹出
 */
public class LockPushActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD //解锁
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON //保持屏幕不息屏
            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);//点亮屏幕
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().addFlags(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        if (Build.VERSION.SDK_INT > 27) {
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
        CallingService callingService = (CallingService) ARouter.getInstance()
            .build(Routes.Service.CALLING).navigation();

        callingService.showCalling(dialog -> {
            finish();
            overridePendingTransition(0, 0);
            boolean lock = Common.isScreenLocked();
            if (lock)
                Common.UIHandler.postDelayed(() -> ARouter.getInstance().build(Routes.Main.HOME).navigation(), 300);
        }, false);
        super.onCreate(savedInstanceState);
    }


}
