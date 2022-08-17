package io.openim.android.demo.vm;

import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConnListener;
import io.openim.android.sdk.models.UserInfo;

public class MainVM extends BaseViewModel<LoginVM.ViewAction> implements OnConnListener {

    private static final String TAG = "MainVM";
    public MutableLiveData<String> nickname = new MutableLiveData<>("");
    public MutableLiveData<Integer> visibility = new MutableLiveData<>(View.INVISIBLE);
    public boolean fromLogin, isInitDate;
    private CallingService callingService;

    @Override
    protected void viewCreate() {
        IMEvent.getInstance().addConnListener(this);

        callingService= (CallingService) ARouter.getInstance()
            .build(Routes.Service.CALLING).navigation();
        callingService.setOnServicePriorLoginCallBack(this::initDate);

        BaseApp.inst().loginCertificate = LoginCertificate.getCache(getContext());
        long status = OpenIMClient.getInstance().getLoginStatus();
        L.e(TAG, "login status-----[" + status + "]");
        if (fromLogin || status == 101) {
            initDate();
        }
        if (!IMUtil.isLogged()) {
            OpenIMClient.getInstance().login(new OnBase<String>() {
                @Override
                public void onError(int code, String error) {
                    IView.toast(error + code);
                    IView.jump();
                }

                @Override
                public void onSuccess(String data) {
                    L.e(TAG, "login -----onSuccess");
                    L.e(TAG, "user_token:" + BaseApp.inst().loginCertificate.token);
                    initDate();
                }
            }, BaseApp.inst().loginCertificate.userID, BaseApp.inst().loginCertificate.token);
        }
        if (null != BaseApp.inst().loginCertificate.nickname)
            nickname.setValue(BaseApp.inst().loginCertificate.nickname);
    }

    private void initDate() {
        if (isInitDate) return;
        isInitDate = true;
        callingService.startAudioVideoService(getContext());

        IView.initDate();
        getSelfUserInfo();
        onConnectSuccess();
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
