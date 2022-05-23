package io.openim.android.demo.vm;

import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.FriendshipInfo;
import io.openim.android.sdk.models.UserInfo;

public class SearchVM extends BaseViewModel {

    public MutableLiveData<List<UserInfo>> userInfo = new MutableLiveData<>();
    public MutableLiveData<List<FriendshipInfo>> friendshipInfo = new MutableLiveData<>();

    public MutableLiveData<String> hail = new MutableLiveData<>();
    public MutableLiveData<String> remark = new MutableLiveData<>();
    public String searchContent = "";


    public void search() {
        List<String> uidList = new ArrayList<>(); // 用户ID集合
        uidList.add(searchContent);
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                userInfo.setValue(null);
            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                userInfo.setValue(data);
            }
        }, uidList);
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
        OpenIMClient.getInstance().friendshipManager.addFriend(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(String data) {
                Toast.makeText(context, "发送成功", Toast.LENGTH_SHORT).show();
                hail.setValue("-1");
            }
        }, searchContent, hail.getValue());
    }

}
