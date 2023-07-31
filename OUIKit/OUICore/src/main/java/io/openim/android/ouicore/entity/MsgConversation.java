package io.openim.android.ouicore.entity;

import androidx.annotation.Nullable;

import java.util.Objects;

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
        try {
            if (null != lastMsg) {
                IMUtil.buildExpandInfo(lastMsg);
                this.lastMsg = IMUtil.getMsgParse(lastMsg);
            } else
                this.lastMsg = "";
            this.conversationInfo = conversationInfo;
            if (lastMsg.getContentType() == MessageType.GROUP_INFO_SET_NTF) {
                notificationMsg = GsonHel.fromJson(lastMsg.getNotificationElem().getDetail(),
                    NotificationMsg.class);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MsgConversation)) return false;
        MsgConversation that = (MsgConversation) o;
        return conversationInfo.equals(that.conversationInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationInfo.getConversationID());
    }
}
