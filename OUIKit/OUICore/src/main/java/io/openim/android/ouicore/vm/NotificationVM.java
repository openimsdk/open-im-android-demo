package io.openim.android.ouicore.vm;

import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.ex.UserEx;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnCustomBusinessListener;
import io.openim.android.sdk.listener.OnFriendshipListener;
import io.openim.android.sdk.listener.OnGroupListener;
import io.openim.android.sdk.models.FriendApplicationInfo;
import io.openim.android.sdk.models.GroupApplicationInfo;

public class NotificationVM extends BaseVM implements OnCustomBusinessListener, OnGroupListener,
    OnFriendshipListener {

    //群红点数量
    public State<List<UserEx>> groupDot = new State<>(new ArrayList<>());
    //好友通知红点
    public State<List<UserEx>> friendDot = new State<>(new ArrayList<>());

    public State<String> customBusinessMessage = new State<>();
    public State<Integer> momentsUnread = new State<>();
    public String K_friendRequest = BaseApp.inst().loginCertificate.userID + Constants.K_FRIEND_NUM;
    public String K_groupRequest = BaseApp.inst().loginCertificate.userID + Constants.K_GROUP_NUM;

    public NotificationVM() {
        OpenIMClient.getInstance().setCustomBusinessListener(this);
        IMEvent.getInstance().addGroupListener(this);
        IMEvent.getInstance().addFriendListener(this);

        initDot();
    }

    private void initDot() {
      try {
          String friendRequest = SharedPreferencesUtil.get(BaseApp.inst()).getString(K_friendRequest);
          String groupRequest = SharedPreferencesUtil.get(BaseApp.inst()).getString(K_groupRequest);
          if (!TextUtils.isEmpty(friendRequest)) {
              List<UserEx> mexList = new ArrayList<>();
              mexList.add(new UserEx(friendRequest));
              friendDot.setValue(mexList);
          }
          if (!TextUtils.isEmpty(groupRequest)) {
              List<UserEx> mexList = new ArrayList<>();
              mexList.add(new UserEx(groupRequest));
              groupDot.setValue(mexList);
          }
      }catch (Exception e){
          e.printStackTrace();
      }
    }

    @Override
    public void onRecvCustomBusinessMessage(String s) {
        customBusinessMessage.setValue(s);
    }


    @Override
    public void onGroupApplicationAccepted(GroupApplicationInfo info) {
        cacheGroupDot(info);
    }

    @Override
    public void onGroupApplicationAdded(GroupApplicationInfo info) {
        cacheGroupDot(info);
    }


    @Override
    public void onGroupApplicationRejected(GroupApplicationInfo info) {
        cacheGroupDot(info);
    }


    private void cacheGroupDot(GroupApplicationInfo info) {
        if (info.getHandleResult() == 0 && !info.getUserID().equals(BaseApp.inst().loginCertificate.userID)) {
            UserEx userEx = new UserEx(info.getGroupID());
            if (!groupDot.val().contains(userEx)) {
                groupDot.val().add(userEx);
                groupDot.update();
                SharedPreferencesUtil.get(BaseApp.inst()).setCache(K_groupRequest,
                    info.getGroupID());
            }
        }
    }

    private void cacheFriendDot(FriendApplicationInfo u) {
        if (u.getHandleResult() == 0 && !u.getFromUserID().equals(BaseApp.inst().loginCertificate.userID)) {
            UserEx userEx = new UserEx(u.getFromUserID());
            if (!friendDot.val().contains(userEx)) {
                friendDot.val().add(userEx);
                friendDot.update();
                SharedPreferencesUtil.get(BaseApp.inst()).setCache(K_friendRequest,
                    u.getFromUserID());
            }
        }
    }


    @Override
    public void onFriendApplicationAccepted(FriendApplicationInfo u) {
        cacheFriendDot(u);
    }

    @Override
    public void onFriendApplicationAdded(FriendApplicationInfo u) {
        cacheFriendDot(u);
    }

    @Override
    public void onFriendApplicationRejected(FriendApplicationInfo u) {
        cacheFriendDot(u);
    }

    public void clearDot(State<List<UserEx>> state) {
        state.val().clear();
        state.update();
        SharedPreferencesUtil.remove(BaseApp.inst(), state == friendDot ? K_friendRequest :
            K_groupRequest);
    }

    public boolean hasDot() {
        return !friendDot.val().isEmpty() || !groupDot.val().isEmpty();
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        IMEvent.getInstance().removeGroupListener(this);
        IMEvent.getInstance().removeFriendListener(this);
        N.clearDispose(tag());
    }
}
