package io.openim.android.ouicore.services;

import androidx.lifecycle.LifecycleOwner;

import com.alibaba.android.arouter.facade.template.IProvider;

import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.sdk.models.ConversationInfo;

public interface IConversationBridge extends IProvider {
    void deleteConversationFromLocalAndSvr(String groupID);

    void closeChatPage();

    ConversationInfo getConversationInfo();
}
