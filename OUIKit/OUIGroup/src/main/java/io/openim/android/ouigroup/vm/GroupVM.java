package io.openim.android.ouigroup.vm;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.entity.NotificationMsg;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouigroup.entity.ExGroupMemberInfo;
import io.openim.android.ouigroup.entity.ExUserInfo;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupInviteResult;
import io.openim.android.sdk.models.GroupMemberRole;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.UserInfo;

public class GroupVM extends BaseViewModel {
    public MutableLiveData<String> groupName = new MutableLiveData<>("");

    public MutableLiveData<GroupInfo> groupsInfo = new MutableLiveData<>();
    //当前用户是否是群主
    public MutableLiveData<Boolean> isGroupOwner = new MutableLiveData<>(true);
    //群所有成员
    public MutableLiveData<List<GroupMembersInfo>> groupMembers = new MutableLiveData<>(new ArrayList<>());
    //封装过的群成员 用于字母导航
    public MutableLiveData<List<ExGroupMemberInfo>> exGroupMembers = new MutableLiveData<>(new ArrayList<>());
    //群管理
    public MutableLiveData<List<ExGroupMemberInfo>> exGroupManagement = new MutableLiveData<>(new ArrayList<>());
    //群字母导航
    public MutableLiveData<List<String>> groupLetters = new MutableLiveData<>(new ArrayList<>());

    //封装过的好友信息 用于字母导航
    public MutableLiveData<List<ExUserInfo>> exUserInfo = new MutableLiveData<>(new ArrayList<>());
    public String groupId;
    public MutableLiveData<List<String>> letters = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<FriendInfo>> selectedFriendInfo = new MutableLiveData<>(new ArrayList<>());
    public LoginCertificate loginCertificate;
    //是否是邀请入群
    public boolean isInviteToGroup = false;


    @Override
    protected void viewCreate() {
        super.viewCreate();
        loginCertificate = LoginCertificate.getCache(getContext());
    }

    public void getAllFriend() {
        exUserInfo.getValue().clear();
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
                    if (isInviteToGroup) {
                        ExGroupMemberInfo exGroupMemberInfo = new ExGroupMemberInfo();
                        exGroupMemberInfo.groupMembersInfo = new GroupMembersInfo();
                        exGroupMemberInfo.groupMembersInfo.setUserID(datum.getUserID());
                        exUserInfo.isSelect = exGroupMembers.getValue().contains(exGroupMemberInfo);
                        //如果已经存在群里则不能点击(不能重复邀请入群)
                        if (exUserInfo.isSelect)
                            exUserInfo.isEnabled = false;
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
                if (data.isEmpty()) return;
                groupsInfo.setValue(data.get(0));
                isGroupOwner.setValue(isOwner());
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
     * 更新群信息
     *
     * @param groupID      群ID
     * @param groupName    群名称
     * @param faceURL      群icon
     * @param notification 群公告
     * @param introduction 群简介
     * @param ex           其他信息
     */
    public void updataGroup(String groupID, String groupName, String faceURL, String notification, String introduction, String ex) {
        OpenIMClient.getInstance().groupManager.setGroupInfo(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                IView.onError(error);
            }

            @Override
            public void onSuccess(String data) {
                IView.onSuccess(data);
                getGroupsInfo();
            }
        }, groupID, groupName, faceURL, notification, introduction, ex);
    }

