package io.openim.android.ouicalling;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.yanzhenjie.permission.AndPermission;

import io.openim.android.ouicalling.service.AudioVideoService;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.sdk.models.SignalingInfo;

@Route(path = Routes.Service.CALLING)
public class CallingServiceImp implements CallingService {
    private OnServicePriorLoginCallBack onServicePriorLoginCallBack;
    public static final String TAG = "CallingServiceImp";
    private Context context;
    public CallDialog callDialog;
    private SignalingInfo signalingInfo;

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void startAudioVideoService(Context base) {
        L.e(TAG, "-----initBackstageAudioVideoService");
        base.startService(new Intent(base, AudioVideoService.class));
    }

    @Override
    public void stopAudioVideoService(Context base) {
        Intent stopIntent = new Intent(base, AudioVideoService.class);
        base.stopService(stopIntent);
    }


    @Override
    public void setOnServicePriorLoginCallBack(OnServicePriorLoginCallBack onServicePriorLoginCallBack) {
        this.onServicePriorLoginCallBack = onServicePriorLoginCallBack;
    }

    @Override
    public OnServicePriorLoginCallBack getOnServicePriorLoginCallBack() {
        return onServicePriorLoginCallBack;
    }

    public static void launchAlarm(Context context) {
        // alarm唤醒
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            return;
        }
        long INTERVAL_WAKEUP_MS = 10 * 1000; // 10 seconds
        long triggerAtTime = SystemClock.elapsedRealtime() + INTERVAL_WAKEUP_MS;
        Intent i = new Intent(context, AudioVideoService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_MUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        } else {
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }
    }


    @Override
    public void init(Context context) {
        this.context = context;
    }

    @Override
    public void onInvitationCancelled(SignalingInfo s) {
        L.e(TAG, "----onInvitationCancelled-----");
        Common.UIHandler.post(() -> {
            if (null == callDialog) return;
            callDialog.dismiss();
        });
    }

    @Override
    public void onInvitationTimeout(SignalingInfo s) {
        L.e(TAG, "----onInvitationTimeout-----");
    }

    @Override
    public void onInviteeAccepted(SignalingInfo s) {
        L.e(TAG, "----onInviteeAccepted-----");
        Common.UIHandler.post(() -> {
            if (null == callDialog) return;
            callDialog.otherSideAccepted();
        });
    }

    @Override
    public void onInviteeAcceptedByOtherDevice(SignalingInfo s) {
        L.e(TAG, "----onInviteeAcceptedByOtherDevice-----");
    }

    @Override
    public void onInviteeRejected(SignalingInfo s) {
        L.e(TAG, "----onInviteeRejected-----");
        Common.UIHandler.post(() -> {
            if (null == callDialog) return;
            callDialog.dismiss();
        });
    }

    @Override
    public void onInviteeRejectedByOtherDevice(SignalingInfo s) {
        L.e(TAG, "----onInviteeRejectedByOtherDevice-----");
    }

    @Override
    public void onReceiveNewInvitation(SignalingInfo signalingInfo) {
        L.e(TAG, "----onReceiveNewInvitation-----");
        this.signalingInfo = signalingInfo;
        Common.UIHandler.post(() -> {
            AndPermission.with(context).overlay().onGranted(data -> {
                context.startActivity(new Intent(context, LockPushActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }).start();
        });
    }

    @Override
    public void showCalling(DialogInterface.OnDismissListener dismissListener,
                            boolean isCallOut) {
        if (callDialog != null) return;
        if (signalingInfo.getInvitation().getInviteeUserIDList().size() > 1)
            callDialog = new GroupCallDialog(context, this, isCallOut);
        else
            callDialog = new CallDialog(context, this, isCallOut);
        callDialog.bindData(signalingInfo);
        if (!callDialog.callingVM.isCallOut) {
            callDialog.setOnDismissListener(dismissListener);
            if (!Common.isScreenLocked()) {
                callDialog.setOnShowListener(dialog ->
                    ARouter.getInstance().build(Routes.Main.HOME).navigation());
            }
        }
        callDialog.show();
    }

    @Override
    public void onHangup(SignalingInfo signalingInfo) {
        L.e(TAG, "----onHangup-----");
        Common.UIHandler.post(() -> {
            if (null == callDialog) return;
            callDialog.dismiss();
        });
    }


    @Override
    public void call(SignalingInfo signalingInfo) {
        this.signalingInfo = signalingInfo;
        showCalling(null, true);
    }

}


