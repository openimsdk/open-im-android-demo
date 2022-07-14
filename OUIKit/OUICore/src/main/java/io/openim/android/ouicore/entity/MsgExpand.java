package io.openim.android.ouicore.entity;

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
    //at消息（at别人才有）
    public AtMsgInfo atMsgInfo;
    //用于在消息输入框监听删除键时 判断删除对应@的人
    public int spanHashCode;
}
