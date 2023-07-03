package io.openim.android.ouicore.services;

import androidx.lifecycle.LifecycleOwner;

import com.alibaba.android.arouter.facade.template.IProvider;

import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.Message;

public interface IConversationBridge extends IProvider {
    void deleteConversationFromLocalAndSvr(String groupID);

    void closeChatPage();


    void setConversationRecvMessageOpt(int status, String cid);

    void setNotDisturbStatusListener(LifecycleOwner owner, IMUtil.OnSuccessListener<Integer> onSuccessListener);

    ConversationInfo getConversationInfo();

    void pinConversation(ConversationInfo conversationInfo, boolean is);

    void setConversationInfoChangeListener(LifecycleOwner owner, IMUtil.OnSuccessListener<ConversationInfo> onSuccessListener);

    void clearCHistory(String id);
}
