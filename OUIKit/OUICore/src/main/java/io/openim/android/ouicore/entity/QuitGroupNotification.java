package io.openim.android.ouicore.entity;

import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

public class QuitGroupNotification {
    /// 群信息
    public GroupInfo group;

    /// 退群的成员信息
    public GroupMembersInfo quitUser;
}
