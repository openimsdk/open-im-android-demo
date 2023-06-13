package io.openim.android.ouicore.entity;

import android.content.Context;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;

public class LoginCertificate {
    public String nickname;
    public String faceURL;
    public String userID;
    public String imToken;
    public String chatToken;

    public boolean allowSendMsgNotFriend;
    /// discoverPageURL
    /// ordinaryUserAddFriend,
    /// bossUserID,
    /// adminURL ,
    /// needInvitationCodeRegister
    /// robots

    // 全局免打扰 0：正常；1：不接受消息；2：接受在线消息不接受离线消息；
    public int globalRecvMsgOpt;

    public void cache(Context context) {
        SharedPreferencesUtil.get(context).setCache("user.LoginCertificate",
            GsonHel.toJson(this));
    }

    public static LoginCertificate getCache(Context context) {
        String u = SharedPreferencesUtil.get(context).getString("user.LoginCertificate");
        if (u.isEmpty()) return null;
        return GsonHel.fromJson(u, LoginCertificate.class);
    }

    public static void clear() {
        SharedPreferencesUtil.remove(BaseApp.inst(),
            "user.LoginCertificate");
    }

}
