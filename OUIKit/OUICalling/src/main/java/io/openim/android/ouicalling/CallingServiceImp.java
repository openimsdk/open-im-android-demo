package io.openim.android.ouicalling;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.service.DegradeService;
import com.yanzhenjie.permission.AndPermission;

import io.openim.android.ouicalling.resident.Leoric;
import io.openim.android.ouicalling.resident.LeoricConfigs;
import io.openim.android.ouicore.utils.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.SignalingCertificate;
import io.openim.android.sdk.models.SignalingInfo;

@Route(path = Routes.Service.CALLING)
public class CallingServiceImp implements CallingService {
    public static final String TAG = "CallingServiceImp";
    private Context context;
    public CallDialog callDialog;
    private Class ClickNotificationToPage;

    public void setClickNotificationToPage(Activity clickNotificationToPage) {
        ClickNotificationToPage = clickNotificationToPage.getClass();

        Leoric.init(context, new LeoricConfigs(
            new LeoricConfigs.LeoricConfig(
                context.getPackageName() + ":resident",
                AudioVideoDaemonService.class.getCanonicalName()),
            new LeoricConfigs.LeoricConfig(
                "openIM.AudioVideoService",
                AudioVideoService.class.getCanonicalName())));

        clickNotificationToPage.startService(new Intent(clickNotificationToPage, AudioVideoService.class));
    }

    @Override
    public Class getClickNotificationToPage() {
        return ClickNotificationToPage;
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceiveNewInvitation(SignalingInfo signalingInfo) {
        L.e(TAG, "----onReceiveNewInvitation-----");
        Common.UIHandler.post(() -> {
            AndPermission.with(context).overlay().onGranted(data -> {
                if (callDialog != null) return;
                if (signalingInfo.getInvitation().getInviteeUserIDList().size() > 1)
                    callDialog = new GroupCallDialog(context, this);
                else
                    callDialog = new CallDialog(context, this);
                callDialog.bindData(signalingInfo);
                callDialog.show();
            }).start();
        });
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
        if (signalingInfo.getInvitation().getInviteeUserIDList().size() > 1)
            return;
        else
            callDialog = new CallDialog(context, this, true);
        callDialog.bindData(signalingInfo);
        callDialog.show();
    }
}


