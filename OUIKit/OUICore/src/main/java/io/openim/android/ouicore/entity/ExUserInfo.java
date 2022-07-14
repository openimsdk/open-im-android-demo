package io.openim.android.ouicore.entity;

import io.openim.android.sdk.models.UserInfo;

public class ExUserInfo  extends  SortLetter{
    public UserInfo userInfo;
    public ExGroupMemberInfo exGroupMemberInfo;
    public boolean isSticky = false; //是否是Sticky
    public boolean isSelect = false;//是否被选中
    public boolean isEnabled = true;//是否可点击
}
