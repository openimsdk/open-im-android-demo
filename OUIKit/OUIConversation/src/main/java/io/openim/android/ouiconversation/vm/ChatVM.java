package io.openim.android.ouiconversation.vm;


import static io.openim.android.ouicore.utils.Common.UIHandler;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.AtMsgInfo;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.entity.NotificationMsg;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.IView;
import io.openim.android.ouicore.entity.LocationInfo;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.FixSizeLinkedList;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;

import io.openim.android.sdk.listener.OnAdvanceMsgListener;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnMsgSendCallback;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.NotDisturbInfo;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.ReadReceiptInfo;
import io.openim.android.sdk.models.RevokedInfo;
import io.openim.android.sdk.models.UserInfo;

public class ChatVM extends BaseViewModel<ChatVM.ViewAction> implements OnAdvanceMsgListener {

    //会话信息
    public MutableLiveData<ConversationInfo> conversationInfo = new MutableLiveData<>();
    public MutableLiveData<Integer> notDisturbStatus = new MutableLiveData<>(0);
    //通知消息
    public MutableLiveData<NotificationMsg> notificationMsg = new MutableLiveData<>();
    public MutableLiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());
    //@消息
    public MutableLiveData<List<Message>> atMessages = new MutableLiveData<>(new ArrayList<>());
    public ObservableBoolean typing = new ObservableBoolean(false);
    public MutableLiveData<String> inputMsg = new MutableLiveData<>("");
    MutableLiveData<Boolean> isNoData = new MutableLiveData<>(false);

    //开启多选
    public MutableLiveData<Boolean> enableMultipleSelect = new MutableLiveData(false);

    private MessageAdapter messageAdapter;
    private Observer<String> inputObserver;
    Message startMsg = null; // 消息体，取界面上显示的消息体对象
    public String otherSideID = ""; // 接受消息Id
    public String groupID = ""; // 接受消息的群ID
    public boolean isSingleChat = true; //是否单聊 false 群聊
    public int count = 20; //条数
    public Message loading, forwardMsg;

    @Override
    protected void viewCreate() {
        super.viewCreate();
        loading = new Message();
        loading.setContentType(Constant.LOADING);

        //加载消息记录
        loadHistoryMessage();
        //标记所有消息已读
        markReaded(null);

        if (isSingleChat)
            listener();
        IMEvent.getInstance().addAdvanceMsgListener(this);
        //获取会话信息
        getConversationInfo();

    }

    private void getConversationInfo() {
        OpenIMClient.getInstance().conversationManager.getOneConversation(new OnBase<ConversationInfo>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(ConversationInfo data) {
                conversationInfo.setValue(data);
                getConversationRecvMessageOpt(data.getConversationID());
            }
        }, isSingleChat ? otherSideID : groupID, isSingleChat ?
            Constant.SessionType.SINGLE_CHAT : Constant.SessionType.GROUP_CHAT);
    }

    @Override
    protected void viewDestroy() {
        super.viewDestroy();
        IMEvent.getInstance().removeAdvanceMsgListener(this);
        inputMsg.removeObserver(inputObserver);
    }

    /**
     * 标记已读
     *
     * @param msgIDs 为null 清除里列表小红点
     */
    private void markReaded(List<String> msgIDs) {
        if (isSingleChat)
            OpenIMClient.getInstance().messageManager.markC2CMessageAsRead(null, otherSideID, null == msgIDs ? new ArrayList<>() : msgIDs);
        else
            OpenIMClient.getInstance().messageManager.markGroupMessageAsRead(null, groupID, null == msgIDs ? new ArrayList<>() : msgIDs);
    }


    private void listener() {
        //提示对方我正在输入
        inputMsg.observeForever(inputObserver = s -> {
                OpenIMClient.getInstance().messageManager.typingStatusUpdate(new OnBase<String>() {
                    @Override
                    public void onError(int code, String error) {

                    }

                    @Override
                    public void onSuccess(String data) {

                    }
                }, otherSideID, "");
            }
        );
    }


    public void setMessageAdapter(MessageAdapter messageAdapter) {
        this.messageAdapter = messageAdapter;
    }

    public void loadHistoryMessage() {
        OpenIMClient.getInstance().messageManager.getHistoryMessageList(new OnBase<List<Message>>() {
            @Override
            public void onError(int code, String error) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(List<Message> data) {
                for (Message datum : data) {
                    IMUtil.buildExpandInfo(datum);
                }
                List<Message> list = messages.getValue();
                if (data.isEmpty()) {
                    if (!messages.getValue().isEmpty()) {
                        isNoData.setValue(true);
                        removeLoading(list);
                    }
                    return;
                } else {
                    startMsg = data.get(0);
                    Collections.reverse(data);
                }
                if (list.isEmpty()) {
                    IMUtil.calChatTimeInterval(data);
                    messages.setValue(data);
                    return;
                }
                removeLoading(list);
                list.addAll(data);
                IMUtil.calChatTimeInterval(list);
                list.add(loading);
                messageAdapter.notifyItemRangeChanged(list.size() - 1 - data.size(), list.size() - 1);
            }

        }, otherSideID, groupID, null, startMsg, count);
    }

    //移除加载视图
    private void removeLoading(List<Message> list) {
        int index = list.indexOf(loading);
        if (index > -1) {
            list.remove(index);
            messageAdapter.notifyItemRemoved(index);
        }
    }

    //发送消息已读回执
    public void sendMsgReadReceipt(int firstVisiblePosition, int lastVisiblePosition) {
        int size = messages.getValue().size();
        if (lastVisiblePosition > size || firstVisiblePosition < 0) return;

        List<Message> megs = messages.getValue().subList(firstVisiblePosition, lastVisiblePosition);
        List<String> msgIds = new ArrayList<>();
        for (Message meg : megs) {
            if (!meg.isRead() && meg.getSendID().equals(otherSideID))
                msgIds.add(meg.getClientMsgID());
        }
        markReaded(msgIds);
    }

    private Runnable typRunnable = () -> {
        typing.set(false);
    };

    @Override
    public void onRecvNewMessage(Message msg) {
        if (isSingleChat) {
            if (TextUtils.isEmpty(msg.getSendID()) || !msg.getSendID().equals(otherSideID)) return;
        } else if (TextUtils.isEmpty(msg.getGroupID()) || !msg.getGroupID().equals(groupID)) return;

        boolean isTyp = msg.getContentType() == Constant.MsgType.TYPING;
        if (isSingleChat) {
            if (msg.getSendID().equals(otherSideID)) {
                UIHandler.post(() -> typing.set(isTyp));
                if (isTyp) {
                    UIHandler.removeCallbacks(typRunnable);
                    UIHandler.postDelayed(typRunnable, 5000);
                }
            }
        }
        if (isTyp) return;

        messages.getValue().add(0, IMUtil.buildExpandInfo(msg));
        IMUtil.calChatTimeInterval(messages.getValue());
        UIHandler.post(() -> {
            IView.scrollToPosition(0);
            messageAdapter.notifyItemInserted(0);
        });

        //清除列表小红点
        markReaded(null);
        //标记本条消息已读
        if (isSingleChat) {
            List<String> ids = new ArrayList<>();
            ids.add(msg.getClientMsgID());
            markReaded(ids);
        }
    }

    @Override
    public void onRecvC2CReadReceipt(List<ReadReceiptInfo> list) {

    }

    @Override
    public void onRecvGroupMessageReadReceipt(List<ReadReceiptInfo> list) {

    }

    @Override
    public void onRecvMessageRevoked(String msgId) {
        try {
            for (int i = 0; i < messageAdapter.getMessages().size(); i++) {
                Message message = messageAdapter.getMessages().get(i);
                if (TextUtils.isEmpty(message.getClientMsgID()))
                    continue;
                if (message.getClientMsgID().equals(msgId)) {
                    message.setContentType(Constant.MsgType.REVOKE);
                    messageAdapter.notifyItemChanged(i);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRecvMessageRevokedV2(RevokedInfo info) {

    }


    public void sendMsg(Message msg) {
        messages.getValue().add(0, IMUtil.buildExpandInfo(msg));
        messageAdapter.notifyItemInserted(0);

        IView.scrollToPosition(0);

        OfflinePushInfo offlinePushInfo = new OfflinePushInfo();  // 离线推送的消息备注；不为null
        OpenIMClient.getInstance().messageManager.sendMessage(new OnMsgSendCallback() {
            @Override
            public void onError(int code, String error) {
                IView.toast(error + code);
            }

            @Override
            public void onProgress(long progress) {
                L.e("");
            }

            @Override
            public void onSuccess(Message message) {
                // 返回新的消息体；替换发送传入的，不然撤回消息会有bug
                int index = messages.getValue().indexOf(msg);
                messages.getValue().remove(index);
                messages.getValue().add(index, IMUtil.buildExpandInfo(message));
                IMUtil.calChatTimeInterval(messages.getValue());
                messageAdapter.notifyItemChanged(index);
            }
        }, msg, otherSideID, groupID, offlinePushInfo);
    }

    /**
     * 独立发送
     */
    public void aloneSendMsg(Message msg, String otherSideID, String otherSideGroupID) {
        if (this.otherSideID.equals(otherSideID) || groupID.equals(otherSideGroupID)) {
            //如果转发给本人/本群
            sendMsg(msg);
            return;
        }
        OfflinePushInfo offlinePushInfo = new OfflinePushInfo();  // 离线推送的消息备注；不为null
        OpenIMClient.getInstance().messageManager.sendMessage(new OnMsgSendCallback() {
            @Override
            public void onError(int code, String error) {
                IView.toast(error + code);
            }

            @Override
            public void onProgress(long progress) {
            }

            @Override
            public void onSuccess(Message message) {
                IView.toast(getContext().getString(io.openim.android.ouicore.R.string.send_succ));
            }
        }, msg, otherSideID, otherSideGroupID, offlinePushInfo);
    }

    /**
     * 撤回消息
     *
     * @param message
     */
    public void revokeMessage(Message message) {
        OpenIMClient.getInstance().messageManager.revokeMessage(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(String data) {
                message.setContentType(Constant.MsgType.REVOKE);
                messageAdapter.notifyItemChanged(messageAdapter.getMessages().indexOf(message));
            }
        }, message);


    }

    public void deleteMessageFromLocalStorage(Message message) {
        message.setExt(null);
        OpenIMClient.getInstance().messageManager.deleteMessageFromLocalStorage(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                IView.toast(error + "(" + code + ")");
            }

            @Override
            public void onSuccess(String data) {
                int index = messageAdapter.getMessages().indexOf(message);
                messageAdapter.getMessages().remove(index);
                messageAdapter.notifyItemRemoved(index);
            }
        }, message);
    }

    public void closePage() {
        IView.closePage();
    }

    public void clearC2CHistory(String uid) {
        WaitDialog waitDialog = new WaitDialog(getContext());
        waitDialog.show();
        OpenIMClient.getInstance().messageManager
            .clearC2CHistoryMessageFromLocalAndSvr(new OnBase<String>() {
                @Override
                public void onError(int code, String error) {
                    waitDialog.dismiss();
                    IView.toast(error + code);
                }

                @Override
                public void onSuccess(String data) {
                    waitDialog.dismiss();
                    messages.getValue().clear();
                    IView.toast(getContext().getString(io.openim.android.ouicore.R.string.clear_succ));
                }
            }, uid);

    }

    public void getConversationRecvMessageOpt(String... cid) {
        OpenIMClient.getInstance().conversationManager.getConversationRecvMessageOpt(new OnBase<List<NotDisturbInfo>>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(List<NotDisturbInfo> data) {
                if (data.isEmpty()) return;
                notDisturbStatus.setValue(data.get(0).getResult());
            }
        }, Arrays.asList(cid));
    }

    public void setConversationRecvMessageOpt(int status, String... cid) {
        OpenIMClient.getInstance().conversationManager.setConversationRecvMessageOpt(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
            }

            @Override
            public void onSuccess(String data) {
                notDisturbStatus.setValue(status);
            }
        }, Arrays.asList(cid), status);
    }

    public interface ViewAction extends IView {
        void scrollToPosition(int position);

        void closePage();
    }
}
