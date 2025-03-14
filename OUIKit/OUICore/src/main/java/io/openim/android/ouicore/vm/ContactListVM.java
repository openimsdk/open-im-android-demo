package io.openim.android.ouicore.vm;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.IView;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.widget.UILocker;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.listener.OnAdvanceMsgListener;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConversationListener;
import io.openim.android.sdk.models.C2CReadReceiptInfo;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.ConversationReq;
import io.openim.android.sdk.models.GroupMessageReceipt;
import io.openim.android.sdk.models.KeyValue;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.RevokedInfo;

public class ContactListVM extends BaseViewModel<ContactListVM.ViewAction> implements OnConversationListener, OnAdvanceMsgListener {
    public State<List<MsgConversation>> conversations = new State<>(new ArrayList<>());
    private UILocker uiLocker;
    @Override
    protected void viewCreate() {
        IMEvent.getInstance().addConversationListener(this);
        IMEvent.getInstance().addAdvanceMsgListener(this);
        updateConversation();
    }

    public void deleteConversationAndDeleteAllMsg(String conversationId) {
        OpenIMClient.getInstance().conversationManager
            .deleteConversationAndDeleteAllMsg(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(String data) {
                updateConversation();
            }
        }, conversationId);
    }

    public void updateConversation() {
        OpenIMClient.getInstance().conversationManager.getAllConversationList(new OnBase<List<ConversationInfo>>() {
            @Override
            public void onError(int code, String error) {
                getIView().onErr(error);
            }

            @Override
            public void onSuccess(List<ConversationInfo> data) {
                conversations.val().clear();
                for (ConversationInfo datum : data) {
                    Message msg = null;
                    if (null!=datum.getLatestMsg()){
                        msg = GsonHel.fromJson(datum.getLatestMsg(), Message.class);
                    }
                    MsgConversation msgConversation=new MsgConversation(msg, datum);
                    conversations.val().add(msgConversation);
                }
                conversations.setValue(conversations.getValue());
            }
        });
    }

    private void lockUI() {
        uiLocker = new UILocker();
        uiLocker.showTransparentDialog(context.get());
    }

    private void unLockUI() {
        if (uiLocker != null) {
            uiLocker.dismissTransparentDialog();
        }
    }

    @Override
    public void onConversationChanged(List<ConversationInfo> list) {
        updateConversation();
    }


    private void sortConversation(List<ConversationInfo> list, boolean needUnlockUI) {
        List<MsgConversation> msgConversations = new ArrayList<>();
        Iterator<MsgConversation> iterator = conversations.val().iterator();
        for (ConversationInfo info : list) {
            msgConversations.add(new MsgConversation(GsonHel.fromJson(info.getLatestMsg(),
                Message.class), info));
            while (iterator.hasNext()) {
                if (iterator.next().conversationInfo.getConversationID()
                    .equals(info.getConversationID()))
                    iterator.remove();
            }
        }
        conversations.val().addAll(msgConversations);
        Collections.sort(conversations.val(), IMUtil.simpleComparator());
        conversations.setValue(conversations.val());
        if (needUnlockUI)
            unLockUI();
    }

    @Override
    public void onNewConversation(List<ConversationInfo> list) {
        sortConversation(list, false);
    }

    @Override
    public void onSyncServerFailed(boolean reinstalled) {

    }

    @Override
    public void onSyncServerFinish(boolean reinstalled) {
        if (reinstalled) {
            OpenIMClient.getInstance().conversationManager.getAllConversationList(new OnBase<List<ConversationInfo>>() {
                @Override
                public void onSuccess(List<ConversationInfo> data) {
                    sortConversation(data, true);
                }

                @Override
                public void onError(int code, String error) {
                    unLockUI();
                    toast(error+code);
                }
            });
        }
    }

    @Override
    public void onSyncServerStart(boolean reinstalled) {
        if (reinstalled) {
            lockUI();
        }
    }

    @Override
    public void onTotalUnreadMessageCountChanged(int i) {

    }

    @Override
    public void onRecvNewMessage(Message msg) {
        changeGroupStatus(msg);
    }

    @Override
    public void onRecvC2CReadReceipt(List<C2CReadReceiptInfo> list) {

    }

    @Override
    public void onRecvGroupMessageReadReceipt(GroupMessageReceipt receipt) {

    }

    @Override
    public void onRecvMessageRevokedV2(RevokedInfo info) {

    }

    @Override
    public void onRecvMessageExtensionsChanged(String msgID, List<KeyValue> list) {

    }

    @Override
    public void onRecvMessageExtensionsDeleted(String msgID, List<String> list) {

    }

    @Override
    public void onRecvMessageExtensionsAdded(String msgID, List<KeyValue> list) {

    }

    @Override
    public void onMsgDeleted(Message message) {

    }

    @Override
    public void onRecvOfflineNewMessage(List<Message> msg) {

    }

    /**
     * 通过最新消息通知判断并改变用户是否在目标群聊中
     * @param message 通知消息
     */
    private void changeGroupStatus(Message message) {
        int msgType = message.getContentType();
        if (msgType < MessageType.MEMBER_KICKED_NTF ||
            msgType > MessageType.GROUP_DISBAND_NTF ||
            msgType == MessageType.MEMBER_ENTER_NTF) {
            return;
        }

        for(int index = 0;index < conversations.val().size();index ++) {
            ConversationInfo currentInfo = conversations.val().get(index).conversationInfo;
            if (message.getGroupID().equals(currentInfo.getGroupID())) {
                currentInfo.setNotInGroup(msgType == MessageType.MEMBER_KICKED_NTF ||
                    msgType == MessageType.GROUP_DISBAND_NTF);
                conversations.setValue(conversations.val());
                return;
            }
        }
    }

    //置顶/取消置顶 会话
    public void pinConversation(ConversationInfo conversationInfo, boolean isPinned) {
        ConversationReq conversationReq = new ConversationReq();
        conversationReq.setPinned(isPinned);
        OpenIMClient.getInstance().conversationManager.setConversation(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                toast(error+"("+code+")");
            }

            @Override
            public void onSuccess(String data) {
                conversationInfo.setPinned(isPinned);
            }
        }, conversationInfo.getConversationID(), conversationReq);
    }

    public interface ViewAction extends IView {
        void onErr(String msg);
    }
}
