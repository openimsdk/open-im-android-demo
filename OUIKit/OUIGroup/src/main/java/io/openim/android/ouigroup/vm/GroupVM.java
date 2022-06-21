package io.openim.android.ouigroup.vm;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.github.promeg.pinyinhelper.Pinyin;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.entity.ExUserInfo;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMemberRole;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.UserInfo;

public class GroupVM extends BaseViewModel {
    public MutableLiveData<String> groupName = new MutableLiveData<>("");
    public MutableLiveData<GroupInfo> groupsInfo = new MutableLiveData<>();
    public MutableLiveData<List<ExUserInfo>> exUserInfo = new MutableLiveData<>(new ArrayList<>());
    public String groupId;
    public MutableLiveData<List<String>> letters = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<FriendInfo>> selectedFriendInfo = new MutableLiveData<>(new ArrayList<>());
    private LoginCertificate loginCertificate;


    @Override
    protected void viewCreate() {
        super.viewCreate();
        loginCertificate = LoginCertificate.getCache(getContext());
    }

    public void getAllFriend() {
        OpenIMClient.getInstance().friendshipManager.getFriendList(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;
                List<ExUserInfo> exInfos = new ArrayList<>();
                List<ExUserInfo> otInfos = new ArrayList<>();
                for (UserInfo datum : data) {
                    ExUserInfo exUserInfo = new ExUserInfo();
                    exUserInfo.userInfo = datum;
                    String letter = Pinyin.toPinyin(exUserInfo.userInfo.getFriendInfo().getNickname().charAt(0));
                    if (!Common.isAlpha(letter)) {
                        exUserInfo.sortLetter = "#";
                        otInfos.add(exUserInfo);
                    } else {
                        exUserInfo.sortLetter = letter;
                        exInfos.add(exUserInfo);
                    }
                }
                for (ExUserInfo userInfo : exInfos) {
                    if (!letters.getValue().contains(userInfo.sortLetter))
                        letters.getValue().add(userInfo.sortLetter);
                }
                if (!otInfos.isEmpty())
                    letters.getValue().add("#");
                letters.setValue(letters.getValue());

                exUserInfo.getValue().addAll(exInfos);
                exUserInfo.getValue().addAll(otInfos);

                exUserInfo.setValue(exUserInfo.getValue());
            }
        });
    }


    /**
     * 获取群组信息
     */
    public void getGroupsInfo() {
        List<String> groupIds = new ArrayList<>(); // 群ID集合
        groupIds.add(groupId);
        OpenIMClient.getInstance().groupManager.getGroupsInfo(new OnBase<List<GroupInfo>>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(List<GroupInfo> data) {
                if (!data.isEmpty())
                    groupsInfo.setValue(data.get(0));
            }
        }, groupIds);
    }

    /**
     * 创建群组
     */
    public void createGroup() {
        List<GroupMemberRole> groupMemberRoles = new ArrayList<>();
        LoginCertificate loginCertificate = LoginCertificate.getCache(getContext());
        for (FriendInfo friendInfo : selectedFriendInfo.getValue()) {
            GroupMemberRole groupMemberRole = new GroupMemberRole();
            if (friendInfo.getUserID().equals(loginCertificate.userID)) {
                groupMemberRole.setRoleLevel(2);
            } else
                groupMemberRole.setRoleLevel(1);
            groupMemberRole.setUserID(friendInfo.getUserID());
            groupMemberRoles.add(groupMemberRole);
        }
        OpenIMClient.getInstance().groupManager.createGroup(new OnBase<GroupInfo>() {
            @Override
            public void onError(int code, String error) {
                IView.onError(error);
            }

            @Override
            public void onSuccess(GroupInfo data) {
                IView.onSuccess(data);
            }
        }, groupName.getValue(), null, null, null, 0, null, groupMemberRoles);
    }

    /**
     * 当前用户是否是群主
     * @return
     */
    public boolean isGroupOwner() {
        GroupInfo groupInfo = groupsInfo.getValue();
        if (null == groupInfo) return false;
        return groupInfo.getOwnerUserID().equals(loginCertificate.userID);
    }
}
