package io.openim.android.ouicontact.vm;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.entity.UserLabel;
import io.openim.android.ouicore.net.RXRetrofit.Exception.RXRetrofitException;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.OneselfService;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;

public class LabelVM extends BaseViewModel {

    private static final String TAG = "LabelVM";
    public MutableLiveData<List<UserLabel>> userLabels = new MutableLiveData<>(new ArrayList<>());

    public void getUserTags() {
        Parameter parameter = new Parameter().add("operationID", System.currentTimeMillis() + "");
        N.API(OneselfService.class).getTags(Constant.getImApiUrl() + "office/get_user_tags",
            BaseApp.inst().loginCertificate.imToken, parameter.buildJsonBody())
            .compose(N.IOMain()).map(OneselfService.turn(UserLabel.class))
            .subscribe(new NetObserver<UserLabel>(TAG) {
            @Override
            public void onSuccess(UserLabel o) {
                if (null==o.getTags()||o.getTags().isEmpty())return;
                userLabels.setValue(o.getTags());
            }

            @Override
            protected void onFailure(Throwable e) {
                getIView().toast(e.getMessage());
            }
        });


    }
}
