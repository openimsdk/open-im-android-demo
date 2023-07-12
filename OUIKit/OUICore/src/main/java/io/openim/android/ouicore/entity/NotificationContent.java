package io.openim.android.ouicore.entity;

import java.io.Serializable;

public class NotificationContent implements Serializable {
    public String groupID;

    public String notification;

    public String ownerUserID;

    public long createTime;

    public int memberCount;

    public String creatorUserID;
}
