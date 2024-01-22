package io.openim.android.ouicore.utils;

import android.text.TextUtils;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.im.IM;

public class Constant {

    //    public static final String DEFAULT_HOST = "test-web.rentsoft.cn";//43
//    public static final String DEFAULT_HOST = "web.rentsoft.cn";//121
    public static final String DEFAULT_HOST = "14.29.213.197";

    //登录注册手机验 证服务器地址
    private static final String APP_AUTH_URL = "https://" + DEFAULT_HOST + "/chat/";
    //IM sdk api地址
    private static final String IM_API_URL = "https://" + DEFAULT_HOST + "/api";
    //web socket
    private static final String IM_WS_URL = "wss://" + DEFAULT_HOST + "/msg_gateway";

    //--------IP----------
    private static final String APP_AUTH = "http://" + DEFAULT_HOST + ":50008/";
    private static final String IM_API = "http://" + DEFAULT_HOST + ":50002";
    private static final String IM_WS = "ws://" + DEFAULT_HOST + ":50001";
    //--------------------

    public static boolean getIsIp() {
        return SharedPreferencesUtil.get(BaseApp.inst()).getBoolean("IS_IP", true);
    }

    public static String getHost() {
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("DEFAULT_IP");
        if (TextUtils.isEmpty(url)) return DEFAULT_HOST;
        return url;
    }

    public static String getImApiUrl() {
        boolean isIp = getIsIp();
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("IM_API_URL");
        if (TextUtils.isEmpty(url)) return isIp ? IM_API : IM_API_URL;
        return url;
    }


    public static String getAppAuthUrl() {
        boolean isIp = getIsIp();
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("APP_AUTH_URL");
        if (TextUtils.isEmpty(url)) return isIp ? APP_AUTH : APP_AUTH_URL;
        return url;
    }

    public static String getImWsUrl() {
        boolean isIp = getIsIp();
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("IM_WS_URL");
        if (TextUtils.isEmpty(url)) return isIp ? IM_WS : IM_WS_URL;
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
        //群解散了
        public static final int DISSOLVE_GROUP =10009 ;
    }

    public static final String K_ID = "Id";
    public static final String K_GROUP_ID = "group_id";
    public static final String K_IS_PERSON = "is_person";
    public static final String K_NOTICE = "notice";
    public static final String K_NAME = "name";
    public static final String K_DATA = "data";
    public static final String K_RESULT = "result";
    public static final String K_RESULT2 = "result2";
    public static final String K_FROM = "from";
    public static final String K_SIZE = "size";
    //语言
    public static final String K_LANGUAGE_SP = "language_sp";
    //上一次登录类型 0手机号 1邮箱
    public static final String K_LOGIN_TYPE = "k_login_type";
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
    //@成员
    public static final String IS_AT_MEMBER = "isAtMember";
    //群通话
    public static final String IS_GROUP_CALL = "isGroupCall";
    //选择好友
    public static final String IS_SELECT_FRIEND = "isSelectFriend";
    //自定义消息类型
    public static final String K_CUSTOM_TYPE = "customType";


    //加载中
    public static final int LOADING = 201;

    public static class MsgType {
        //本地呼叫记录
        public static final int LOCAL_CALL_HISTORY = -110;
        //会议邀请
        public static final int CUSTOMIZE_MEETING = 905;
    }

    public static class MediaType {
        public static final String VIDEO = "video";
        public static final String AUDIO = "audio";
    }
}
