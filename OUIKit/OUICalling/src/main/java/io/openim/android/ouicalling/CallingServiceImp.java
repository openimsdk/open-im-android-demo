package io.openim.android.ouicalling;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.service.DegradeService;
import com.yanzhenjie.permission.AndPermission;

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
    private CallDialog callDialog;

    @Override
    public void init(Context context) {
        this.context = context;
    }

    @Override
    public void onInvitationCancelled(SignalingInfo s) {
        L.e(TAG, "----onInvitationCancelled-----");
        if (null == callDialog) return;
        callDialog.dismiss();
    }

    @Override
    public void onInvitationTimeout(SignalingInfo s) {
        L.e(TAG, "----onInvitationTimeout-----");
    }

    @Override
    public void onInviteeAccepted(SignalingInfo s) {
        L.e(TAG, "----onInviteeAccepted-----");
        if (null != callDialog)
            callDialog.otherSideAccepted();
    }

    @Override
    public void onInviteeAcceptedByOtherDevice(SignalingInfo s) {
        L.e(TAG, "----onInviteeAcceptedByOtherDevice-----");
    }

    @Override
    public void onInviteeRejected(SignalingInfo s) {
        L.e(TAG, "----onInviteeRejected-----");
        callDialog.dismiss();
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
                callDialog = new CallDialog(context, this);
                callDialog.bindData(signalingInfo);
                callDialog.show();
            }).start();
        });
    }

    @Override
    public void onHangup(SignalingInfo signalingInfo) {
        L.e(TAG, "----onHangup-----");
        if (null == callDialog) return;
        Common.UIHandler.post(() -> {
            callDialog.dismiss();
        });
    }


    @Override
    public void call(boolean isVideoCalls, SignalingInfo signalingInfo) {
        callDialog = new CallDialog(context, this, true, signalingInfo);
        callDialog.bindData(signalingInfo);
        callDialog.show();
    }
}


