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
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConnListener;
import io.openim.android.sdk.models.UserInfo;

public class MainVM extends BaseViewModel<LoginVM.ViewAction> implements OnConnListener {

    private static final String TAG = "App";
    public MutableLiveData<String> nickname = new MutableLiveData<>("");
    public MutableLiveData<Integer> visibility = new MutableLiveData<>(View.INVISIBLE);
    public boolean fromLogin, isInitDate;
    private CallingService callingService;

    @Override
    protected void viewCreate() {
        IMEvent.getInstance().addConnListener(this);

        callingService = (CallingService) ARouter.getInstance()
            .build(Routes.Service.CALLING).navigation();
        if (null != callingService)
            callingService.setOnServicePriorLoginCallBack(this::initDate);

        BaseApp.inst().loginCertificate = LoginCertificate.getCache(getContext());
        boolean logged = IMUtil.isLogged("App");
        if (fromLogin || logged) {
            initDate();
        } else {
            OpenIMClient.getInstance().login(new OnBase<String>() {
                @Override
                public void onError(int code, String error) {
                    getIView().toast(error + code);
                    getIView().jump();
                }

                @Override
                public void onSuccess(String data) {
                    L.e(TAG, "login -----onSuccess");
                    initDate();
                }
            }, BaseApp.inst().loginCertificate.userID,
                BaseApp.inst().loginCertificate.imToken);
        }
        if (null != BaseApp.inst().loginCertificate.nickname)
            nickname.setValue(BaseApp.inst().loginCertificate.nickname);
    }

    private void initDate() {
        if (isInitDate) return;
        isInitDate = true;
        if (null != callingService)
            callingService.startAudioVideoService(getContext());

        getIView().initDate();
        getSelfUserInfo();
        onConnectSuccess();
    }

    void getSelfUserInfo() {
        OpenIMClient.getInstance().userInfoManager.getSelfUserInfo(new OnBase<UserInfo>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + code);
            }

            @Override
            public void onSuccess(UserInfo data) {
                // 返回当前登录用户的资料
                BaseApp.inst().loginCertificate.nickname = data.getNickname();
                BaseApp.inst().loginCertificate.faceURL = data.getFaceURL();
                BaseApp.inst().loginCertificate.cache(getContext());
                nickname.setValue(BaseApp.inst().loginCertificate.nickname);
                Obs.newMessage(Constant.Event.USER_INFO_UPDATE);
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
