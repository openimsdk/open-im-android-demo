package io.openim.android.ouigroup.vm;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouigroup.R;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

public class GroupVM extends BaseViewModel {

    public MutableLiveData<GroupInfo> groupsInfo = new MutableLiveData<>();
    public String groupId;

    @Override
    protected void viewCreate() {
        super.viewCreate();

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


}
