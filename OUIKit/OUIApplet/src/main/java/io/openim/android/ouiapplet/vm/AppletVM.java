package io.openim.android.ouiapplet.vm;

import java.util.HashMap;

import io.openim.android.ouiapplet.service.NetService;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.utils.L;

public class AppletVM extends BaseVM {

    public void findApplet() {
        N.API(NetService.class)
            .findApplet(new  Parameter().buildJsonBody())
            .map(OneselfService.turn(HashMap.class))
            .compose(N.IOMain())
            .subscribe(new NetObserver<HashMap>("") {


                @Override
                public void onSuccess(HashMap o) {
                    o.get("applets");
                    L.e("");

                }

                @Override
                protected void onFailure(Throwable e) {
                    L.e("");
                }
            });
    }
}
