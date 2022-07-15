package io.openim.android.ouicore.entity;

import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.Message;

//解析最后的消息
public class MsgConversation {
    public Message lastMsg;
    public ConversationInfo conversationInfo;
    public NotificationMsg notificationMsg;

    public MsgConversation(Message lastMsg, ConversationInfo conversationInfo) {
        this.lastMsg = IMUtil.buildExpandInfo(lastMsg);
        this.conversationInfo = conversationInfo;

        try {
            if (lastMsg.getContentType() == Constant.MsgType.BULLETIN) {
                notificationMsg = GsonHel.fromJson(lastMsg.getNotificationElem().getDetail(),
                    NotificationMsg.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
