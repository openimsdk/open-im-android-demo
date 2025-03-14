package io.openim.android.ouicore.entity;

import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

public class GroupRightsTransferNotification {
    /// 群信息
    public GroupInfo group;

    /// 操作者信息
    public  GroupMembersInfo opUser;

    /// 群新的拥有者信息
    public GroupMembersInfo newGroupOwner;
}
