package io.openim.android.demo.vm;

import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.api.NiService;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.NotificationVM;
import io.openim.android.ouicore.vm.UserLogic;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConnListener;
import io.openim.android.sdk.listener.OnConversationListener;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.UserInfo;

public class MainVM extends BaseViewModel<LoginVM.ViewAction> implements OnConnListener,
    OnConversationListener {

    public MutableLiveData<String> nickname = new MutableLiveData<>("");
    public MutableLiveData<Integer> visibility = new MutableLiveData<>(View.INVISIBLE);
    public boolean fromLogin, isInitDate;
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

        BaseApp.inst().loginCertificate = LoginCertificate.getCache(getContext());
        boolean logged = IMUtil.isLogged();
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
                    initDate();
                }
            }, BaseApp.inst().loginCertificate.userID, BaseApp.inst().loginCertificate.imToken);
        }
        if (null != BaseApp.inst().loginCertificate.nickname)
            nickname.setValue(BaseApp.inst().loginCertificate.nickname);
    }

    private void initDate() {
        if (isInitDate) return;
        isInitDate = true;
        if (null != callingService)
            callingService.startAudioVideoService(getContext());

        initGlobalVM();
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
                   HashMap<String,Object> map = (HashMap) m.get("config");
                    int allowSendMsgNotFriend= Integer.valueOf((String) map.get("allowSendMsgNotFriend"));
                    BaseApp.inst().loginCertificate.allowSendMsgNotFriend = allowSendMsgNotFriend == 1;

                    BaseApp.inst().loginCertificate.cache(BaseApp.inst());
                } catch (Exception ignored) {
                }
            }

            @Override
            protected void onFailure(Throwable e) {
               toast(e.getMessage());
            }
        });
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
                BaseApp.inst().loginCertificate.nickname = (null==data.getNickname())?"":data.getNickname();
                BaseApp.inst().loginCertificate.faceURL = data.getFaceURL();
                BaseApp.inst().loginCertificate.cache(getContext());
                nickname.setValue(BaseApp.inst().loginCertificate.nickname);
                Obs.newMessage(Constant.Event.USER_INFO_UPDATE);
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
