package io.openim.android.ouicore.vm;

import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.Message;

public class ForwardVM extends BaseVM {
    public Message forwardMsg;
    //转发的内容 用于显示
    public String tips;

    public void createForwardMessage(Message message) {
        reset();
        tips = IMUtil.getMsgParse(message).toString();
        forwardMsg = OpenIMClient.getInstance().messageManager.createForwardMessage(message);
    }

    public void reset() {
        forwardMsg = null;
        tips="";
    }
}
