package io.openim.android.ouicore.utils;

import io.openim.android.ouicore.im.IM;

public class Constant {
    public static final String DEFAULT_IP = "121.37.25.71";
//    public static final String DEFAULT_IP = "43.128.5.63";

    //IM sdk api地址
    public static final String IM_API_URL = "http://" + DEFAULT_IP + ":10002";
    //登录注册手机验 证服务器地址
    public static final String APP_AUTH_URL = "http://" + DEFAULT_IP + ":10004";
    //web socket
    public static final String IM_WS_URL = "ws://" + DEFAULT_IP + ":10001";

    //存储音频的文件夹
    public static final String AUDIODIR = IM.getStorageDir() + "/audio/";
    //视频存储文件夹
    public static final String VIDEODIR = IM.getStorageDir() + "/video/";
    //图片存储文件夹
    public static final String PICTUREDIR = IM.getStorageDir() + "/picture/";

    //二维码
    public static class QR {
        public static final String QR_ADD_FRIEND = "addFriend";
        public static final String QR_JOIN_GROUP = "joinGroup";
    }


    public static class Event {
        //已读数量变化
        public static final int READ_CHANGE = 10001;
        //转发选人
        public static final int FORWARD = 10002;
    }

    //会话类型
    public static class SessionType {
        public static final int SINGLE_CHAT = 1;
        public static final int GROUP_CHAT = 2;
    }

    public static final String K_ID = "Id";
    public static final String K_GROUP_ID = "group_id";
    public static final String K_IS_PERSON = "is_person";
    public static final String K_NOTICE = "notice";
    public static final String K_NAME = "name";
    public static final String K_CONVERSATION_ID = "conversationID";

    /**
     * 发送状态
     */
    public static class Send_State {
        //发送中...
        public static final int SENDING = 1;
        //发送失败
        public static final int SEND_FAILED = 3;
    }


    //加载中
    public static final int LOADING = 201;

    public static class MsgType {
        //            * 101:文本消息<br/>
        public static final int TXT = 101;
        //            * 102:图片消息<br/>
        public static final int PICTURE = 102;
        //            * 103:语音消息<br/>
        public static final int VOICE = 103;
        //            * 104:视频消息<br/>
        public static final int VIDEO = 104;
        //            * 105:文件消息<br/>'
        public static final int FILE = 105;
        //            * 106:@消息<br/>
        public static final int MENTION = 106;
        //            * 107:合并消息<br/>
        public static final int MERGE = 107;
        //            * 108:转发消息<br/>
        public static final int TRANSIT = 108;
        //            * 109:位置消息<br/>
        public static final int LOCATION = 109;
        //            * 110:自定义消息<br/>
        public static final int CUSTOMIZE = 110;
        //            * 111:撤回消息回执<br/>
        public static final int REVOKE = 111;
        //            * 112:C2C已读回执<br/>
        public static final int ALREADY_READ = 112;
        //            * 113:正在输入状态
        public static final int TYPING = 113;
        //通知消息一般大于1200
        public static final int NOTICE = 1200;
        //群公告
        public static final int BULLETIN = 1502;
    }


    /// 会话强提示内容
    public static class GroupAtType {
        /// 无提示
        public static final int atNormal = 0;

        /// @了我提示
        public static final int atMe = 1;

        /// @了所有人提示
        public static final int atAll = 2;

        /// @了所有人@了我
        public static final int atAllAtMe = 3;

        /// 群公告提示
        public static final int groupNotification = 4;
    }
}
