package io.openim.android.ouicalling;

import android.content.Context;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.service.DegradeService;
import com.yanzhenjie.permission.AndPermission;

import io.openim.android.ouicore.utils.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.sdk.models.SignalingInfo;

@Route(path = Routes.Service.CALLING)
public class CallingServiceImp implements CallingService {
    private static final String TAG = "CallingServiceImp";
    private CallDialog callDialog;
    private Context context;

    @Override
    public void init(Context context) {
        this.context = context;
        callDialog = new CallDialog(context);
    }

    @Override
    public void onInvitationCancelled(SignalingInfo s) {
        L.e(TAG, "----onInvitationCancelled-----");

    }

    @Override
    public void onInvitationTimeout(SignalingInfo s) {
        L.e(TAG, "----onInvitationTimeout-----");
    }

    @Override
    public void onInviteeAccepted(SignalingInfo s) {
        L.e(TAG, "----onInviteeAccepted-----");
    }

    @Override
    public void onInviteeAcceptedByOtherDevice(SignalingInfo s) {
        L.e(TAG, "----onInviteeAcceptedByOtherDevice-----");
    }

    @Override
    public void onInviteeRejected(SignalingInfo s) {
        L.e(TAG, "----onInviteeRejected-----");
    }

    @Override
    public void onInviteeRejectedByOtherDevice(SignalingInfo s) {
        L.e(TAG, "----onInviteeRejectedByOtherDevice-----");
    }

    @Override
    public void onReceiveNewInvitation(SignalingInfo s) {
        L.e(TAG, "----onReceiveNewInvitation-----");
        Common.UIHandler.post(() -> {
            AndPermission.with(context).overlay().onGranted(data -> {
                callDialog.bindData(s);
                callDialog.show();
            }).start();
        });
    }

    @Override
    public void onHangup(SignalingInfo signalingInfo) {
        L.e(TAG, "----onHangup-----");
        Common.UIHandler.post(() -> {
            callDialog.dismiss();
        });
    }
}


