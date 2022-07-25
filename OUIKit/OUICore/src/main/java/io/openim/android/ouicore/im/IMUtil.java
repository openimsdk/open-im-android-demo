package io.openim.android.ouicore.im;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.AtMsgInfo;
import io.openim.android.ouicore.entity.LocationInfo;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.Message;

public class IMUtil {
    //android PlatformID 2
    public static final int PLATFORM_ID = 2;

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

    public static Message createMergerMessage(boolean isSingleChat, String otherSideName, List<Message> list) {
        String title = "";
        List<String> summaryList = new ArrayList<>();
        for (Message message : list) {
            summaryList.add(message.getSenderNickname() + ":" + getMsgParse(message));
            if (summaryList.size() >= 2) break;
        }
        if (isSingleChat) {
            title = LoginCertificate.getCache(BaseApp.inst()).nickname
                + BaseApp.inst().getString(R.string.and) + otherSideName
                + BaseApp.inst().getString(R.string.chat_history);
        } else {
            title = BaseApp.inst().getString(R.string.group_chat_history);
        }

        return OpenIMClient.getInstance().messageManager.createMergerMessage(list, title, summaryList);
    }

    /**
     * 解析扩展信息 避免在bindView时解析造成卡顿
     *
     * @param msg
     */
    public static Message buildExpandInfo(Message msg) {
        MsgExpand msgExpand = (MsgExpand) msg.getExt();
        if (null == msgExpand)
            msgExpand = new MsgExpand();
        try {
            if (msg.getContentType() == Constant.MsgType.LOCATION)
                msgExpand.locationInfo = GsonHel.fromJson(msg.getLocationElem().getDescription(), LocationInfo.class);
            if (msg.getContentType() == Constant.MsgType.MENTION)
                msgExpand.atMsgInfo = GsonHel.fromJson(msg.getContent(), AtMsgInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        msg.setExt(msgExpand);
        return msg;
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
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.picture) + "]";
                break;
            case Constant.MsgType.VOICE:
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.voice) + "]";
                break;
            case Constant.MsgType.VIDEO:
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.video) + "]";
                break;
            case Constant.MsgType.FILE:
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.file) + "]";
                break;
            case Constant.MsgType.LOCATION:
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.location) + "]";
                break;
            case Constant.MsgType.MENTION:
                lastMsg = ((MsgExpand) msg.getExt()).atMsgInfo.text;
                break;
            case Constant.MsgType.MERGE:
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.chat_history2) + "]";
                break;
        }
        return lastMsg;
    }
}
