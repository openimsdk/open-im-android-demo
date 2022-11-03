package io.openim.android.demo.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityServerConfigBinding;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;

public class ServerConfigActivity extends BaseActivity<BaseViewModel, ActivityServerConfigBinding> {

    private final ServerConfigVM serverConfigVM = new ServerConfigVM();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityServerConfigBinding.inflate(getLayoutInflater()));
        sink();
        view.setServerConfigVM(serverConfigVM);
        view.restart.setOnClickListener(v -> {
            if (!serverConfigVM.IM_API_URL.getValue().equals(Constant.getImApiUrl()))
                SharedPreferencesUtil.get(BaseApp.inst()).setCache("IM_API_URL", serverConfigVM.IM_API_URL.getValue());
            if (!serverConfigVM.APP_AUTH_URL.getValue().equals(Constant.getAppAuthUrl()))
                SharedPreferencesUtil.get(BaseApp.inst()).setCache("APP_AUTH_URL", serverConfigVM.APP_AUTH_URL.getValue());
            if (!serverConfigVM.IM_WS_URL.getValue().equals(Constant.getImWsUrl()))
                SharedPreferencesUtil.get(BaseApp.inst()).setCache("IM_WS_URL", serverConfigVM.IM_WS_URL.getValue());
            if (!serverConfigVM.STORAGE_TYPE.getValue().equals(Constant.getStorageType()))
                SharedPreferencesUtil.get(BaseApp.inst()).setCache("STORAGE_TYPE", serverConfigVM.STORAGE_TYPE.getValue());
            Common.UIHandler.postDelayed(this::restart, 1000);
        });
    }

    private void restart() {
        System.exit(0);
    }

    public static class ServerConfigVM {
        public MutableLiveData<String> IM_API_URL = new MutableLiveData<>(Constant.getImApiUrl());
        public MutableLiveData<String> APP_AUTH_URL = new MutableLiveData<>(Constant.getAppAuthUrl());
        public MutableLiveData<String> IM_WS_URL = new MutableLiveData<>(Constant.getImWsUrl());
        public MutableLiveData<String> STORAGE_TYPE = new MutableLiveData<>(Constant.getStorageType());
    }
}
