package io.openim.android.ouicalling;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicalling.databinding.DialogCallBinding;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.SignalingCertificate;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;
import okio.Sink;


public class CallDialog extends BaseDialog {

    private Context context;
    private DialogCallBinding view;

    public CallDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
    }

    private void initView() {
        Window window = getWindow();
        view = DialogCallBinding.inflate(getLayoutInflater());
        window.requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(view.getRoot());
        //状态栏透明
        window.setDimAmount(1f);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        window.setBackgroundDrawableResource(android.R.color.transparent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        window.setAttributes(params);
    }

    public void bindData(SignalingInfo signalingInfo) {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(signalingInfo.getInvitation().getInviterUserID());
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                Toast.makeText(context, error + code, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;
                UserInfo userInfo = data.get(0);
                view.avatar.load(userInfo.getFaceURL());
                view.name.setText(userInfo.getNickname());
            }
        }, ids);
        listener(signalingInfo);
    }

    private void listener(SignalingInfo signalingInfo) {
        view.hangUp.setOnClickListener(v -> {
            OpenIMClient.getInstance().signalingManager.signalingHungUp(new OnBase<SignalingCertificate>() {
                @Override
                public void onError(int code, String error) {
                    Toast.makeText(context, error + code, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(SignalingCertificate data) {
                    dismiss();
                }
            }, signalingInfo);

        });
    }

    @Override
    public void show() {
        super.show();
    }
}
