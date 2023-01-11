package io.openim.android.ouicontact.vm;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.net.RXRetrofit.Exception.RXRetrofitException;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.OneselfService;
import io.openim.android.ouicore.utils.L;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;

public class LabelVM extends BaseViewModel {

    private static final String TAG = "LabelVM";

    public void getUserTags() {
        Parameter parameter = new Parameter().add("operationID", System.currentTimeMillis() + "");
        N.API(OneselfService.class).getTags(parameter.buildJsonBody())
            .compose(N.IOMain()).map(OneselfService.turn(Object.class))
            .subscribe(new NetObserver<Object>(TAG) {
                @Override
                public void onSuccess(Object o) {
                    L.e("");
                }

                @Override
                protected void onFailure(Throwable e) {
                    L.e("");
                }
            });


    }
}
