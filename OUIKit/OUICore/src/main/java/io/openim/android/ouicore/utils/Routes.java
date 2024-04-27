package io.openim.android.ouicore.utils;

public class Routes {
    public static class Applet {
        public final static String HOME = "/Applet/AppletFragment";
    }

    public static class Service {
        public final static String CALLING = "/CallingService/CallingServiceImp";
        public static final String CONVERSATION = "/ConversationService/IBridgeImpl";
        public static final String MOMENTS = "/MomentsService/IBridgeImpl";
        public static final String MEETING = "/MeetingService/IBridgeImpl";
    }

    public static class Main {
        //发送验证信息
        public final static String SEND_VERIFY = "/main/SendVerifyActivity";
        //好友详情
        public final static String PERSON_DETAIL = "/main/PersonDetailActivity";
        //首页
        public final static String HOME = "/main/MainActivity";

        public final static String SPLASH = "/main/SplashActivity";

        //添加
        public static final String ADD_CONVERS = "/main/AddConversActivity";

        public static final String CALL_HISTORY = "/main/CallHistoryActivity";
        //搜索id
        public static final String SEARCH_CONVER = "/main/SearchContactActivity";
    }

    //会话相关
    public static class Conversation {
        //会话列表
        public final static String CONTACT_LIST = "/conversation/ContactListFragment";
        //聊天页
        public final static String CHAT = "/conversation/ChatActivity";

        public static final String CHAT_HISTORY = "/conversation/ChatHistorySearchActivity";
        //全局搜索
        public final static String SEARCH = "/conversation/SearchActivity";
        //预览图片或视频
        public static final String PREVIEW = "/conversation/PreviewActivity";
        //拍摄照片或视频
        public static final String SHOOT = "/conversation/ShootActivity";
        //多媒体聊天记录
        public static final String MEDIA_HISTORY = "/conversation/MediaHistoryActivity";
        //文件记录
        public static final String FILE_HISTORY = "/conversation/FileHistoryActivity";
    }

    //群组
    public static class Group {
        //搜索详情
        public final static String DETAIL = "/group/GroupDetailActivity";
        //创建群组
        public final static String CREATE_GROUP = "/group/InitiateGroupActivity";
        public final static String CREATE_GROUP2 = "/group/CreateGroupActivity";
        public final static String SELECT_TARGET = "/group/SelectTargetActivityV3";
        //群资料
        public final static String MATERIAL = "/group/GroupMaterialActivity";
        //通知详情
        public final static String NOTICE_DETAIL = "/group/NoticeDetailActivity";
        //分享二维码
        public static final String SHARE_QRCODE = "/group/ShareQrcodeActivity";

        public static final String SUPER_GROUP_MEMBER = "/group/SuperGroupMemberActivity";
        //群公告
        public static final String GROUP_BULLETIN="/group/GroupBulletinActivity";
    }

    //关系链
    public static class Contact {
        public final static String HOME = "/contact/ContactFragment";
        public final static String FORWARD = "/contact/ForwardToActivity";
        public static final String ALL_FRIEND = "/contact/AllFriendActivity";
        public static final String SEARCH_GROUP_MEMBER = "/contact/SearchGroupMember";
        public static final String SEARCH_FRIENDS_GROUP = "/contact/SearchGroupAndFriendsActivity";
    }

    //朋友圈
    public static class Moments {
        public static final String HOME = "/moments/MomentsActivity";
        public static final String ToUserMoments = "/moments/ToUserMomentsActivity";
    }

    //会议
    public static class Meeting {
        public static final String LAUNCH = "/meeting/MeetingLaunchActivity";
        public static final String HOME = "/meeting/MeetingHomeActivity";

    }
}
