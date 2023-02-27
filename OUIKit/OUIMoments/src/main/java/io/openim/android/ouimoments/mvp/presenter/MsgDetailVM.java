package io.openim.android.ouimoments.mvp.presenter;

import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson2.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouimoments.bean.EXWorkMomentsInfo;
import io.openim.android.ouimoments.bean.MomentsContent;
import io.openim.android.ouimoments.bean.MomentsData;
import io.openim.android.ouimoments.widgets.dialog.CommentDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.WorkMomentsInfo;

public class MsgDetailVM extends BaseViewModel {
   public MutableLiveData<List<EXWorkMomentsInfo>> workMomentsInfo = new MutableLiveData<>(new ArrayList<>());

    public void getWorkMomentsNotification() {
        OpenIMClient.getInstance().workMomentsManager
            .getWorkMomentsNotification(new OnBase<List<WorkMomentsInfo>>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error);
            }

            @Override
            public void onSuccess(List<WorkMomentsInfo> data) {
                for (WorkMomentsInfo datum : data) {
                    Map map = JSONObject.parseObject(datum.getWorkMomentContent(), Map.class);
                    JsonElement string = JsonParser.parseString((String) map.get("data"));
                    MomentsData momentsContent = GsonHel.fromJson(string.toString(),
                        MomentsData.class);
                    workMomentsInfo.getValue().add(new EXWorkMomentsInfo(momentsContent.data,datum));
                }
                workMomentsInfo.setValue(workMomentsInfo.getValue());
            }
        }, 0, 10000);
    }

    public void clearMsg() {
        OpenIMClient.getInstance().workMomentsManager.clearWorkMomentsNotification(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(String data) {
                workMomentsInfo.getValue().clear();
                workMomentsInfo.setValue(workMomentsInfo.getValue());
            }
        });
    }
}
