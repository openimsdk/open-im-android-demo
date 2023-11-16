package io.openim.android.ouicore.services;


import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.alibaba.android.arouter.facade.template.IProvider;

import io.openim.android.sdk.listener.OnSignalingListener;
import io.openim.android.sdk.models.SignalingInfo;

public interface CallingService extends IProvider, OnSignalingListener {

    Dialog buildCallDialog(DialogInterface.OnDismissListener dismissListener,
                           boolean isCallOut);

    default Dialog buildCallDialog(Context context,
                                   DialogInterface.OnDismissListener dismissListener,
                                   boolean isCallOut) {
       throw new RuntimeException();
    }

    /**
     * 呼叫
     */
    void call(SignalingInfo signalingInfo);

    /**
     * 加入
     */
    void join(SignalingInfo signalingInfo);

    void initKeepAlive(String precessName);

    void startAudioVideoService(Context base);

    void stopAudioVideoService(Context base);

    void setOnServicePriorLoginCallBack(OnServicePriorLoginCallBack onServicePriorLoginCallBack);

    OnServicePriorLoginCallBack getOnServicePriorLoginCallBack();

    interface OnServicePriorLoginCallBack {
        void onLogin();
    }

}
