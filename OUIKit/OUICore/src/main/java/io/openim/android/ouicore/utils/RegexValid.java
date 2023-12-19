package io.openim.android.ouicore.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexValid {
    /**
     * 密码需要位6-18位包含字母数字
     *
     * @param password
     * @return
     */
    public static boolean isValidPassword(String password) {
        // 定义正则表达式
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$";

        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);

        // 使用正则表达式匹配密码
        Matcher matcher = pattern.matcher(password);

        // 返回匹配结果
        return matcher.matches();
    }

    /**
     * 全为数字
     * @param content
     * @return
     */
    public static boolean isAllNumber(String content) {
        String regex = "\\d+";
        return content.matches(regex);
    }

}
