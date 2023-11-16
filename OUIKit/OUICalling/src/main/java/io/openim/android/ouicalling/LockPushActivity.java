package io.openim.android.ouicalling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hjq.permissions.Permission;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.NotificationUtil;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.models.SignalingInfo;

/**
 * 锁屏弹出
 */
public class LockPushActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD //解锁
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON //保持屏幕不息屏
            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);//点亮屏幕
       Common.addTypeSystemAlert(getWindow().getAttributes());
        if (Build.VERSION.SDK_INT > 27) {
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
        NotificationUtil.cancelNotify(CallingServiceImp.A_NOTIFY_ID);

        CallingService callingService =
            (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();

        callingService.buildCallDialog(this, dialog -> {
            finish();
            overridePendingTransition(0, 0);
            if (Common.isScreenLocked()) {
                Common.UIHandler.postDelayed(() -> ARouter.getInstance()
                    .build(Routes.Main.HOME).navigation(), 300);
            }
        }, false).show();

        super.onCreate(savedInstanceState);
    }


}
