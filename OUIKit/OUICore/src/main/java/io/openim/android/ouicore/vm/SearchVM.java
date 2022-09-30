package io.openim.android.ouicore.vm;

import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.FriendshipInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.UserInfo;

public class SearchVM extends BaseViewModel {

    public MutableLiveData<List<GroupInfo>> groupsInfo = new MutableLiveData<>();
    public MutableLiveData<List<UserInfo>> userInfo = new MutableLiveData<>();
    public MutableLiveData<List<FriendshipInfo>> friendshipInfo = new MutableLiveData<>();

    public MutableLiveData<String> hail = new MutableLiveData<>();
    public MutableLiveData<String> remark = new MutableLiveData<>();
    //用户 或群组id
    public String searchContent = "";

    //y 搜索人 n 搜索群
    public boolean isPerson = false;

    public void searchPerson() {
        searchPerson(null);
    }

    public void searchPerson(List<String> ids) {
        if (null == ids) {
            ids = new ArrayList<>(); // 用户ID集合
            ids.add(searchContent);
        }
        //兼容旧版
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                L.e(error + "-" + code);
                userInfo.setValue(null);
            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;
                userInfo.setValue(data);
            }
        }, ids);
    }

    public void checkFriend(List<UserInfo> data) {
        List<String> uIds = new ArrayList<>();
        uIds.add(data.get(0).getUserID());
        OpenIMClient.getInstance().friendshipManager.checkFriend(new OnBase<List<FriendshipInfo>>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(List<FriendshipInfo> data) {
                friendshipInfo.setValue(data);
            }
        }, uIds);
    }

    public void addFriend() {
        OnBase<String> callBack = new OnBase<String>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(String data) {
                Toast.makeText(getContext(), "发送成功", Toast.LENGTH_SHORT).show();
                hail.setValue("-1");
            }
        };
        if (isPerson)
            OpenIMClient.getInstance().friendshipManager.addFriend(callBack, searchContent, hail.getValue());
        else
            OpenIMClient.getInstance().groupManager.joinGroup(callBack, searchContent, hail.getValue(), 2);
    }

    public void search() {
        if (isPerson)
            searchPerson();
        else
            searchGroup();
    }

    private void searchGroup() {
        List<String> groupIds = new ArrayList<>(); // 群ID集合
        groupIds.add(searchContent);
        OpenIMClient.getInstance().groupManager.getGroupsInfo(new OnBase<List<GroupInfo>>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(List<GroupInfo> data) {
                groupsInfo.setValue(data);
            }
        }, groupIds);

    }

    /**
     * 移除好友
     *
     * @param uid
     */
    public void deleteFriend(String uid) {
        OpenIMClient.getInstance().friendshipManager.deleteFriend(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                IView.toast(error);
            }

            @Override
            public void onSuccess(String data) {
                IView.toast(getContext().getString(io.openim.android.ouicore.R.string.delete_friend));
                IView.onSuccess(data);
            }
        }, uid);
    }
}
