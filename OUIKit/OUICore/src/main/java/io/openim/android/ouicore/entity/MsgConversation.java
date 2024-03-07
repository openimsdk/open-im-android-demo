package io.openim.android.ouicore.entity;

import android.text.SpannableStringBuilder;

import androidx.annotation.Nullable;

import java.util.Objects;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.enums.GroupAtType;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.enums.Opt;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.Message;

//解析最后的消息
public class MsgConversation {
    public CharSequence lastMsg;
    public ConversationInfo conversationInfo;
    public NotificationMsg notificationMsg;

    public MsgConversation(Message lastMsg, ConversationInfo conversationInfo) {
        try {
            this.lastMsg = "";
            if (null != lastMsg) {
                IMUtil.buildExpandInfo(lastMsg);
                this.lastMsg = IMUtil.getMsgParse(lastMsg,
                    conversationInfo.getConversationType()
                        == ConversationType.SUPER_GROUP_CHAT);
            }
            this.conversationInfo = conversationInfo;
            if (conversationInfo.getGroupAtType() != GroupAtType.AT_NORMAL) {
                this.lastMsg = IMUtil.getPrefixTag(this.lastMsg,
                    conversationInfo);
            }
            if (lastMsg.getContentType() == MessageType.GROUP_INFO_SET_NTF) {
                notificationMsg = GsonHel.fromJson(lastMsg.getNotificationElem().getDetail(),
                    NotificationMsg.class);
            }
            if (conversationInfo.getRecvMsgOpt() != Opt.NORMAL
                && conversationInfo.getUnreadCount()>0) {
                this.lastMsg =
                    new SpannableStringBuilder("[" + conversationInfo.getUnreadCount() + BaseApp.inst()
                        .getString(io.openim.android.ouicore.R.string.count)+ "] ")
                    .append(this.lastMsg);
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
