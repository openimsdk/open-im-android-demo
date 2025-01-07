package io.openim.android.demo.vm;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hjq.permissions.Permission;
import com.igexin.sdk.PushManager;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.openim.android.demo.repository.OpenIMService;
import io.openim.android.ouicore.api.NiService;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.NotificationVM;
import io.openim.android.ouicore.vm.UserLogic;
import io.openim.android.ouicore.widget.ProgressDialog;
import io.openim.android.ouicore.widget.UILocker;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConnListener;
import io.openim.android.sdk.listener.OnConversationListener;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.UserInfo;
import timber.log.Timber;

public class MainVM extends BaseViewModel<LoginVM.ViewAction> implements OnConnListener, OnConversationListener {

    public MutableLiveData<Integer> visibility = new MutableLiveData<>(View.INVISIBLE);
    public boolean fromLogin;
    private CallingService callingService;
    public State<Integer> totalUnreadMsgCount = new State<>();
    private final UserLogic userLogic = Easy.find(UserLogic.class);
    private UILocker uiLocker;
    private ProgressDialog progressDialog;

    @Override
    protected void viewCreate() {
        IMEvent.getInstance().addConnListener(this);
        IMEvent.getInstance().addConversationListener(this);
        callingService = (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
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
        uiLocker = new UILocker();

    }

    private void initDate() {
        BaseApp.inst().loginCertificate = LoginCertificate.getCache(context.get());
        CrashReport.setDeviceId(context.get(), BaseApp.inst().loginCertificate.userID);

        initGlobalVM();

        getIView().initDate();
        getSelfUserInfo();
        onConnectSuccess();

        getClientConfig();


        getTotalUnreadMsgCount();
    }

    private void getTotalUnreadMsgCount() {
        OpenIMClient.getInstance().conversationManager.getTotalUnreadMsgCount(new OnBase<String>() {
            @Override
            public void onSuccess(String data) {
                totalUnreadMsgCount.setValue(Integer.valueOf(data));
            }
        });
    }

    private void initGlobalVM() {
        Easy.installVM(NotificationVM.class);
    }

    private void getClientConfig() {
        N.API(NiService.class).CommNI(Constants.getAppAuthUrl() + "client_config/get", BaseApp.inst().loginCertificate.chatToken, NiService.buildParameter().buildJsonBody()).compose(N.IOMain()).map(OneselfService.turn(Map.class)).subscribe(new NetObserver<Map>(getContext()) {
            @Override
            public void onSuccess(Map m) {
                try {
                    HashMap<String, Object> map = (HashMap) m.get("config");
                    BaseApp.inst().loginCertificate.cache(BaseApp.inst());
                    //userLogic.discoverPageURL.setValue((String) map.get("discoverPageURL"));

                } catch (Exception ignored) {
                }
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

        certificate.globalRecvMsgOpt = Objects.requireNonNullElse(userInfo.getGlobalRecvMsgOpt(), 0);
        certificate.allowAddFriend = Objects.requireNonNullElse(userInfo.getAllowAddFriend(), 0) == 1;
        certificate.allowBeep = Objects.requireNonNullElse(userInfo.getAllowBeep(), 0) == 1;
        certificate.allowVibration = Objects.requireNonNullElse(userInfo.getAllowVibration(), 0) == 1;

        BaseApp.inst().loginCertificate.cache(BaseApp.inst());
    }

    /**
     * Prevent the sdk from having no user’s data, just used to notice sdk get user info
     * @param callback request finish
     */
    private void safetyEvent(Consumer<String> callback) {
        OpenIMClient.getInstance().userInfoManager.getSelfUserInfo(new OnBase<UserInfo>() {
            @Override
            public void onError(int code, String error) {
                if (callback != null)
                    callback.accept(error + code);
            }

            @Override
            public void onSuccess(UserInfo data) {
                if (callback != null)
                    callback.accept(null);
            }
        });
    }

    void getSelfUserInfo() {
        List<String> ids = new ArrayList<>();
        ids.add(BaseApp.inst().loginCertificate.userID);
        Parameter parameter = new Parameter().add("userIDs", ids);
        N.API(OneselfService.class).getUsersFullInfo(parameter.buildJsonBody()).map(OpenIMService.turn(HashMap.class)).compose(N.IOMain()).subscribe(new NetObserver<HashMap>(getContext()) {
            @Override
            protected void onFailure(Throwable e) {
                toast(e.getMessage());
            }

            @Override
            public void onSuccess(HashMap map) {
                try {
                    List arrayList = (List) map.get("users");
                    if (null == arrayList || arrayList.isEmpty()) return;

                    UserInfo us = GsonHel.getGson().fromJson(arrayList.get(0).toString(), UserInfo.class);
                    updateConfig(us);
                    userLogic.info.setValue(us);
                    Obs.newMessage(Constants.Event.USER_INFO_UPDATE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void initOfflineNotificationConfig(@NonNull Context context, @NonNull String userId) {
        new HasPermissions(context, Permission.NOTIFICATION_SERVICE).safeGo(() -> {
            PushManager.getInstance().bindAlias(context, userId);
        });
    }

    public void clearOfflineNotificationConfig(@NonNull Context context, @NonNull String userId) {
        new HasPermissions(context, Permission.NOTIFICATION_SERVICE).safeGo(() -> {
            PushManager.getInstance().unBindAlias(context, userId, true);
        });
    }

    private void displaySyncProgressDialog(Context context) {
        try {
            if (context == null) return;
            if (progressDialog == null) progressDialog = new ProgressDialog(context);
            progressDialog.setNotDismiss();
            progressDialog.setInfo(BaseApp.inst().getString(io.openim.android.ouicore.R.string.syncing));
            if (!progressDialog.isShowing()) progressDialog.show();
        } catch (Exception e) {}

    }

    private void setSyncProgress(long progress) {
        try {
            if (progressDialog != null) {
                progressDialog.setInfo(BaseApp.inst().getString(io.openim.android.ouicore.R.string.syncing));
            }
        } catch (Exception e) {}
    }

    private void dismissSyncProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }

    private void lockUI() {
        if (null != uiLocker && null != context.get()) {
            uiLocker.showTransparentDialog(context.get());
        }
    }

    private void unLockUI() {
        if (null != uiLocker) {
            uiLocker.dismissTransparentDialog();
        }
    }

    @Override
    protected void releaseRes() {
        IMEvent.getInstance().removeConnListener(this);
        Easy.delete(NotificationVM.class);
    }

    @Override
    public void onConnectFailed(int code, String error) {
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.CONNECT_ERR);
    }

    @Override
    public void onConnectSuccess() {
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.DEFAULT);
        visibility.setValue(View.VISIBLE);
    }

    @Override
    public void onConnecting() {
        safetyEvent(null);
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.CONNECTING);
    }

    @Override
    public void onKickedOffline() {

    }

    @Override
    public void onUserTokenExpired() {

    }

    @Override
    public void onUserTokenInvalid(String reason) {

    }

    @Override
    public void onConversationChanged(List<ConversationInfo> list) {

    }

    @Override
    public void onNewConversation(List<ConversationInfo> list) {

    }

    @Override
    public void onSyncServerFailed(boolean reinstalled) {
        safetyEvent(null);
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.SYNC_ERR);
        if (reinstalled) {
            dismissSyncProgressDialog();
        }
    }

    @Override
    public void onSyncServerProgress(long progress) {
        setSyncProgress(progress);
    }

    @Override
    public void onSyncServerFinish(boolean reinstalled) {
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.DEFAULT);
        getTotalUnreadMsgCount();
        if (reinstalled) {
            unLockUI();
            dismissSyncProgressDialog();
        }
    }

    @Override
    public void onSyncServerStart(boolean reinstalled) {
        userLogic.connectStatus.setValue(UserLogic.ConnectStatus.SYNCING);
        if (reinstalled) {
            lockUI();
            displaySyncProgressDialog(context.get());
        }
    }

    @Override
    public void onTotalUnreadMessageCountChanged(int i) {
        totalUnreadMsgCount.setValue(i);
    }
}
