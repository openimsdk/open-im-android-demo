package io.openim.android.demo.vm;

import android.view.View;

import androidx.lifecycle.MutableLiveData;

import io.openim.android.ouicore.base.BaseApp;
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
    public boolean fromLogin;

    @Override
    protected void viewCreate() {
        IMEvent.getInstance().addConnListener(this);
        BaseApp.inst().loginCertificate = LoginCertificate.getCache(getContext());
        if (fromLogin) {
            IView.initDate();
            getSelfUserInfo();
            onConnectSuccess();
        } else {
            OpenIMClient.getInstance().login(new OnBase<String>() {
                @Override
                public void onError(int code, String error) {
                    IView.toast(error + code);
                }

                @Override
                public void onSuccess(String data) {
                    L.e("user_token:" + BaseApp.inst().loginCertificate.token);
                    IView.initDate();
                    getSelfUserInfo();
                }
            }, BaseApp.inst().loginCertificate.userID, BaseApp.inst().loginCertificate.token);

        }
        if (null != BaseApp.inst().loginCertificate.nickname)
            nickname.setValue(BaseApp.inst().loginCertificate.nickname);
    }

    void getSelfUserInfo() {
        OpenIMClient.getInstance().userInfoManager.getSelfUserInfo(new OnBase<UserInfo>() {
            @Override
            public void onError(int code, String error) {
                IView.toast(error + code);
            }

            @Override
            public void onSuccess(UserInfo data) {
                // 返回当前登录用户的资料
                BaseApp.inst().loginCertificate.nickname = data.getNickname();
                BaseApp.inst().loginCertificate.faceURL = data.getFaceURL();
                BaseApp.inst().loginCertificate.cache(getContext());
                nickname.setValue(BaseApp.inst().loginCertificate.nickname);
            }
        });
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
