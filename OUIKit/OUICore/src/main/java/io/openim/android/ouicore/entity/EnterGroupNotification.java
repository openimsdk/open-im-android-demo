package io.openim.android.ouicore.entity;

import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

public class EnterGroupNotification {
    /// 群信息
    public GroupInfo group;

    /// 进入群的成员信息
    public GroupMembersInfo entrantUser;
}
