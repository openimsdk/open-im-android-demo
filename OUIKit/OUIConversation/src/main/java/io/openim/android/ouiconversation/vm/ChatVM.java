package io.openim.android.ouiconversation.vm;


import static io.openim.android.ouicore.utils.Common.UIHandler;

import android.text.TextUtils;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouiconversation.utils.Constant;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.IView;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.sdk.OpenIMClient;

import io.openim.android.sdk.listener.OnAdvanceMsgListener;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnMsgSendCallback;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.ReadReceiptInfo;

public class ChatVM extends BaseViewModel<ChatVM.ViewAction> implements OnAdvanceMsgListener {
    public MutableLiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());
    public ObservableBoolean typing = new ObservableBoolean(false);
    public MutableLiveData<String> inputMsg = new MutableLiveData<>("");
    MutableLiveData<Boolean> isNoData = new MutableLiveData<>(false);

    private MessageAdapter messageAdapter;
    private Observer<String> inputObserver;
    Message startMsg = null; // 消息体，取界面上显示的消息体对象
    public String otherSideID = ""; // 消息发送方
    public String groupID = ""; // 接受消息的群ID
    public boolean isSingleChat = true; //是否单聊 false 群聊
    int count = 20; //条数
    Message loading;


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

            @Override
            public void onSuccess(List<Message> data) {
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
                    messages.setValue(data);
                    return;
                }
                removeLoading(list);
                list.addAll(data);
                IMUtil.calChatTimeInterval(list);
                list.add(loading);
                messageAdapter.notifyItemRangeChanged(list.size() - 1 - data.size(), list.size() - 1);
            }

        }, otherSideID, groupID, startMsg, count);
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
        int size=messages.getValue().size();
        if (size>firstVisiblePosition||size<lastVisiblePosition)return;

        List<Message> megs = messages.getValue().subList(firstVisiblePosition, lastVisiblePosition);
        List<String> msgIds = new ArrayList<>();
        for (Message meg : megs) {
            if (!meg.isRead() && meg.getSendID().equals(otherSideID))
                msgIds.add(meg.getClientMsgID());
        }
        OpenIMClient.getInstance().messageManager.markC2CMessageAsRead(null, otherSideID, msgIds);
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
        if (!isTyp) {
            messages.getValue().add(0, msg);
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

    public void sendMsg(Message msg) {
        messages.getValue().add(0, msg);
        messageAdapter.notifyItemInserted(0);

        IView.scrollToPosition(0);

        List<Message> megs = messages.getValue();
        OfflinePushInfo offlinePushInfo = new OfflinePushInfo();  // 离线推送的消息备注；不为null
        OpenIMClient.getInstance().messageManager.sendMessage(new OnMsgSendCallback() {
            @Override
            public void onError(int code, String error) {
                L.e("");
            }

            @Override
            public void onProgress(long progress) {
                L.e("");
            }

            @Override
            public void onSuccess(Message message) {
                // 返回新的消息体；替换发送传入的，不然扯回消息会有bug
                int index = megs.indexOf(msg);
                megs.add(index, message);
                megs.remove(index + 1);
                messageAdapter.notifyItemChanged(index);
            }
        }, msg, otherSideID, groupID, offlinePushInfo);

    }

    public interface ViewAction extends IView {
        void scrollToPosition(int position);
    }
}
