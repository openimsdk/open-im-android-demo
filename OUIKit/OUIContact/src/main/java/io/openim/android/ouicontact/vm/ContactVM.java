package io.openim.android.ouicontact.vm;

import java.util.List;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnFriendshipListener;
import io.openim.android.sdk.listener.OnGroupListener;
import io.openim.android.sdk.models.FriendApplicationInfo;
import io.openim.android.sdk.models.GroupApplicationInfo;
import io.openim.android.sdk.models.UserInfo;

public class ContactVM extends BaseViewModel implements OnGroupListener, OnFriendshipListener {

    //申请列表
    public State<List<GroupApplicationInfo>> groupApply = new State<>();
    //好友申请列表
    public State<List<FriendApplicationInfo>> friendApply = new State<>();
    //申请详情
    public State<GroupApplicationInfo> groupDetail = new State<>();
    //好友申请详情
    public State<FriendApplicationInfo> friendDetail = new State<>();
    //常联系的好友
    public State<UserInfo> frequentContacts = new State<>();


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
        getRecvGroupApplicationList();
    }

    @Override
    public void onGroupApplicationRejected(GroupApplicationInfo info) {
        getRecvGroupApplicationList();
    }
}
