package io.openim.android.ouicore.entity;

import io.openim.android.sdk.models.GroupMembersInfo;

public class ExGroupMemberInfo extends CommEx {
    public GroupMembersInfo groupMembersInfo;

    /**
     * contains 方法
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExGroupMemberInfo that = (ExGroupMemberInfo) o;
        if (that.groupMembersInfo.getUserID().equals(groupMembersInfo.getUserID()))
            return true;
        return false;
    }

}
