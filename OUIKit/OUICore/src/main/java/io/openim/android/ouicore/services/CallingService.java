package io.openim.android.ouicore.services;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.alibaba.android.arouter.facade.template.IProvider;

import io.openim.android.sdk.models.SignalingInfo;

public interface CallingService extends IProvider {

    default Dialog buildCallDialog(Context context,
                                   DialogInterface.OnDismissListener dismissListener,
                                   boolean isCallOut) {
        throw new RuntimeException();
    }

    default void call(SignalingInfo signalingInfo) {
        throw new RuntimeException();
    }

    default void setOnServicePriorLoginCallBack(OnServicePriorLoginCallBack onServicePriorLoginCallBack) {
        throw new RuntimeException();
    }

    default void onInvitationCancelled(SignalingInfo s) {
        throw new RuntimeException();
    }

    default void onInvitationTimeout(SignalingInfo s) {
        throw new RuntimeException();
    }

    default void onInviteeAccepted(SignalingInfo s) {
        throw new RuntimeException();
    }

    default void onInviteeAcceptedByOtherDevice(SignalingInfo s) {
        throw new RuntimeException();
    }

    default void onInviteeRejected(SignalingInfo s) {
        throw new RuntimeException();
    }

    default void onInviteeRejectedByOtherDevice(SignalingInfo s) {
        throw new RuntimeException();
    }

    default void onReceiveNewInvitation(SignalingInfo s) {
        throw new RuntimeException();
    }

    default void onHangup(SignalingInfo s) {
        throw new RuntimeException();
    }

    OnServicePriorLoginCallBack getOnServicePriorLoginCallBack();

    interface OnServicePriorLoginCallBack {
        void onLogin();
    }
}
