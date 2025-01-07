package io.openim.android.ouicore.utils;

import android.text.TextUtils;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.im.IM;

public class Constants {

    public static final String DEFAULT_HOST = "your-server-ip or your-domain";
    private static final String APP_AUTH = "http://" + DEFAULT_HOST + ":10008/";
    private static final String IM_API = "http://" + DEFAULT_HOST + ":10002";
    private static final String IM_WS = "ws://" + DEFAULT_HOST + ":10001";

    public static String getHost() {
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("DEFAULT_IP");
        if (TextUtils.isEmpty(url)) return DEFAULT_HOST;
        return url;
    }

    public static String getImApiUrl() {
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("IM_API_URL");
        if (TextUtils.isEmpty(url)) return IM_API;
        return url;
    }


    public static String getAppAuthUrl() {
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("APP_AUTH_URL");
        if (TextUtils.isEmpty(url)) return APP_AUTH;
        return url;
    }

    public static String getImWsUrl() {
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("IM_WS_URL");
        if (TextUtils.isEmpty(url)) return IM_WS;
        return url;
    }

    public static String getStorageType() {
        String url = SharedPreferencesUtil.get(BaseApp.inst()).getString("STORAGE_TYPE");
        if (TextUtils.isEmpty(url)) return "minio";
        return url;
    }

    public static String getLogLevel() {
        String level = SharedPreferencesUtil.get(BaseApp.inst()).getString(Constants.K_LOG_LEVEL);
        if (TextUtils.isEmpty(level)) return "3";
        return level;
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
    //日志级别
    public static final String K_LOG_LEVEL = "logLevel";
    // 阅后即焚存储标识
    public static final String SP_Prefix_ReadVanish = "ReadVanish_";


    //加载中
    public static final int LOADING = 201;

    public static class MsgType {
        //本地呼叫记录
        public static final int LOCAL_CALL_HISTORY = -110;
        public static final int callingInvite = 200;
        public static final int callingAccept = 201;
        public static final int callingReject = 202;
        public static final int callingCancel = 203;
        public static final int callingHungup = 204;
    }

    public static class MediaType {
        public static final String VIDEO = "video";
        public static final String AUDIO = "audio";
    }

    public static class ActivityResult {
        public static final int SET_REMARK = 1000000;
        public static final int DELETE_FRIEND = 1000001;
    }
}
