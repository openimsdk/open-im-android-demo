package io.openim.android.ouicore.entity;

import android.text.SpannableStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * message 的 ext
 */
public class MsgExpand {
    public MsgExpand() {
    }
    //多选被选中
    public boolean isChoice;
    //此item 应该显示时间
    public boolean isShowTime;
    //地址信息（发送位置才有）
    public LocationInfo locationInfo;
    //at消息（at人才有）
    public AtMsgInfo atMsgInfo;
    //用于在消息输入框监听删除键时 判断删除对应@的人
    public int spanHashCode;
    //富文本（at 消息、表情）
    public transient SpannableStringBuilder sequence;
    //oa 通知
    public OANotification oaNotification;
    //群公告、消息通知
    public NotificationMsg notificationMsg;
    //普通通知 富文本
    public CharSequence tips;
    //阅后即焚倒计时
    public int readVanishNum;

    // ---呼叫记录---
    public CallHistory callHistory;
    // 呼叫时长
    public String callDuration;
    // ------

    //会议邀请信息
    public MeetingInfo meetingInfo;
    //自定义表情
    public CustomEmojiEntity customEmoji;
    //发送消息进度
    public long sendProgress=0;
}
