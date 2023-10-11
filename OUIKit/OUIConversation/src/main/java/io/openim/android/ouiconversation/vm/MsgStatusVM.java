package io.openim.android.ouiconversation.vm;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.GroupMembersInfo;

public class MsgStatusVM extends BaseVM {
    public String conversationId, msgId;
    public int offset = 0;
    public int count = 20;

    public State<List<GroupMembersInfo>> groupMembersInfoList = new State<>(new ArrayList<>());

    /**
     * @param filter 0已读 1未读
     */
    public void getGroupMessageReaderList(int filter) {
        OpenIMClient.getInstance().messageManager.getGroupMessageReaderList(conversationId, msgId
            , filter, offset, count, new IMUtil.IMCallBack<List<GroupMembersInfo>>() {
                @Override
                public void onSuccess(List<GroupMembersInfo> data) {
                    if (data.isEmpty()) return;
                    offset += count;
                    groupMembersInfoList.val().addAll(data);
                    groupMembersInfoList.update();
                }
            });
    }
}
