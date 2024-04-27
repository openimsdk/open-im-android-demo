package io.openim.android.ouicore.net.RXRetrofit.Exception;


import android.content.Context;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;

public class RXRetrofitException extends Exception {

    private static final long serialVersionUID = 114946L;


    public RXRetrofitException(int code, String errMsg) {
        super(getErrTips(code, errMsg));
    }

    //"1101" = "账号不存在";
//"20001" = "密码错误";
//"20002" = "账号不存在";
//"20003" = "手机号已注册";
//"20004" = "账号已注册";
//"20005" = "频繁获取验证码";
//"20006" = "验证码错误";
//"20007" = "验证码过期";
//"20008" = "验证码失败次数过多";
//"20009" = "验证码已经使用";
//"20010" = "邀请码已经使用";
//"20011" = "邀请码不存在";
//"20012" = "被禁止登录注册";
//"20013" = "拒绝添加好友";
//"20014" = "邮箱已注册";
    private static String getErrTips(int code, String errMsg) {
        Context context = BaseApp.inst();
        switch (code) {
            case 1101:
            case 20002:
                return context
                    .getString(R.string.account_not_exist);
            case 20001:
                return context.getString(R.string.wrong_password);
            case 20003:
                return context.getString(R.string.phone_num_registered);
            case 20004:
                return context.getString(R.string.account_registered);
            case 20005:
                return context.getString(R.string.get_codes_frequently);
            case 20006:
                return context.getString(R.string.vq_err);
            case 20007:
                return context.getString(R.string.vq_expired);
            case 20008:
                return context.getString(R.string.vq_failed_num_too_many);
            case 20009:
                return context.getString(R.string.vq_used);
            case 20010:
                return context.getString(R.string.invite_used);
            case 20011:
                return context.getString(R.string.invite_not_exist);
            case 20012:
                return context.getString(R.string.restrict_login_registration);
            case 20013:
                return context.getString(R.string.decline_add);
            case 20014:
                return context.getString(R.string.emil_registered);
        }
        return errMsg;
    }

    public RXRetrofitException(String message) {
        super(message);
    }

    public RXRetrofitException(String message, Throwable cause) {
        super(message, cause);
    }

    public RXRetrofitException(Throwable cause) {
        super(cause);
    }

}
