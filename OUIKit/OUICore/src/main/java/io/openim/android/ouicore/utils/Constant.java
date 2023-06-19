package io.openim.android.ouicore.utils;

import android.text.TextUtils;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.im.IM;

public class Constant {
//            public static final String DEFAULT_IP = "test-web.rentsoft.cn";//43
    public static final String DEFAULT_IP = "web.rentsoft.cn";//121
//    public static final String DEFAULT_IP = "121.196.195.35";//121

    //登录注册手机验 证服务器地址
    private static final String APP_AUTH_URL = "https://" + DEFAULT_IP + "/chat/";
    //IM sdk api地址
    private static final String IM_API_URL = "https://" + DEFAULT_IP + "/api/";
    //web socket
    private static final String IM_WS_URL = "wss://" + DEFAULT_IP + "/msg_gateway";
    //admin Manage
    private static final String ADMIN_MANAGE = "https://" + DEFAULT_IP + "/complete_admin/";


    public static String getAdminManage() {
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("ADMIN_MANAGE");
        if (TextUtils.isEmpty(url)) return ADMIN_MANAGE;
        return url;
    }

    public static String getImApiUrl() {
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("IM_API_URL");
        if (TextUtils.isEmpty(url)) return IM_API_URL;
        return url;
    }

    public static String getAppAuthUrl() {
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("APP_AUTH_URL");
        if (TextUtils.isEmpty(url)) return APP_AUTH_URL;
        return url;
    }

    public static String getImWsUrl() {
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("IM_WS_URL");
        if (TextUtils.isEmpty(url)) return IM_WS_URL;
        return url;
    }

    public static String getStorageType() {
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("STORAGE_TYPE");
        if (TextUtils.isEmpty(url)) return "minio";
        return url;
    }

    //存储音频的文件夹
    public static final String AUDIO_DIR = IM.getStorageDir() + "/audio/";
    //视频存储文件夹
    public static final String VIDEO_DIR = IM.getStorageDir() + "/video/";
    //图片存储文件夹
    public static final String PICTURE_DIR = IM.getStorageDir() + "/picture/";
    //文件夹
    public static final String File_DIR = IM.getStorageDir() + "/file/";

    //二维码
    public static class QR {
        public static final String QR_ADD_FRIEND = "io.openim.app/addFriend";
        public static final String QR_JOIN_GROUP = "io.openim.app/joinGroup";
    }


    public static class Event {
        //会话列表初始
        public static final int CONTACT_LIST_VM_INIT = 10001;
        //转发选人
        public static final int FORWARD = 10002;
        //音视频通话
        public static final int CALLING_REQUEST_CODE = 10003;
        //用户信息更新
        public static final int USER_INFO_UPDATE = 10004;
        //设置背景
        public static final int SET_BACKGROUND = 10005;
        //群信息更新
        public static final int UPDATE_GROUP_INFO = 10006;
        //设置群通知
        public static final int SET_GROUP_NOTIFICATION = 10007;
        //插入了消息到本地
        public static final int INSERT_MSG = 10008;
    }

    public static final String K_ID = "Id";
    public static final String K_GROUP_ID = "group_id";
    public static final String K_IS_PERSON = "is_person";
    public static final String K_NOTICE = "notice";
    public static final String K_NAME = "name";
    public static final String K_RESULT = "result";
    public static final String K_RESULT2 = "result2";
    public static final String K_FROM = "from";
    public static final String K_SIZE = "size";

    //最大通话人数
    public static final int MAX_CALL_NUM = 9;
    //好友红点
    public static final String K_FRIEND_NUM = "k_friend_num";
    //群红点
    public static final String K_GROUP_NUM = "k_group_num";
    public static final String K_SET_BACKGROUND = "set_background";

    //邀请入群
    public static final String IS_INVITE_TO_GROUP = "isInviteToGroup";
    //移除群聊
    public static final String IS_REMOVE_GROUP = "isRemoveGroup";
    //选择群成员
    public static final String IS_SELECT_MEMBER = "isSelectMember";
    //选择好友
    public static final String IS_SELECT_FRIEND = "isSelectFriend";

