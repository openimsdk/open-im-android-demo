package io.openim.android.ouimoments.mvp.presenter;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.WorkMomentsInfo;

public class MsgDetailVM extends BaseViewModel {
   public MutableLiveData<List<WorkMomentsInfo>> workMomentsInfo = new MutableLiveData<>();

    public void getWorkMomentsNotification() {
        OpenIMClient.getInstance().workMomentsManager
            .getWorkMomentsNotification(new OnBase<List<WorkMomentsInfo>>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error);
            }

            @Override
            public void onSuccess(List<WorkMomentsInfo> data) {
                workMomentsInfo.setValue(data);
            }
        }, 0, 10000);
    }
}
