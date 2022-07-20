package io.openim.android.ouicore.utils;

public class Routes {
    public static class Service {
        public final static String CALLING = "/Service/CallingServiceImp";
    }

    public static class Main {
        //发送验证信息
        public final static String SEND_VERIFY = "/main/SendVerifyActivity";
        //好友详情
        public final static String PERSON_DETAIL = "/main/PersonDetailActivity";
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
        //创建群组
        public final static String CREATE_GROUP = "/group/CreateGroupActivity";
        //群资料
        public final static String MATERIAL = "/group/GroupMaterialActivity";
        //通知详情
        public final static String NOTICE_DETAIL = "/group/NoticeDetailActivity";
        //分享二维码
        public static final String SHARE_QRCODE = "/group/ShareQrcodeActivity";
    }

    //关系链
    public static class Contact {
        public final static String HOME = "/contact/ContactFragment";
        public final static String FORWARD = "/contact/ForwardToActivity";
    }

}
