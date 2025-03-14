package io.openim.android.ouicore.entity;

import io.openim.android.sdk.models.FileElem;
import io.openim.android.sdk.models.PictureElem;
import io.openim.android.sdk.models.SoundElem;
import io.openim.android.sdk.models.VideoElem;

public class OANotification {
    /// 标题
    public String notificationName;

    /// 头像
    public String notificationFaceURL;

    /// 类型
    public int notificationType;

    /// 文本内容
    public String text;

    /// 跳转链接
    public String externalUrl;

    /// 0：纯文字通知 1：文字+图片通知 2：文字+视频通知 3：文字+文件通知
    public int mixType;

    /// 图片信息
    public PictureElem pictureElem;

    /// 语音信息
    public SoundElem soundElem;

    /// 视频信息
    public VideoElem videoElem;

    /// 文件信息
    public FileElem fileElem;

    /// 扩展字段
    public String ex;

}
