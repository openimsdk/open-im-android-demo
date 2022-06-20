package io.openim.android.ouiconversation.utils;

public class Constant {

    //加载中
    public static final int LOADING=201;

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
        //通知消息一般大于1500
        public static final int NOTICE = 1500;
        //            * 111:撤回消息回执<br/>
        public static final int REVOKE = 111;
        //            * 112:C2C已读回执<br/>
        public static final int ALREADY_READ = 112;
        //            * 113:正在输入状态
        public static final int TYPING = 113;
    }

}
