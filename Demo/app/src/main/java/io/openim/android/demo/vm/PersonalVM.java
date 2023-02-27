package io.openim.android.demo.vm;

import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson2.JSONObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.demo.repository.OpenIMService;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.entity.ExtendUserInfo;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.UserInfo;

public class PersonalVM extends BaseViewModel {
    public WaitDialog waitDialog;
    public MutableLiveData<ExtendUserInfo> exUserInfo = new MutableLiveData<>();

    @Override
    protected void viewCreate() {
        super.viewCreate();
        waitDialog = new WaitDialog(getContext());
    }

    OnBase<String> callBack = new OnBase<String>() {
        @Override
        public void onError(int code, String error) {
            waitDialog.dismiss();
            getIView().toast(error + code);
        }

        @Override
        public void onSuccess(String data) {
            waitDialog.dismiss();
            exUserInfo.setValue(exUserInfo.getValue());

            BaseApp.inst().loginCertificate.nickname = exUserInfo.getValue().userInfo.getNickname();
            BaseApp.inst().loginCertificate.faceURL = exUserInfo.getValue().userInfo.getFaceURL();
            BaseApp.inst().loginCertificate.globalRecvMsgOpt = exUserInfo.getValue().userInfo.getGlobalRecvMsgOpt();
            Obs.newMessage(Constant.Event.USER_INFO_UPDATE);
        }
    };

    public void getSelfUserInfo() {
        waitDialog.show();
        OpenIMClient.getInstance().userInfoManager.getSelfUserInfo(new OnBase<UserInfo>() {
            @Override
            public void onError(int code, String error) {
                waitDialog.dismiss();
                getIView().toast(error + code);
            }

            @Override
            public void onSuccess(UserInfo data) {
                waitDialog.dismiss();
                ExtendUserInfo extendUserInfo = new ExtendUserInfo();
                extendUserInfo.userInfo = data;
                exUserInfo.setValue(extendUserInfo);

                getExtendUserInfo();
            }
        });

    }

    private void getExtendUserInfo() {
        List<String> ids = new ArrayList<>();
        ids.add(BaseApp.inst().loginCertificate.userID);
        Parameter parameter = new Parameter().add("operationID", System.currentTimeMillis() + "").add("pageNumber", 1).add("showNumber", 1).add("userIDList", ids);
        N.API(OpenIMService.class).getUsersFullInfo(parameter.buildJsonBody()).map(OpenIMService.turn(HashMap.class)).compose(N.IOMain()).subscribe(new NetObserver<HashMap>(getContext()) {
            @Override
            protected void onFailure(Throwable e) {
                getIView().toast(e.getMessage());
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(HashMap map) {
                try {
                    ArrayList arrayList = (ArrayList) map.get("userFullInfoList");
                    if (null == arrayList || arrayList.isEmpty()) return;
                    String json = GsonHel.getGson().toJson(exUserInfo.getValue().userInfo);
                    HashMap map1 = JSONObject.parseObject(json, HashMap.class);
                    HashMap map2 = JSONObject.parseObject(arrayList.get(0).toString(), HashMap.class);
                    map1.putAll(map2);

                    exUserInfo.getValue().userInfo = GsonHel.getGson().fromJson(GsonHel.toJson(map1), UserInfo.class);
                    exUserInfo.setValue(exUserInfo.getValue());
                    BaseApp.inst().loginCertificate.globalRecvMsgOpt = exUserInfo.getValue().userInfo.getGlobalRecvMsgOpt();
                    BaseApp.inst().loginCertificate.cache(BaseApp.inst());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void getUserInfo(String id) {
        waitDialog.show();
        List<String> ids = new ArrayList<>();
        ids.add(id);
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                waitDialog.dismiss();
                getIView().toast(error + code);
            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                waitDialog.dismiss();
                if (data.isEmpty()) return;
                ExtendUserInfo extendUserInfo = new ExtendUserInfo();
                extendUserInfo.userInfo = data.get(0);
                exUserInfo.setValue(extendUserInfo);

                getExtendUserInfo();
            }
        }, ids);
    }

    public void setSelfInfo() {
        waitDialog.show();
        String userInfo = GsonHel.toJson(exUserInfo.getValue().userInfo);
        Map userInfoMap = JSONObject.parseObject(userInfo, Map.class);
        //扩展
        userInfoMap.put("platform", IMUtil.PLATFORM_ID);
        userInfoMap.put("userID", BaseApp.inst().loginCertificate.userID);
        userInfoMap.put("operationID", System.currentTimeMillis() + "");

        N.API(OpenIMService.class).updateUserInfo(Parameter.buildJsonBody(GsonHel.toJson(userInfoMap))).compose(N.IOMain()).map(OpenIMService.turn(Object.class))

            .subscribe(new NetObserver<Object>(getContext()) {
                @Override
                protected void onFailure(Throwable e) {
                    callBack.onError(-1, e.getMessage());
                }

                @Override
                public void onSuccess(Object o) {
                    callBack.onSuccess("");
                }
            });
    }

    public void setNickname(String nickname) {
        exUserInfo.getValue().userInfo.setNickname(nickname);
        setSelfInfo();
    }

    public void setFaceURL(String faceURL) {
        exUserInfo.getValue().userInfo.setFaceURL(faceURL);
        setSelfInfo();
    }

    public void setGender(int gender) {
        exUserInfo.getValue().userInfo.setGender(gender);
        setSelfInfo();
    }

    public void setBirthday(long birth) {
        exUserInfo.getValue().userInfo.setBirth(birth);
        setSelfInfo();
    }
}
