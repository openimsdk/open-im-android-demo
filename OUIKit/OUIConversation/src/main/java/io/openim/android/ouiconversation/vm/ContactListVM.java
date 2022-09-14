package io.openim.android.ouiconversation.vm;

import static io.openim.android.ouicore.utils.Common.UIHandler;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.IView;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnAdvanceMsgListener;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConversationListener;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.ReadReceiptInfo;
import io.openim.android.sdk.models.RevokedInfo;

public class ContactListVM extends BaseViewModel<ContactListVM.ViewAction> implements OnConversationListener, OnAdvanceMsgListener {
    public MutableLiveData<List<MsgConversation>> conversations = new MutableLiveData<>(new ArrayList<>());

    @Override
    protected void viewCreate() {
        IMEvent.getInstance().addConversationListener(this);
        IMEvent.getInstance().addAdvanceMsgListener(this);
        updataConversation();

        UIHandler.postDelayed(this::updataConversation, 5 * 1000);
    }

    public void deleteConversationFromLocalAndSvr(String conversationId) {
        OpenIMClient.getInstance().conversationManager.deleteConversationFromLocalAndSvr(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(String data) {
                updataConversation();
            }
        }, conversationId);
    }

    private void updataConversation() {
        OpenIMClient.getInstance().conversationManager.getAllConversationList(new OnBase<List<ConversationInfo>>() {
            @Override
            public void onError(int code, String error) {
                IView.onErr(error);
            }

            @Override
            public void onSuccess(List<ConversationInfo> data) {
                L.e("AllConversationList---size--" + data.size());
                conversations.getValue().clear();
                for (ConversationInfo datum : data) {
                    Message msg = GsonHel.fromJson(datum.getLatestMsg(), Message.class);
                    if (null == msg)
                        continue;
                    conversations.getValue().add(new MsgConversation(msg, datum));
                }
                conversations.setValue(conversations.getValue());
            }
        });
    }

    @Override
    public void onConversationChanged(List<ConversationInfo> list) {
        updataConversation();
    }


    private void sortConversation(List<ConversationInfo> list) {
        List<MsgConversation> msgConversations = new ArrayList<>();
        for (ConversationInfo info : list) {
            msgConversations.add(new MsgConversation(GsonHel.fromJson(info.getLatestMsg(), Message.class), info));
            Iterator<MsgConversation> iterator = conversations.getValue().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().conversationInfo.getConversationID().equals(info.getConversationID()))
                    iterator.remove();
            }
        }
        conversations.getValue().addAll(msgConversations);
        Collections.sort(conversations.getValue(), IMUtil.simpleComparator());
        conversations.setValue(conversations.getValue());
    }

    @Override
    public void onNewConversation(List<ConversationInfo> list) {
        sortConversation(list);
    }

    @Override
    public void onSyncServerFailed() {

    }

    @Override
    public void onSyncServerFinish() {

    }

    @Override
    public void onSyncServerStart() {

    }

    @Override
    public void onTotalUnreadMessageCountChanged(int i) {
        L.e("");
    }

    @Override
    public void onRecvNewMessage(Message msg) {

    }

    @Override
    public void onRecvC2CReadReceipt(List<ReadReceiptInfo> list) {

    }

    @Override
    public void onRecvGroupMessageReadReceipt(List<ReadReceiptInfo> list) {

    }

    @Override
    public void onRecvMessageRevoked(String msgId) {

    }

    @Override
    public void onRecvMessageRevokedV2(RevokedInfo info) {

    }

    public interface ViewAction extends IView {
        void onErr(String msg);
    }
}
