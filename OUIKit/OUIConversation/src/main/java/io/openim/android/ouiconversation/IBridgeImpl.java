package io.openim.android.ouiconversation;

import android.content.Context;

import androidx.lifecycle.LifecycleOwner;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.vm.ContactListVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.services.IConversationBridge;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.sdk.models.ConversationInfo;

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

    @Override
    public void setConversationRecvMessageOpt(int status, String... cid) {
        ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
        if (null != chatVM)
            chatVM.setConversationRecvMessageOpt(status, cid);
    }

    @Override
    public void setNotDisturbStatusListener(LifecycleOwner owner, IMUtil.OnSuccessListener<Integer> onSuccessListener) {
        try {
            ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
            if (null != chatVM) {
                chatVM.notDisturbStatus.observe((LifecycleOwner) chatVM.getContext(), integer -> {
                    onSuccessListener.onSuccess(integer);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public ConversationInfo getConversationInfo() {
        try {
            ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
            if (null != chatVM) {
                return chatVM.conversationInfo.getValue();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    @Override
    public void pinConversation(ConversationInfo conversationInfo, boolean is) {
        ContactListVM contactListVM = BaseApp.inst().getVMByCache(ContactListVM.class);
        if (null != contactListVM) {
            contactListVM.pinConversation(conversationInfo, is);
        }
    }

    @Override
    public void setConversationInfoChangeListener(LifecycleOwner owner, IMUtil.OnSuccessListener<ConversationInfo> onSuccessListener) {
        ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
        if (null != chatVM) {
            chatVM.conversationInfo.observe(owner, conversationInfo -> {
                onSuccessListener.onSuccess(conversationInfo);
            });
        }
    }

    @Override
    public void clearCHistory(String id) {
        ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
        if (null != chatVM) {
            chatVM.clearCHistory(id);
        }
    }


}