    /**
     * 修改所在群的昵称
     *
     * @param gid           群ID
     * @param uid           群成员userID
     * @param groupNickname 群内显示名称
     */
    public void setGroupMemberNickname(String gid, String uid, String groupNickname) {
        OpenIMClient.getInstance().groupManager.setGroupMemberNickname(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(String data) {
                IView.onSuccess(data);
                getGroupsInfo();
            }
        }, gid, uid, groupNickname);
    }

    /**
     * 获取群成员信息
     */
    public void getGroupMemberList() {
        exGroupMembers.getValue().clear();
        exGroupManagement.getValue().clear();
        groupLetters.getValue().clear();
        OpenIMClient.getInstance().groupManager.getGroupMemberList(new OnBase<List<GroupMembersInfo>>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(List<GroupMembersInfo> data) {
                if (data.isEmpty()) return;
                groupMembers.setValue(data);

                List<ExGroupMemberInfo> exGroupMemberInfos = new ArrayList<>();
                List<ExGroupMemberInfo> otGroupMemberInfos = new ArrayList<>();
                for (GroupMembersInfo datum : data) {
                    ExGroupMemberInfo exGroupMemberInfo = new ExGroupMemberInfo();
                    exGroupMemberInfo.groupMembersInfo = datum;
                    if (datum.getRoleLevel() > 1) {
                        //群管理单独存放
                        exGroupManagement.getValue().add(exGroupMemberInfo);
                        continue;
                    }
                    String nickName = "0";
                    if (!TextUtils.isEmpty(datum.getNickname()))
                        nickName = datum.getNickname();
                    String letter = Pinyin.toPinyin(nickName.charAt(0));
                    letter = (letter.charAt(0) + "").trim().toUpperCase();
                    if (!Common.isAlpha(letter)) {
                        exGroupMemberInfo.sortLetter = "#";
                        otGroupMemberInfos.add(exGroupMemberInfo);
                    } else {
                        exGroupMemberInfo.sortLetter = letter;
                        exGroupMemberInfos.add(exGroupMemberInfo);
                    }
                    if (!groupLetters.getValue().contains(exGroupMemberInfo.sortLetter))
                        groupLetters.getValue().add(exGroupMemberInfo.sortLetter);
                }

                Collections.sort(groupLetters.getValue(), new LettersPinyinComparator());
                groupLetters.getValue().add(0, "↑");
                groupLetters.setValue(groupLetters.getValue());

                exGroupMembers.getValue().addAll(exGroupMemberInfos);
                exGroupMembers.getValue().addAll(otGroupMemberInfos);

                Collections.sort(exGroupMembers.getValue(), new PinyinComparator());
                Collections.sort(exGroupManagement.getValue(), (o1, o2) -> {
                    if (o2.groupMembersInfo.getRoleLevel()
                        < o1.groupMembersInfo.getRoleLevel())
                        return -1;
                    return 0;
                });
                exGroupMembers.setValue(exGroupMembers.getValue());
            }
        }, groupId, 0, 0, 0);
    }

    /**
     * 邀请入群
     */
    public void inviteUserToGroup(List<FriendInfo> friendInfos) {
        List<String> userIds = new ArrayList<>();
        for (FriendInfo friendInfo : friendInfos) {
            userIds.add(friendInfo.getUserID());
        }
        OpenIMClient.getInstance().groupManager.inviteUserToGroup(new OnBase<List<GroupInviteResult>>() {
            @Override
            public void onError(int code, String error) {
                IView.toast(error);
            }

            @Override
            public void onSuccess(List<GroupInviteResult> data) {
                IView.toast(getContext().getString(io.openim.android.ouicore.R.string.Invitation_succeeded));
                getGroupMemberList();
                IView.onSuccess(null);
            }
        }, groupId, userIds, "");
    }

    /**
     * 邀请入群
     */
    public void kickGroupMember(List<FriendInfo> friendInfos) {
        List<String> userIds = new ArrayList<>();
        for (FriendInfo friendInfo : friendInfos) {
            userIds.add(friendInfo.getUserID());
        }
        OpenIMClient.getInstance().groupManager.kickGroupMember(new OnBase<List<GroupInviteResult>>() {
            @Override
            public void onError(int code, String error) {
                IView.toast(error);
            }

            @Override
            public void onSuccess(List<GroupInviteResult> data) {
                IView.toast(getContext().getString(io.openim.android.ouicore.R.string.kicked_out));
                getGroupMemberList();
                IView.onSuccess(null);
            }
        }, groupId, userIds, "");
    }

    /**
     * 获取userID在群里的ExGroupMemberInfo 对象
     *
     * @return
     */
    public ExGroupMemberInfo getOwnInGroup(String userID) {
        ExGroupMemberInfo memberInfo = new ExGroupMemberInfo();
        GroupMembersInfo groupMembersInfo = new GroupMembersInfo();
        groupMembersInfo.setUserID(userID);
        memberInfo.groupMembersInfo = groupMembersInfo;
        int index = exGroupManagement.getValue().indexOf(memberInfo);
        if (index != -1)
            return exGroupManagement.getValue().get(index);

        index = exGroupMembers.getValue().indexOf(memberInfo);
        if (index != -1)
            return exGroupMembers.getValue().get(index);
        return null;
    }

    public class PinyinComparator implements Comparator<ExGroupMemberInfo> {

        public int compare(ExGroupMemberInfo o1, ExGroupMemberInfo o2) {
            //根据ABCDEFG...来排序
            if (o1.sortLetter.equals("#")) {
                return 1;
            } else if (o2.sortLetter.equals("#")) {
                return -1;
            } else {
                return o1.sortLetter.compareTo(o2.sortLetter);
            }
        }
    }

    public class LettersPinyinComparator implements Comparator<String> {

        public int compare(String o1, String o2) {
            //根据ABCDEFG...来排序
            if (o1.equals("#")) {
                return 1;
            } else if (o2.equals("#")) {
                return -1;
            } else {
                return o1.compareTo(o2);
            }
        }
    }

    public boolean isOwner() {
        GroupInfo groupInfo = groupsInfo.getValue();
        if (null == groupInfo) return false;
        return groupInfo.getOwnerUserID().equals(loginCertificate.userID);
    }
}
