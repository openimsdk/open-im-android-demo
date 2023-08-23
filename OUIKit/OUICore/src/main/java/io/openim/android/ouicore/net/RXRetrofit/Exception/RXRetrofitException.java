package io.openim.android.ouicore.net.RXRetrofit.Exception;


public class RXRetrofitException extends Exception {

    private static final long serialVersionUID = 114946L;


    public RXRetrofitException(int code, String errMsg) {
        super(getErrTips(code, errMsg));
    }

//    ErrPassword                 = errs.NewCodeError(20001, "PasswordError")            // 密码错误
//    ErrAccountNotFound          = errs.NewCodeError(20002, "AccountNotFound")          // 账号不存在
//    ErrPhoneAlreadyRegister     = errs.NewCodeError(20003, "PhoneAlreadyRegister")     // 手机号已经注册
//    ErrAccountAlreadyRegister   = errs.NewCodeError(20004, "AccountAlreadyRegister")   // 账号已经注册
//    ErrVerifyCodeSendFrequently = errs.NewCodeError(20005, "VerifyCodeSendFrequently") // 频繁获取验证码
//    ErrVerifyCodeNotMatch       = errs.NewCodeError(20006, "VerifyCodeNotMatch")       // 验证码错误
//    ErrVerifyCodeExpired        = errs.NewCodeError(20007, "VerifyCodeExpired")        // 验证码过期
//    ErrVerifyCodeMaxCount       = errs.NewCodeError(20008, "VerifyCodeMaxCount")       //
//    验证码失败次数过多
//    ErrVerifyCodeUsed           = errs.NewCodeError(20009, "VerifyCodeUsed")           // 已经使用
//    ErrInvitationCodeUsed       = errs.NewCodeError(20010, "InvitationCodeUsed")       // 邀请码已经使用
//    ErrInvitationNotFound       = errs.NewCodeError(20011, "InvitationNotFound")       // 邀请码不存在
//    ErrForbidden                = errs.NewCodeError(20012, "Forbidden")                // 限制登录注册
//    ErrRefuseFriend             = errs.NewCodeError(20013, "RefuseFriend")             // 拒绝添加好友
    private static String getErrTips(int code, String errMsg) {
        switch (code) {
            case 20001:
                return "密码错误";
            case 20002:
                return "账号不存在";
            case 20003:
                return "手机号已经注册";
            case 20004:
                return "账号已经注册";
            case 20005:
                return "频繁获取验证码";
            case 20006:
                return "验证码错误";
            case 20007:
                return "验证码过期";
            case 20008:
                return " 验证码失败次数过多";
            case 20009:
                return " 验证码已经使用";
            case 20010:
                return " 邀请码已经使用";
            case 20011:
                return " 邀请码不存在";
            case 20012:
                return " 限制登录注册";
            case 20013:
                return " 拒绝添加好友";
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
