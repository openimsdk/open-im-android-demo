package io.openim.android.ouicore.utils;


import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.template.IProvider;

import io.openim.android.sdk.listener.OnSignalingListener;
import io.openim.android.sdk.models.SignalingCertificate;
import io.openim.android.sdk.models.SignalingInfo;

public interface CallingService extends IProvider, OnSignalingListener {
    /**
     * 呼叫
     */
    void call(SignalingInfo signalingInfo);
}
