package io.openim.android.ouicore.im;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.openim.android.ouicore.entity.MsgConversation;
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
     *  设置时间显示
     * @param list
     * @return
     */
   public static List<Message> calChatTimeInterval(List<Message> list) {
        Message first = list.get(0);
        long milliseconds = first.getSendTime();
        first.setExt(true);
        long lastShowTimeStamp = milliseconds;
        for (int i = 1; i < list.size(); i++) {
            int index = i + 1;
            if (index <= list.size() - 1) {
                if (list.get(index).getSendTime() - lastShowTimeStamp > (1000 * 60 * 5)) {
                    lastShowTimeStamp = milliseconds;
                    list.get(index).setExt(true);
                }
            }
        }
        return list;
    }
}
