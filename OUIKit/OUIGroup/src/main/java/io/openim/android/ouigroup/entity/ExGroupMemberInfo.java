package io.openim.android.ouigroup.entity;

import java.util.Objects;

import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.UserInfo;

public class ExGroupMemberInfo {
    public String sortLetter; //显示数据拼音的首字母
    public GroupMembersInfo groupMembersInfo;
    public boolean isSticky = false; //是否是Sticky


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
