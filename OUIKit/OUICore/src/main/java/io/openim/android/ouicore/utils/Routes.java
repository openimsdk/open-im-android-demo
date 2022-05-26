package io.openim.android.ouicore.utils;

public class Routes {
    public static class Main {
        //发送验证信息
        public final static String SEND_VERIFY = "/main/SendVerifyActivity";
    }

    //会话相关
    public static class Conversation {
        //会话列表
        public final static String CONTACT_LIST = "/conversation/ContactListFragment";
        //聊天页
        public final static String CHAT = "/conversation/ChatActivity";
    }

    //群组
    public static class Group {
        //搜索详情
        public final static String DETAIL = "/group/GroupDetailActivity";
    }

    //关系链
    public static class Contact {
        public final static String HOME = "/contact/ContactFragment";
    }
}
