package io.openim.android.ouicore.widget;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IM;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConnListener;

/**
 * 模块调试时使用
 */
public class DebugActivity extends FragmentActivity implements OnBase<String> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IM.initSdk();

        LoginCertificate loginCertificate = new LoginCertificate();
        loginCertificate.userID = "a@qq.com";
        loginCertificate.nickname = "a@qq.com";
        loginCertificate.token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJVSUQiOiJhQHFxLmNvbSIsIlBsYXRmb3JtIjoiQW5kcm9pZCIsImV4cCI6MTk2ODYzOTQxOSwibmJmIjoxNjUzMjc5NDE5LCJpYXQiOjE2NTMyNzk0MTl9.XAfKwQ-KDhLBn96FYgH52-OWEZjN3buCgiLxn6wlAhg";
        loginCertificate.cache(this);
        OpenIMClient.getInstance().login(this, loginCertificate.userID, loginCertificate.token);
    }


    @Override
    public void onError(int code, String error) {
        L.e("登录失败---" + error);

    }

    @Override
    public void onSuccess(String data) {

    }
}
