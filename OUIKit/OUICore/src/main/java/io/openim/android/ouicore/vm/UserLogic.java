package io.openim.android.ouicore.vm;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.UserInfo;

public class UserLogic extends BaseVM {
    public enum LoginStatus {
        DEFAULT, LOGGING, SUCCESS, FAIL;
        public String value;

        public LoginStatus setValue(String value) {
            this.value = value;
            return this;
        }
    }

    public enum ConnectStatus {
        DEFAULT(""), CONNECTING(BaseApp.inst().getString(R.string.connecting)),
        CONNECT_ERR(BaseApp.inst().getString(R.string.conn_failed)),
        SYNCING(BaseApp.inst().getString(R.string.syncing)),
        SYNC_ERR(BaseApp.inst().getString(R.string.sync_err));
        public final String value;

        ConnectStatus(String value) {
            this.value = value;
        }
    }

    public State<LoginStatus> loginStatus = new State<>(LoginStatus.DEFAULT);
    public State<ConnectStatus> connectStatus = new State<>(ConnectStatus.DEFAULT);
    public State<UserInfo> info = new State<>();


    public boolean isCacheUser() {
        return null != (BaseApp.inst().loginCertificate =
            LoginCertificate.getCache(BaseApp.inst()));
    }

    public void loginCacheUser() {
        if (IMUtil.isLogged()) {
            loginStatus.setValue(LoginStatus.SUCCESS);
            return;
        }
        loginStatus.setValue(LoginStatus.LOGGING);
        LoginCertificate certificate = LoginCertificate.getCache(BaseApp.inst());
        assert certificate != null;
        OpenIMClient.getInstance().login(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                loginStatus.setValue(LoginStatus.FAIL.setValue("(" + code + ")" + error));
            }

            @Override
            public void onSuccess(String data) {
                loginStatus.setValue(LoginStatus.SUCCESS);
            }
        }, certificate.userID, certificate.imToken);
    }

}
