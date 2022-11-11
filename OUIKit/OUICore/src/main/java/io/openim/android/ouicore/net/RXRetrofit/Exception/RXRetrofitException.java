package io.openim.android.ouicore.net.RXRetrofit.Exception;


public class RXRetrofitException extends Exception {

    private static final long serialVersionUID = 114946L;

//10001 : 请求参数错误
//10002 ：数据库错误
//10003 : 服务器错误
//10006 : 记录不存在
//        注册相关
//20001 ：账号已注册
//20002 ：重复发送验证码
//20003 ：邀请码错误
//20004 : 注册IP受限
//        验证码相关
//30001 ：验证码错误
//30002 : 验证码已过期
//30003 : 邀请码被使用
//30004 ：邀请码不存在
//        登录相关
//40001 ：账号未注册
//40002 : 密码错误
//40003 ：登录受ip限制
//40004 ：ip禁止注册登录
//        token相关
//50001 : 过期
//50002 : 格式错误
//50003 ：未生效
//50004 ：未知错误
//50005 ：创建错误

    public RXRetrofitException(int code, String errMsg) {
        super(getErrTips(code, errMsg));
    }

    private static String getErrTips(int code, String errMsg) {
        switch (code) {
            case 10001:
                return "请求参数错误";
            case 10002:
                return "数据库错误";
            case 10003:
                return "服务器错误";
            case 10006:
                return "记录不存在";
            case 20001:
                return "账号已注册";
            case 20002:
                return "重复发送验证码";
            case 20003:
                return "邀请码错误";
            case 20004:
                return "注册IP受限";
            case 30001:
                return "验证码错误";
            case 30002:
                return "验证码已过期";
            case 30003:
                return "邀请码被使用";
            case 30004:
                return "邀请码不存在";
            case 40001:
                return "账号未注册";
            case 40002:
                return "密码错误";
            case 40003:
                return "登录受ip限制";
            case 40004:
                return "ip禁止注册登录";
            case 50001:
                return "token过期";
            case 50002:
                return "token格式错误";
            case 50003:
                return "token未生效";
            case 50004:
                return "未知错误";
            case 50005:
                return "创建错误";
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
