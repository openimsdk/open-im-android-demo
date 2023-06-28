package io.openim.android.ouicore.entity;

import java.util.List;

import io.openim.android.sdk.models.AtUserInfo;

public class AtMsgInfo {
    public String text;
    public List<String> atUserList;
    public List<AtUserInfo> atUsersInfo;
    public boolean isAtSelf;
}