    /**
     * 发送状态
     */
    public static class Send_State {
        //发送中...
        public static final int SENDING = 1;
        //发送成功
        public static final int SEND_SUCCESS = 2;
        //发送失败
        public static final int SEND_FAILED = 3;
    }


    //加载中
    public static final int LOADING = 201;

    public static class MsgType {
        //本地呼叫记录
        public static final int LOCAL_CALL_HISTORY = -110;
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
        //            * 108:名片消息<br/>
        public static final int CARD = 108;
        //            * 109:位置消息<br/>
        public static final int LOCATION = 109;
        //            * 110:自定义消息<br/>
        public static final int CUSTOMIZE = 110;
        //会议邀请
        public static final int CUSTOMIZE_MEETING = 905;
        //            * 111:撤回消息回执<br/>
        public static final int REVOKE = 111;
        //2.3.0版本的消息撤回类型，兼容单人撤回，群撤回，新增群主管理员撤回成员信息
        public static final int ADVANCED_REVOKE = 118;
        //            * 112:C2C已读回执<br/>
        public static final int ALREADY_READ = 112;
        //            * 113:正在输入状态
        public static final int TYPING = 113;
        /// 引用回复
        public static final int QUOTE = 114;
        //自定义表情
        public static final int CUSTOM_EMOJI = 115;
        //通知消息一般大于1200
        public static final int NOTICE = 1200;
        //后台发送过来的通知
        public static final int OA_NOTICE = 1400;
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

    /**
     * 群身份
     */
    public static class RoleLevel {
        public static final int MEMBER = 1;
        public static final int GROUP_OWNER = 2;
        public static final int ADMINISTRATOR = 3;
    }

    public static class MsgNotification {
        /// 好友已添加
        public static final int friendAddedNotification = 1204;

        /// 群已被创建
        public static final int groupCreatedNotification = 1501;

        /// 群资料改变
        public static final int groupInfoSetNotification = 1502;

        /// 进群申请
        public static final int joinGroupApplicationNotification = 1503;

        /// 群成员退出
        public static final int memberQuitNotification = 1504;

        /// 群申请被接受
        public static final int groupApplicationAcceptedNotification = 1505;

        /// 群申请被拒绝
        public static final int groupApplicationRejectedNotification = 1506;

        /// 群拥有者权限转移
        public static final int groupOwnerTransferredNotification = 1507;

        /// 群成员被踢出群
        public static final int memberKickedNotification = 1508;

        /// 邀请进群
        public static final int memberInvitedNotification = 1509;

        /// 群成员进群
        public static final int memberEnterNotification = 1510;

        /// 解散群
        public static final int dismissGroupNotification = 1511;

        public static final int groupNotificationEnd = 1599;

        /// 群成员被禁言
        public static final int groupMemberMutedNotification = 1512;

        /// 群成员被取消禁言
        public static final int groupMemberCancelMutedNotification = 1513;

        /// 群禁言
        public static final int groupMutedNotification = 1514;

        /// 取消群禁言
        public static final int groupCancelMutedNotification = 1515;

        /// 阅后即焚
        public static final int burnAfterReadingNotification = 1701;
        /// 群成员信息改变
        public static final int groupMemberInfoChangedNotification = 1516;
    }

    public static class CacheKey {
    }

    public static class GroupStatus {
        //        0正常，1被封，2解散，3禁言
        public static final int status0 = 0;
        public static final int status1 = 1;
        public static final int status2 = 2;
        public static final int status3 = 3;
    }

    /// 进群验证设置选项
    public static class GroupVerification {
        /// 申请需要同意 邀请直接进
        public static final int applyNeedVerificationInviteDirectly = 0;

        /// 所有人进群需要验证，除了群主管理员邀
        public static final int allNeedVerification = 1;

        /// 直接进群
        public static final int directly = 2;
    }

    public static class GroupType {
        /// 普通群
        public static final int general = 0;

        /// 工作群
        public static final int work = 2;
    }

    //超级群
    public static final int SUPER_GROUP_LIMIT = 250;

    public static class MediaType {
        public static final String VIDEO = "video";
        public static final String AUDIO = "audio";
    }
}
