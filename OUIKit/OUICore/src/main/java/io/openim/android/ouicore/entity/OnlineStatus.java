package io.openim.android.ouicore.entity;

import java.util.List;

public class OnlineStatus {
    public String userID;
    public String status;
    public List<DetailPlatformStatus> detailPlatformStatus;

    public static class DetailPlatformStatus {
        public String platform;
        public String status;
    }

}



