package io.openim.android.ouicore.im;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.Message;

public class IMUtil {
    /**
     * 会话排序比较器
     */
    public static Comparator<MsgConversation> simpleComparator() {
        return (a, b) -> {
            if ((a.conversationInfo.isPinned() && b.conversationInfo.isPinned()) ||
                (!a.conversationInfo.isPinned() && !b.conversationInfo.isPinned())) {
                long aCompare = Math.max(a.conversationInfo.getDraftTextTime(), a.conversationInfo.getLatestMsgSendTime());
                long bCompare = Math.max(b.conversationInfo.getDraftTextTime(), b.conversationInfo.getLatestMsgSendTime());
                return Long.compare(bCompare, aCompare);
            } else if (a.conversationInfo.isPinned() && !b.conversationInfo.isPinned()) {
                return -1;
            } else {
                return 1;
            }
        };
    }


    /**
     * 设置时间显示
     *
     * @param list
     * @return
     */
    public static List<Message> calChatTimeInterval(List<Message> list) {
        Message first = list.get(0);
        long lastShowTimeStamp = first.getSendTime();
        for (int i = 1; i < list.size(); i++) {
            Message message = list.get(i);
            if (lastShowTimeStamp - message.getSendTime() > (1000 * 60 * 5)) {
                lastShowTimeStamp = message.getSendTime();
                MsgExpand msgExpand = (MsgExpand) message.getExt();
                if (null == msgExpand) {
                    msgExpand = new MsgExpand();
                    message.setExt(msgExpand);
                }
                msgExpand.isShowTime = true;
            }
        }
        return list;
    }

    public static Message createMergerMessage(boolean isSingleChat, List<Message> list) {
        String title = "";
        List<String> summaryList = new ArrayList<>();
        for (Message message : list) {
            summaryList.add(message.getSenderNickname() + ":" + getMsgParse(message));
            if (summaryList.size() >= 2) break;
        }
        if (isSingleChat) {
            LoginCertificate loginCertificate = LoginCertificate.getCache(BaseApp.instance());
            String otherSideName = "";
            for (Message message : list) {
                if (!message.getSendID().equals(loginCertificate.userID)) {
                    otherSideName = message.getSenderNickname();
                }
            }
            title = LoginCertificate.getCache(BaseApp.instance()).nickname
                + BaseApp.instance().getString(R.string.and) + otherSideName
                + BaseApp.instance().getString(R.string.chat_history);
        } else {
            title = BaseApp.instance().getString(R.string.group_chat_history);
        }

        return OpenIMClient.getInstance().messageManager.createMergerMessage(list, title, summaryList);
    }

    /**
     * 解析消息内容
     *
     * @param msg
     * @return
     */
    public static String getMsgParse(Message msg) {
        String lastMsg = "";
        switch (msg.getContentType()) {
            default:
                lastMsg = msg.getNotificationElem().getDefaultTips();
                break;
            case Constant.MsgType.TXT:
                lastMsg = msg.getContent();
                break;
            case Constant.MsgType.PICTURE:
                lastMsg = "[" + BaseApp.instance().getString(io.openim.android.ouicore.R.string.picture) + "]";
                break;
            case Constant.MsgType.VOICE:
                lastMsg = "[" + BaseApp.instance().getString(io.openim.android.ouicore.R.string.voice) + "]";
                break;
            case Constant.MsgType.VIDEO:
                lastMsg = "[" + BaseApp.instance().getString(io.openim.android.ouicore.R.string.video) + "]";
                break;
            case Constant.MsgType.FILE:
                lastMsg = "[" + BaseApp.instance().getString(io.openim.android.ouicore.R.string.file) + "]";
                break;
            case Constant.MsgType.LOCATION:
                lastMsg = "[" + BaseApp.instance().getString(io.openim.android.ouicore.R.string.location) + "]";
                break;
            case Constant.MsgType.MENTION:
                lastMsg = "[" + BaseApp.instance().getString(io.openim.android.ouicore.R.string.chat_history2) + "]";
                break;
        }
        return lastMsg;
    }
}
