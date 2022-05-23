package io.openim.android.demo.vm;

import static io.openim.android.ouicore.utils.Common.md5;

import androidx.lifecycle.MutableLiveData;


import java.io.IOException;


import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.demo.repository.OpenIMService;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.IView;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import okhttp3.ResponseBody;

public class LoginVM extends BaseViewModel<LoginVM.ViewAction> {

    public MutableLiveData<String> account = new MutableLiveData<>("");
    public MutableLiveData<String> pwd = new MutableLiveData<>("");
    public MutableLiveData<Boolean> isPhone = new MutableLiveData<>(true);


    public void login() {
        Parameter parameter = new Parameter()
                .add("password", md5(pwd.getValue()))
                .add("platform", 2)
                .add("operationID", System.currentTimeMillis() + "");
        if (isPhone.getValue()) {
            parameter.add("phoneNumber", account.getValue());
            parameter.add("areaCode", "+86");
        } else
            parameter.add("email", account.getValue());


        WaitDialog waitDialog = new WaitDialog(context);
        waitDialog.setNotDismiss();
        waitDialog.show();
        N.API(OpenIMService.class).login(parameter.buildJsonBody())
                .compose(N.IOMain())
                .subscribe(new NetObserver<ResponseBody>(context) {

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        waitDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(ResponseBody o) {
                        try {
                            String body = o.string();
                            Base<LoginCertificate> loginCertificate = GsonHel.dataObject(body, LoginCertificate.class);
                            if (loginCertificate.errCode != 0) {
                                IView.loginErr(loginCertificate.errMsg);
                                return;
                            }

                            OpenIMClient.getInstance().login(new OnBase<String>() {
                                @Override
                                public void onError(int code, String error) {
                                    IView.loginErr(error);
                                }

                                @Override
                                public void onSuccess(String data) {
                                    //缓存登录信息
                                    loginCertificate.data.cache(context);
                                    IView.jump();
                                }
                            }, loginCertificate.data.userID, loginCertificate.data.token);

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        IView.loginErr(e.getMessage());
                    }
                });
    }

    public interface ViewAction extends IView  {
        ///跳转
        void jump();

        void loginErr(String msg);

        void initDate();
    }

}
