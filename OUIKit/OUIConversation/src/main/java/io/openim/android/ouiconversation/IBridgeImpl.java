package io.openim.android.ouiconversation;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouiconversation.vm.ContactListVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.services.IConversationBridge;
import io.openim.android.ouicore.utils.Routes;

@Route(path = Routes.Service.CONVERSATION)
public class IBridgeImpl implements IConversationBridge {

    @Override
    public void init(Context context) {

    }


    @Override
    public void deleteConversationFromLocalAndSvr(String groupID) {
        ContactListVM contactListVM = BaseApp.inst().getVMByCache(ContactListVM.class);
        if (null != contactListVM) {
            String conversationID = "";
            for (MsgConversation msgConversation : contactListVM.conversations.getValue()) {
                if (msgConversation.conversationInfo.getGroupID().equals(groupID))
                    conversationID = msgConversation.conversationInfo.getConversationID();
            }
            if (!conversationID.isEmpty())
                contactListVM.deleteConversationFromLocalAndSvr(conversationID);
        }

    }

    @Override
    public void closeChatPage() {
        ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
        if (null != chatVM) {
            chatVM.closePage();
        }
    }
}
