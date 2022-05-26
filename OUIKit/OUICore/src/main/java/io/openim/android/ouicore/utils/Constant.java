package io.openim.android.ouicore.utils;

public class Constant {
    public static final String DEFAULT_IP = "121.37.25.71";

    //IM sdk api地址
    public static final String IM_API_URL = "http://" + DEFAULT_IP + ":10002";
    //登录注册手机验 证服务器地址
    public static final String APP_AUTH_URL = "http://" + DEFAULT_IP + ":10004";
    //web socket
    public static final String IM_WS_URL = "ws://" + DEFAULT_IP + ":10001";


    public static class Event {
        //已读数量变化
        public static final int READ_CHANGE = 10001;
    }

    //会话类型
    public static class SessionType {
        public static final int SINGLE_CHAT = 1;
        public static final int GROUP_CHAT = 2;
    }

    public static final String ID = "Id";
    public static final String GROUP_ID = "group_id";
    public static final String IS_PERSON = "is_person";

}
