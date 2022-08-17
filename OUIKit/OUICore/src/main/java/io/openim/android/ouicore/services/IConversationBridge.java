package io.openim.android.ouicore.services;

import com.alibaba.android.arouter.facade.template.IProvider;

public interface IConversationBridge extends IProvider {
    void deleteConversationFromLocalAndSvr(String groupID);
    void closeChatPage();
}
