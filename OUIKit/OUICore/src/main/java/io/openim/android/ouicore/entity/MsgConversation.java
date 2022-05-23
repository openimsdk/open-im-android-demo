package io.openim.android.ouicore.entity;

import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.Message;

//解析最后的消息
public class MsgConversation{
    public Message lastMsg;
    public ConversationInfo conversationInfo;

    public MsgConversation(Message lastMsg, ConversationInfo conversationInfo) {
        this.lastMsg = lastMsg;
        this.conversationInfo = conversationInfo;
    }
}
