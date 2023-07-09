package io.openim.android.ouimoments.mvp.presenter;

import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.OneselfService;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouimoments.api.MomentsService;
import io.openim.android.ouimoments.bean.EXWorkMomentsInfo;
import io.openim.android.ouimoments.bean.MomentsContent;
import io.openim.android.ouimoments.bean.MomentsData;
import io.openim.android.ouimoments.bean.WorkMoments;
import io.openim.android.ouimoments.widgets.dialog.CommentDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.WorkMomentsInfo;
import okhttp3.ResponseBody;

public class MsgDetailVM extends BaseViewModel {

    public static final int WorkMomentLogTypeLike = 1;
    public static final int WorkMomentLogTypeAt = 2;
    public static final int WorkMomentLogTypeComment = 3;

    // 1:未读数 2:消息列表 3:全部
    public static final int clear_unread_num=1;
    public static final int msg_list=2;
    public static final int all=3;



    private static final String TAG = "MsgDetailVM";
    public State<List<WorkMoments>> workMomentsInfo = new State<>(new ArrayList<>());


    public void getWorkMomentsNotification() {
        N.API(MomentsService.class)
            .momentsMsg(MomentsService.buildPagination(1, 10000).buildJsonBody()).compose(N.IOMain()).map(OneselfService.turn(HashMap.class)).subscribe(new NetObserver<HashMap>(TAG) {
            @Override
            public void onSuccess(HashMap obj) {

                try {
                    JSONArray json = (JSONArray) obj.get("workMoments");
                    Type type = new TypeToken<List<WorkMoments>>() {
                    }.getType();
                    List<WorkMoments> data = JSONObject.parseObject(json.toString(), type);
                    workMomentsInfo.setValue(data);
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

    public void clearMsg(int type) {
        N.API(MomentsService.class)
            .clearMsg(new Parameter().add("type",type)
                .buildJsonBody())
                .compose(N.IOMain())
                    .map(OneselfService.turn(HashMap.class))
                        .subscribe(new NetObserver<HashMap>(TAG) {
                            @Override
                            public void onSuccess(HashMap o) {
                                workMomentsInfo.getValue().clear();
                                workMomentsInfo.setValue(workMomentsInfo.getValue());
                            }

                            @Override
                            protected void onFailure(Throwable e) {
                                toast(e.getMessage());
                            }
                        });
    }
}
