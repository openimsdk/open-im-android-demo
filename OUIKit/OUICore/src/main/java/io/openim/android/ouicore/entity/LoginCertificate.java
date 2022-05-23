package io.openim.android.ouicore.entity;

import android.content.Context;

import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;

public class LoginCertificate {
    public String nickname;
    public String userID;
    public String token;

    public void cache(Context context) {
        SharedPreferencesUtil.get(context).setCache("user.LoginCertificate", GsonHel.toJson(this));
    }

    public static LoginCertificate getCache(Context context) {
        String u = SharedPreferencesUtil.get(context).getString("user.LoginCertificate");
        if (u.isEmpty()) return null;
        return GsonHel.fromJson(u, LoginCertificate.class);
    }
}
