package io.openim.android.ouicore.entity;

import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.Message;

//解析最后的消息
public class MsgConversation {
    public CharSequence lastMsg;
    public ConversationInfo conversationInfo;
    public NotificationMsg notificationMsg;

    public MsgConversation(Message lastMsg, ConversationInfo conversationInfo) {
        this.lastMsg = IMUtil.getMsgParse(lastMsg);
        this.conversationInfo = conversationInfo;

        try {
            if (lastMsg.getContentType() == MessageType.GROUP_INFO_SET_NTF) {
                notificationMsg = GsonHel.fromJson(lastMsg.getNotificationElem().getDetail(),
                    NotificationMsg.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
