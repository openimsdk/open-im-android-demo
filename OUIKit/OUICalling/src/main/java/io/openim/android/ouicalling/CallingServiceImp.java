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

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicalling.service.AudioVideoService;
import io.openim.android.ouicalling.vm.CallingVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.CallHistory;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.PersonalVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.CustomSignalingInfo;
import io.openim.android.sdk.models.MeetingStreamEvent;
import io.openim.android.sdk.models.RoomCallingInfo;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;
import io.openim.keepalive.Alive;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmResults;

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
        Alive.restart(base);
    }

    @Override
    public void stopAudioVideoService(Context base) {
        Alive.finishService(base);
    }

    @Override
    public void setOnServicePriorLoginCallBack(OnServicePriorLoginCallBack onServicePriorLoginCallBack) {
        this.onServicePriorLoginCallBack = onServicePriorLoginCallBack;
    }

    @Override
    public OnServicePriorLoginCallBack getOnServicePriorLoginCallBack() {
        return onServicePriorLoginCallBack;
    }

    @Override
    public void initKeepAlive(String precessName) {
        Alive.init(context,precessName,AudioVideoService.class);
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
            callDialog.callingVM.renewalDB(signalingInfo,
                callHistory -> callHistory.setSuccess(true));
        });
    }

    @Override
    public void onInviteeAcceptedByOtherDevice(SignalingInfo s) {
        L.e(TAG, "----onInviteeAcceptedByOtherDevice-----");
    }

    @Override
    public void onInviteeRejected(SignalingInfo signalingInfo) {
        L.e(TAG, "----onInviteeRejected-----");
        Common.UIHandler.post(() -> {
            if (null == callDialog) return;
            callDialog.callingVM.renewalDB(signalingInfo,
                callHistory -> callHistory.setSuccess(false));
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
        Common.UIHandler.post(() -> {
            if (callDialog != null) return;
            this.signalingInfo = signalingInfo;
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

        insetDB();
    }

    @Override
    public void onHangup(SignalingInfo signalingInfo) {
        L.e(TAG, "----onHangup-----");
        Common.UIHandler.post(() -> {
            if (null == callDialog) return;
            callDialog.callingVM.renewalDB(signalingInfo,
                callHistory -> callHistory.setDuration(
                    (int) (System.currentTimeMillis() - callHistory.getDate())));
            callDialog.dismiss();
        });
    }

    @Override
    public void onRoomParticipantConnected(RoomCallingInfo s) {
        
    }

    @Override
    public void onRoomParticipantDisconnected(RoomCallingInfo s) {

    }

    @Override
    public void onMeetingStreamChanged(MeetingStreamEvent e) {

    }

    @Override
    public void onReceiveCustomSignal(CustomSignalingInfo s) {

    }


    @Override
    public void call(SignalingInfo signalingInfo) {
        this.signalingInfo = signalingInfo;
        showCalling(null, true);
    }

    private void insetDB() {
        if (callDialog.callingVM.isGroup) return;
        List<String> ids = new ArrayList<>();
        ids.add(callDialog.callingVM.isCallOut ?
            signalingInfo.getInvitation().getInviteeUserIDList().get(0)
            : signalingInfo.getInvitation().getInviterUserID());

        boolean isCallOut=!callDialog.callingVM.isCallOut;
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;
                UserInfo userInfo = data.get(0);

                BaseApp.inst().realm.executeTransactionAsync(realm -> {
                    CallHistory callHistory = new CallHistory(
                        signalingInfo.getInvitation().getRoomID(),
                        userInfo.getUserID(), userInfo.getNickname(), userInfo.getFaceURL(),
                        signalingInfo.getInvitation().getMediaType(), false,isCallOut ,
                        System.currentTimeMillis(), 0
                    );
                    realm.insert(callHistory);
                });
            }
        }, ids);
    }

}


