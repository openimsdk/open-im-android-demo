package io.openim.android.ouicontact.vm;

import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnGroupListener;
import io.openim.android.sdk.models.GroupApplicationInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

public class ContactVM extends BaseViewModel implements OnGroupListener {
    //红点数量
    public MutableLiveData<Integer> dotNum = new MutableLiveData<>(0);

    //申请列表
    public MutableLiveData<List<GroupApplicationInfo>> groupApply = new MutableLiveData<>();

    //申请详情
    public MutableLiveData<GroupApplicationInfo> applyDetail = new MutableLiveData<>();


    @Override
    protected void viewCreate() {
        super.viewCreate();
        IMEvent.getInstance().addGroupListener(this);
    }

    @Override
    protected void viewDestroy() {
        super.viewDestroy();
        IMEvent.getInstance().removeGroupListener(this);
    }

    //申请列表
    public void getRecvGroupApplicationList() {
        OpenIMClient.getInstance().groupManager.getRecvGroupApplicationList(new OnBase<List<GroupApplicationInfo>>() {
            @Override
            public void onError(int code, String error) {

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
             IView.toast(error);
        }

        @Override
        public void onSuccess(String data) {
            getRecvGroupApplicationList();
            IView.onSuccess(null);
        }
    };

    //通过
    public void pass() {
        OpenIMClient.getInstance().groupManager.acceptGroupApplication(onBase, applyDetail.getValue().getGroupID(), applyDetail.getValue().getUserID(), "");
    }


    //拒绝
    public void refuse() {
        OpenIMClient.getInstance().groupManager.acceptGroupApplication(onBase, applyDetail.getValue().getGroupID(), applyDetail.getValue().getUserID(), "");
    }

    @Override
    public void onGroupApplicationAccepted(GroupApplicationInfo info) {
        dotNum.setValue(dotNum.getValue() + 1);
    }

    @Override
    public void onGroupApplicationAdded(GroupApplicationInfo info) {
        dotNum.setValue(dotNum.getValue() + 1);
    }

    @Override
    public void onGroupApplicationDeleted(GroupApplicationInfo info) {

    }

    @Override
    public void onGroupApplicationRejected(GroupApplicationInfo info) {

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
        dotNum.setValue(dotNum.getValue() + 1);
    }

    @Override
    public void onJoinedGroupDeleted(GroupInfo info) {

    }
}
