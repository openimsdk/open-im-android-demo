package io.openim.android.demo.vm;

import android.view.View;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.launcher.ARouter;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.demo.repository.OpenIMService;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.api.NiService;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.NotificationVM;
import io.openim.android.ouicore.vm.UserLogic;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConnListener;
import io.openim.android.sdk.listener.OnConversationListener;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.UserInfo;
import open_im_sdk.Open_im_sdk;

public class MainVM extends BaseViewModel<LoginVM.ViewAction> implements OnConnListener,
    OnConversationListener {

    public MutableLiveData<Integer> visibility = new MutableLiveData<>(View.INVISIBLE);
    public boolean fromLogin;
    private CallingService callingService;
    public State<Integer> totalUnreadMsgCount = new State<>();
    private final UserLogic userLogic = Easy.find(UserLogic.class);

    @Override
    protected void viewCreate() {
        IMEvent.getInstance().addConnListener(this);
        IMEvent.getInstance().addConversationListener(this);

        callingService =
            (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
        if (null != callingService) callingService.setOnServicePriorLoginCallBack(this::initDate);

        if (fromLogin) {
            initDate();
        } else {
            WaitDialog waitDialog = new WaitDialog(context.get());
            waitDialog.setNotDismiss();
            userLogic.loginStatus.observe((LifecycleOwner) context.get(), loginStatus -> {
                if (loginStatus == UserLogic.LoginStatus.LOGGING) {
                    waitDialog.show();
                }
                if (loginStatus == UserLogic.LoginStatus.SUCCESS) {
                    initDate();
                    waitDialog.dismiss();
                }
                if (loginStatus == UserLogic.LoginStatus.FAIL) {
                    waitDialog.dismiss();
                    getIView().toast(loginStatus.value);
                    getIView().jump();
                }
            });
        }

    }

    private void initDate() {
        BaseApp.inst().loginCertificate = LoginCertificate.getCache(context.get());
        CrashReport.setDeviceId(context.get(), BaseApp.inst().loginCertificate.userID);

        initGlobalVM();

        if (null != callingService)
            callingService.startAudioVideoService(getContext());

        getIView().initDate();
        getSelfUserInfo();
        onConnectSuccess();

        getClientConfig();
    }

    private void initGlobalVM() {
        Easy.installVM(NotificationVM.class);
    }

    private void getClientConfig() {
        N.API(NiService.class).CommNI(Constant.getAppAuthUrl() + "client_config/get",
            BaseApp.inst().loginCertificate.chatToken,
            NiService.buildParameter().buildJsonBody()).compose(N.IOMain()).map(OneselfService.turn(Map.class)).subscribe(new NetObserver<Map>(getContext()) {
            @Override
            public void onSuccess(Map m) {
                try {
                    HashMap<String, Object> map = (HashMap) m.get("config");
                    int allowSendMsgNotFriend = Integer.parseInt((String) map.get("allowSendMsgNotFriend"));
                    BaseApp.inst().loginCertificate.allowSendMsgNotFriend = allowSendMsgNotFriend == 1;
                    BaseApp.inst().loginCertificate.cache(BaseApp.inst());
                    userLogic.discoverPageURL.setValue((String) map.get("discoverPageURL"));

                } catch (Exception ignored) {}
            }

            @Override
            protected void onFailure(Throwable e) {
                toast(e.getMessage());
            }
        });
    }

    public void updateConfig(UserInfo userInfo) {
        LoginCertificate certificate = BaseApp.inst().loginCertificate;
        if (!userInfo.getUserID().equals(certificate.userID)) return;

        certificate.nickname = userInfo.getNickname();
        certificate.faceURL = userInfo.getFaceURL();

        certificate.globalRecvMsgOpt = userInfo.getGlobalRecvMsgOpt();
        certificate.allowAddFriend = userInfo.getAllowAddFriend() == 1;
        certificate.allowBeep = userInfo.getAllowBeep() == 1;
        certificate.allowVibration = userInfo.getAllowVibration() == 1;

        BaseApp.inst().loginCertificate.cache(BaseApp.inst());
    }

    void getSelfUserInfo() {
        List<String> ids = new ArrayList<>();
        ids.add(BaseApp.inst().loginCertificate.userID);
        Parameter parameter = new Parameter().add("userIDs", ids);
        N.API(OneselfService.class).getUsersFullInfo(parameter.buildJsonBody())
            .map(OpenIMService.turn(HashMap.class))
            .compose(N.IOMain())
            .subscribe(new NetObserver<HashMap>(getContext()) {
                @Override
                protected void onFailure(Throwable e) {
                    toast(e.getMessage());
                }

                @Override
                public void onSuccess(HashMap map) {
                    try {
                        List arrayList = (List) map.get("users");
                        if (null == arrayList || arrayList.isEmpty()) return;

                        UserInfo us = GsonHel.getGson().fromJson(arrayList.get(0).toString(),
                            UserInfo.class);
                        updateConfig(us);
                        userLogic.info.setValue(us);
                        Obs.newMessage(Constant.Event.USER_INFO_UPDATE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
    }

    @Override
    protected void releaseRes() {
        IMEvent.getInstance().removeConnListener(this);
    }

    @Override
    public void onConnectFailed(long code, String error) {
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.CONNECT_ERR);
    }

    @Override
    public void onConnectSuccess() {
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.DEFAULT);
        visibility.setValue(View.VISIBLE);
    }

    @Override
    public void onConnecting() {
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.CONNECTING);
    }

    @Override
    public void onKickedOffline() {

    }

    @Override
    public void onUserTokenExpired() {

    }

    @Override
    public void onConversationChanged(List<ConversationInfo> list) {

    }

    @Override
    public void onNewConversation(List<ConversationInfo> list) {

    }

    @Override
    public void onSyncServerFailed() {
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.SYNC_ERR);
    }

    @Override
    public void onSyncServerFinish() {
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.DEFAULT);
    }

    @Override
    public void onSyncServerStart() {
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.SYNCING);
    }

    @Override
    public void onTotalUnreadMessageCountChanged(int i) {
        totalUnreadMsgCount.setValue(i);
    }
}
