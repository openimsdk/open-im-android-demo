package io.openim.android.ouicore.entity;

import java.util.List;

import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

public class GroupNotification {
    /// 群信息
    public GroupInfo group;

    /// 当前事件操作者信息
    public GroupMembersInfo opUser;

    /// 群拥有者信息
    public GroupMembersInfo groupOwnerUser;

    /// 产生影响的群成员列表
    public List<GroupMembersInfo> memberList;

    /// 资料发生改变的成员
    GroupMembersInfo changedUser;
}
