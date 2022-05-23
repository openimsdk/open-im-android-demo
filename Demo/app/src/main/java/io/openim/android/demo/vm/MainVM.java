package io.openim.android.demo.vm;

import android.view.View;

import androidx.lifecycle.MutableLiveData;

import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConnListener;
import io.openim.android.sdk.models.UserInfo;

public class MainVM extends BaseViewModel<LoginVM.ViewAction> implements OnConnListener {

    public MutableLiveData<String> nickname = new MutableLiveData<>("");
    public MutableLiveData<Integer> visibility = new MutableLiveData<>(View.INVISIBLE);
    private LoginCertificate loginCertificate;

    @Override
    protected void viewCreate() {
        IMEvent.getInstance().addConnListener(this);

        loginCertificate = LoginCertificate.getCache(context);
        OpenIMClient.getInstance().login(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                    IView.loginErr(error);
            }

            @Override
            public void onSuccess(String data) {
                L.e("user_token:"+loginCertificate.token);
                IView.initDate();
                OpenIMClient.getInstance().userInfoManager.getSelfUserInfo(new OnBase<UserInfo>() {
                    @Override
                    public void onError(int code, String error) {

                    }

                    @Override
                    public void onSuccess(UserInfo data) {
                        // 返回当前登录用户的资料
                        loginCertificate.nickname = data.getNickname();
                        loginCertificate.cache(context);
                        nickname.setValue(loginCertificate.nickname);
                    }
                });
            }
        }, loginCertificate.userID, loginCertificate.token);

        if (null != loginCertificate.nickname)
            nickname.setValue(loginCertificate.nickname);
    }

    @Override
    protected void viewDestroy() {
        IMEvent.getInstance().removeConnListener(this);
    }

    @Override
    public void onConnectFailed(long code, String error) {

    }

    @Override
    public void onConnectSuccess() {
        visibility.setValue(View.VISIBLE);
    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onKickedOffline() {

    }

    @Override
    public void onUserTokenExpired() {

    }
}
