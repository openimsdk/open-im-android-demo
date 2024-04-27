package io.openim.android.ouicalling;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.NotificationUtil;
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
        Common.addTypeSystemAlert(getWindow().getAttributes());
        if (Build.VERSION.SDK_INT > 27) {
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
        NotificationUtil.cancelNotify(CallingServiceImp.A_NOTIFY_ID);

        CallingService callingService =
            (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();

        Dialog callDialog = callingService.buildCallDialog(this, dialog -> {
            finish();
            overridePendingTransition(0, 0);
            if (Common.isScreenLocked()) {
                Common.UIHandler.postDelayed(() -> ARouter.getInstance()
                    .build(Routes.Main.HOME).navigation(), 300);
            }
        }, false);
        if (null != callDialog)
            callDialog.show();

        super.onCreate(savedInstanceState);
    }


}
