package io.openim.android.ouicore.entity;

import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

public class MuteMemberNotification {
    /// 群信息
    public GroupInfo group;

    /// 操作者信息
    public GroupMembersInfo opUser;

    /// 被禁言的成员信息
    public GroupMembersInfo mutedUser;

    /// 禁言时间s
    public  int mutedSeconds;
}
