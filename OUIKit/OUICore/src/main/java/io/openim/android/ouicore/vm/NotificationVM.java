package io.openim.android.ouicore.vm;

import java.util.HashMap;

import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnCustomBusinessListener;

public class NotificationVM extends BaseVM implements OnCustomBusinessListener {

    public State<String> customBusinessMessage = new State<>();
    public State<Integer> momentsUnread = new State<>();

    public NotificationVM() {
        OpenIMClient.getInstance().setCustomBusinessListener(this);
    }

    @Override
    public void onRecvCustomBusinessMessage(String s) {
        getWorkMomentsUnReadCount();
        customBusinessMessage.setValue(s);
    }

    public void getWorkMomentsUnReadCount() {
        N.API(OneselfService.class).getMomentsUnreadCount(new Parameter().buildJsonBody())
            .map(OneselfService.turn(HashMap.class))
            .compose(N.IOMain()).subscribe(new NetObserver<HashMap>(tag()) {
            @Override
            public void onSuccess(HashMap map) {
                try {
                    int size = (int) map.get("total");
                    momentsUnread.setValue(size);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onFailure(Throwable e) {
                toast(e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        N.clearDispose(tag());
    }
}
