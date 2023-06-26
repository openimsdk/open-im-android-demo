package io.openim.android.ouicore.vm;

import java.util.List;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.Message;

public class ForwardVM extends BaseVM {
    public Message forwardMsg;
    //转发的内容 用于显示
    public String tips;
    //留言
    public Message leaveMsg;

    public void createLeaveMsg(String leave) {
        leaveMsg = OpenIMClient.getInstance().messageManager.createTextMessage(leave);
    }

    public void createForwardMessage(Message message) {
        reset();
        tips = IMUtil.getMsgParse(message).toString();
        forwardMsg = OpenIMClient.getInstance().messageManager.createForwardMessage(message);
    }

    public void createMergerMessage(boolean isSingleChat, String showName, List<Message> messages) {
        reset();
        if (isSingleChat) {
            tips =
                LoginCertificate.getCache(BaseApp.inst()).nickname + BaseApp.inst().getString(R.string.and) + showName + BaseApp.inst().getString(R.string.chat_history);
        } else {
            tips = BaseApp.inst().getString(R.string.group_chat_history);
        }
        forwardMsg = IMUtil.createMergerMessage(tips, messages);
    }

    public void reset() {
        forwardMsg = null;
        leaveMsg=null;
        tips="";
    }
}
