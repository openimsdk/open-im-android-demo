package io.openim.android.ouicontact.vm;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnFriendshipListener;
import io.openim.android.sdk.listener.OnGroupListener;
import io.openim.android.sdk.models.BlacklistInfo;
import io.openim.android.sdk.models.FriendApplicationInfo;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.GroupApplicationInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.UserInfo;

public class ContactVM extends BaseViewModel implements OnGroupListener, OnFriendshipListener {
    //群红点数量
    public MutableLiveData<Integer> groupDotNum = new MutableLiveData<>(0);
    //好友通知红点
    public MutableLiveData<Integer> friendDotNum = new MutableLiveData<>(0);
    //申请列表
    public MutableLiveData<List<GroupApplicationInfo>> groupApply = new MutableLiveData<>();
    //好友申请列表
    public MutableLiveData<List<FriendApplicationInfo>> friendApply = new MutableLiveData<>();
    //申请详情
    public MutableLiveData<GroupApplicationInfo> groupDetail = new MutableLiveData<>();
    //好友申请详情
    public MutableLiveData<FriendApplicationInfo> friendDetail = new MutableLiveData<>();
    //常联系的好友
    public MutableLiveData<UserInfo> frequentContacts = new MutableLiveData<>();


    @Override
    protected void viewCreate() {
        super.viewCreate();
        IMEvent.getInstance().addGroupListener(this);
        IMEvent.getInstance().addFriendListener(this);
        int requestNum = SharedPreferencesUtil.get(getContext()).getInteger(Constant.K_FRIEND_NUM);
        int groupNum = SharedPreferencesUtil.get(getContext()).getInteger(Constant.K_GROUP_NUM);
        friendDotNum.setValue(requestNum);
        groupDotNum.setValue(groupNum);
    }

    @Override
    protected void releaseRes() {
        super.releaseRes();
        IMEvent.getInstance().removeGroupListener(this);
        IMEvent.getInstance().removeFriendListener(this);
    }

    //个人申请列表
    public void getRecvFriendApplicationList() {
        OpenIMClient.getInstance().friendshipManager.getRecvFriendApplicationList(new OnBase<List<FriendApplicationInfo>>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(List<FriendApplicationInfo> data) {
                if (data.isEmpty()) return;
                friendApply.setValue(data);
            }
        });
    }

    //群申请列表
    public void getRecvGroupApplicationList() {
        OpenIMClient.getInstance().groupManager.getRecvGroupApplicationList(new OnBase<List<GroupApplicationInfo>>() {
            @Override
            public void onError(int code, String error) {
                L.e("");
            }

            @Override
            public void onSuccess(List<GroupApplicationInfo> data) {
                L.e("");
                if (!data.isEmpty())
                    groupApply.setValue(data);
            }
        });
    }

    private OnBase onBase = new OnBase<String>() {
        @Override
        public void onError(int code, String error) {
            getIView().toast(error);
        }

        @Override
        public void onSuccess(String data) {
            if (null != groupDetail)
                getRecvGroupApplicationList();
            if (null != friendDetail)
                getRecvFriendApplicationList();
            getIView().onSuccess(null);
        }
    };

    //好友通过
    public void friendPass() {
        OpenIMClient.getInstance().friendshipManager.acceptFriendApplication(onBase, friendDetail.getValue().getFromUserID(), "");
    }


    //好友拒绝
    public void friendRefuse() {
        OpenIMClient.getInstance().friendshipManager.refuseFriendApplication(onBase, friendDetail.getValue().getFromUserID(), "");
    }

    //群通过
    public void pass() {
        OpenIMClient.getInstance().groupManager.acceptGroupApplication(onBase, groupDetail.getValue().getGroupID(), groupDetail.getValue().getUserID(), "");
    }

    //群拒绝
    public void refuse() {
        OpenIMClient.getInstance().groupManager.refuseGroupApplication(onBase,
            groupDetail.getValue().getGroupID(), groupDetail.getValue().getUserID(), "");
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
    public void onGroupApplicationDeleted(GroupApplicationInfo info) {

    }

    @Override
    public void onGroupApplicationRejected(GroupApplicationInfo info) {
        cacheGroupDot(info);
    }

    @Override
    public void onGroupDismissed(GroupInfo info) {

    }

    @Override
    public void onGroupInfoChanged(GroupInfo info) {

    }

    @Override
    public void onGroupMemberAdded(GroupMembersInfo info) {

    }

    @Override
    public void onGroupMemberDeleted(GroupMembersInfo info) {

    }

    @Override
    public void onGroupMemberInfoChanged(GroupMembersInfo info) {

    }

    @Override
    public void onJoinedGroupAdded(GroupInfo info) {

    }

    private void cacheGroupDot(GroupApplicationInfo info) {
        if (info.getHandleResult() == 0
            && !info.getUserID().equals(BaseApp.inst().loginCertificate.userID)) {
            groupDotNum.setValue(friendDotNum.getValue() + 1);
            SharedPreferencesUtil.get(getContext()).setCache(Constant.K_GROUP_NUM,
                groupDotNum.getValue());
        }
    }

    private void cacheFriendDot(FriendApplicationInfo u) {
        if (u.getHandleResult() == 0
            && !u.getFromUserID().equals(BaseApp.inst().loginCertificate.userID)) {
            friendDotNum.setValue(friendDotNum.getValue() + 1);
            SharedPreferencesUtil.get(getContext()).setCache(Constant.K_FRIEND_NUM,
                friendDotNum.getValue());
        }
    }

    @Override
    public void onJoinedGroupDeleted(GroupInfo info) {

    }

    @Override
    public void onBlacklistAdded(BlacklistInfo u) {

    }

    @Override
    public void onBlacklistDeleted(BlacklistInfo u) {

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
    public void onFriendApplicationDeleted(FriendApplicationInfo u) {
    }

    @Override
    public void onFriendApplicationRejected(FriendApplicationInfo u) {
        cacheFriendDot(u);
    }

    @Override
    public void onFriendInfoChanged(FriendInfo u) {

    }

    @Override
    public void onFriendAdded(FriendInfo u) {

    }

    @Override
    public void onFriendDeleted(FriendInfo u) {

    }
}
