package io.openim.android.ouicore.services;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.template.IProvider;

import io.openim.android.sdk.listener.OnSignalingListener;
import io.openim.android.sdk.models.SignalingCertificate;
import io.openim.android.sdk.models.SignalingInfo;

public interface CallingService extends IProvider, OnSignalingListener {

    void showCalling(DialogInterface.OnDismissListener dismissListener,
                     boolean isCallOut);

    /**
     * 呼叫
     */
    void call(SignalingInfo signalingInfo);

    void initKeepAlive(String precessName);

    void startAudioVideoService(Context base);

    void stopAudioVideoService(Context base);

    void setOnServicePriorLoginCallBack(OnServicePriorLoginCallBack onServicePriorLoginCallBack);

    OnServicePriorLoginCallBack getOnServicePriorLoginCallBack();

    interface OnServicePriorLoginCallBack {
        void onLogin();
    }

}
