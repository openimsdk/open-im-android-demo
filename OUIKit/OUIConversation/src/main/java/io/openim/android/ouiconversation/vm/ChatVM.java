package io.openim.android.ouiconversation.vm;


import static io.openim.android.ouicore.utils.Common.UIHandler;
import static io.openim.android.ouicore.utils.Common.md5;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;


import javax.annotation.Nullable;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.AtMsgInfo;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.entity.NotificationMsg;
import io.openim.android.ouicore.entity.OnlineStatus;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.services.OneselfService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.IView;
import io.openim.android.ouicore.entity.LocationInfo;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.FixSizeLinkedList;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;

import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.listener.OnAdvanceMsgListener;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConversationListener;
import io.openim.android.sdk.listener.OnGroupListener;
import io.openim.android.sdk.listener.OnMsgSendCallback;
import io.openim.android.sdk.models.AdvancedMessage;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.GroupApplicationInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.KeyValue;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.NotDisturbInfo;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.ReadReceiptInfo;
import io.openim.android.sdk.models.RevokedInfo;
import io.openim.android.sdk.models.SearchResult;
import io.openim.android.sdk.models.SearchResultItem;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;
import okhttp3.ResponseBody;

public class ChatVM extends BaseViewModel<ChatVM.ViewAction> implements OnAdvanceMsgListener,
    OnGroupListener, OnConversationListener, java.util.Observer {
    public CallingService callingService =
        (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();

    //阅后即焚Timers
    HashMap<String, Timer> readVanishTimers = new HashMap<>();
    //搜索的本地消息
    public MutableLiveData<List<Message>> searchMessageItems =
        new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<Message>> addSearchMessageItems =
        new MutableLiveData<>(new ArrayList<>());
    //回复消息
    public MutableLiveData<Message> replyMessage = new MutableLiveData<>();
    //免打扰状态
    public MutableLiveData<Integer> notDisturbStatus = new MutableLiveData<>(0);
    //通知消息
    public MutableLiveData<ConversationInfo> conversationInfo = new MutableLiveData<>();
    public MutableLiveData<GroupInfo> groupInfo = new MutableLiveData<>();
    public MutableLiveData<NotificationMsg> notificationMsg = new MutableLiveData<>();
    public MutableLiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());
    //@消息
    public MutableLiveData<List<Message>> atMessages = new MutableLiveData<>(new ArrayList<>());
    //表情
    public MutableLiveData<List<String>> emojiMessages = new MutableLiveData<>(new ArrayList<>());
    public ObservableBoolean typing = new ObservableBoolean(false);
    public MutableLiveData<String> inputMsg = new MutableLiveData<>("");
    MutableLiveData<Boolean> isNoData = new MutableLiveData<>(false);

    //开启多选
    public MutableLiveData<Boolean> enableMultipleSelect = new MutableLiveData();

    public boolean viewPause = false;
    private MessageAdapter messageAdapter;
    private Observer<String> inputObserver;
    public Message startMsg = null; // 消息体，取界面上显示的消息体对象/搜索时的起始坐标
    //otherSideID 与 GROUP_ID 互斥
    public String otherSideID = ""; // 接受消息的用户Id
    public String groupID = ""; // 接受消息的群ID
    public String conversationID; //会话id
    public boolean isSingleChat = true; //是否单聊 false 群聊
    public boolean isVideoCall = true;//是否是视频通话
    public boolean fromChatHistory = false;//从查看聊天记录跳转过来
    public boolean firstChatHistory = true;// //用于第一次消息定位
    public boolean hasPermission = false;// 为true 则是管理员或群主

    public int count = 20; //条数
    public Message loading, forwardMsg;
    private int lastMinSeq = 0;


    public void init() {
        loading = new Message();
        loading.setContentType(Constant.LOADING);
        //获取会话信息
        getConversationInfo();
        //标记所有消息已读
        markReaded(null);

        IMEvent.getInstance().addAdvanceMsgListener(this);
        IMEvent.getInstance().addConversationListener(this);
        if (isSingleChat) {
            listener();
        } else {
            getGroupPermissions();
            IMEvent.getInstance().addGroupListener(this);
        }
    }

    /**
     * 获取自己在这个群的权限
     */
    private void getGroupPermissions() {
        List<String> uid = new ArrayList<>();
        uid.add(BaseApp.inst().loginCertificate.userID);
        OpenIMClient.getInstance().groupManager.getGroupMembersInfo(new OnBase<List<GroupMembersInfo>>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(List<GroupMembersInfo> data) {
                if (data.isEmpty()) return;
                hasPermission = data.get(0).getRoleLevel() != Constant.RoleLevel.MEMBER;
            }
        }, groupID, uid);
    }

    @Override
    protected void viewPause() {
        viewPause = true;
    }

    @Override
    protected void viewResume() {
        viewPause = false;
    }

    //获取在线状态
    public void getUserOnlineStatus(UserOnlineStatusListener userOnlineStatusListener) {
        List<String> uIds = new ArrayList<>();
        uIds.add(otherSideID);
        Parameter parameter = new Parameter().add("userIDList", uIds).add("operationID",
            System.currentTimeMillis() + "");

        N.API(OneselfService.class).getUsersOnlineStatus(Constant.getImApiUrl() + "/user" +
                "/get_users_online_status", BaseApp.inst().loginCertificate.imToken,
            parameter.buildJsonBody()).compose(N.IOMain()).subscribe(new NetObserver<ResponseBody>(getContext()) {

            @Override
            public void onSuccess(ResponseBody o) {
                try {
                    String body = o.string();
                    Base<List<OnlineStatus>> base = GsonHel.dataArray(body, OnlineStatus.class);
                    if (base.errCode != 0) {
                        getIView().toast(base.errMsg);
                        return;
                    }
                    if (null == base.data || base.data.isEmpty()) return;
                    userOnlineStatusListener.onResult(base.data.get(0));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            protected void onFailure(Throwable e) {
                getIView().toast(e.getMessage());
            }
        });
    }

    public String handlePlatformCode(List<OnlineStatus.DetailPlatformStatus> detailPlatformStatus) {
        List<String> pList = new ArrayList<>();
        for (OnlineStatus.DetailPlatformStatus platform : detailPlatformStatus) {
            if (platform.platform.equals("Android") || platform.platform.equals("IOS")) {
                pList.add(getContext().getString(io.openim.android.ouicore.R.string.mobile_phone));
            } else if (platform.platform.equals("Windows")) {
                pList.add(getContext().getString(io.openim.android.ouicore.R.string.pc));
            } else if (platform.platform.equals("Web")) {
                pList.add(getContext().getString(io.openim.android.ouicore.R.string.Web));
            } else if (platform.platform.equals("MiniWeb")) {
                pList.add(getContext().getString(io.openim.android.ouicore.R.string.webMiniOnline));
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return String.join("/", pList);
        }
        return "";
    }

    /**
     * 清空选择的msg
     */
    public void clearSelectMsg() {
        forwardMsg = null;
        for (Message message : messageAdapter.getMessages()) {
            MsgExpand msgExpand = (MsgExpand) message.getExt();
            if (null != msgExpand) {
                msgExpand.isChoice = false;
                message.setExt(msgExpand);
            }
        }
    }

    @Override
    public void onGroupApplicationAccepted(GroupApplicationInfo info) {

    }

    @Override
    public void onGroupApplicationAdded(GroupApplicationInfo info) {

    }

    @Override
    public void onGroupApplicationDeleted(GroupApplicationInfo info) {

    }

    @Override
    public void onGroupApplicationRejected(GroupApplicationInfo info) {

    }

    @Override
    public void onGroupInfoChanged(GroupInfo info) {
        groupInfo.setValue(info);
    }

    @Override
    public void onGroupMemberAdded(GroupMembersInfo info) {

    }

    @Override
    public void onGroupMemberDeleted(GroupMembersInfo info) {

    }

    @Override
    public void onGroupMemberInfoChanged(GroupMembersInfo info) {

    }

    @Override
    public void onJoinedGroupAdded(GroupInfo info) {

    }

    @Override
    public void onJoinedGroupDeleted(GroupInfo info) {

    }

    @Override
    public void onConversationChanged(List<ConversationInfo> list) {
        try {
            for (ConversationInfo info : list) {
                if (info.getConversationID().equals(conversationInfo.getValue().getConversationID()))
                    conversationInfo.setValue(info);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onNewConversation(List<ConversationInfo> list) {

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

    }

    /**
     * 添加到阅后即焚timers
     *
     * @param message
     */
    public void addReadVanish(Message message) {
        String id = message.getClientMsgID();
        if (readVanishTimers.containsKey(id)) return;
        final int[] countdown = {getReadCountdown(message)};
        if (countdown[0] <= 0) {
            deleteMessageFromLocalAndSvr(message);
            return;
        }
        Timer timer = new Timer();
        readVanishTimers.put(id, timer);
        MsgExpand msgExpand = (MsgExpand) message.getExt();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int num = countdown[0]--;
                msgExpand.readVanishNum = num;
                message.setExt(msgExpand);
                if (num > 0) {
                    int index = messages.getValue().indexOf(message);
                    UIHandler.post(() -> messageAdapter.notifyItemChanged(index));
                    return;
                }
                cancel();
                deleteMessageFromLocalAndSvr(message);
            }
        }, 0, 1000);
    }

    private void deleteMessageFromLocalAndSvr(Message message) {
        message.setExt(null);
        OpenIMClient.getInstance().messageManager.deleteMessageFromLocalAndSvr(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                deleteMessageFromLocalStorage(message);
            }

            @Override
            public void onSuccess(String data) {
                removeMsList(message);
            }
        }, message);
    }

    int getReadCountdown(Message message) {
        int burnDuration = message.getAttachedInfoElem().getBurnDuration();
        long hasReadTime = message.getAttachedInfoElem().getHasReadTime();
        if (hasReadTime > 0) {
            long end = hasReadTime + (burnDuration * 1000L);
            long diff = (end - System.currentTimeMillis()) / 1000;
            return diff < 0 ? 0 : (int) diff;
        }

        return 0;
    }

    @Override
    public void update(Observable o, Object arg) {

    }

    public interface UserOnlineStatusListener {
        void onResult(OnlineStatus onlineStatus);
    }

    private void getConversationInfo() {
        if (isSingleChat) {
            getOneConversation(null);
        } else {
            getGroupsInfo(groupID, null);
        }
    }

    public void getGroupsInfo(String groupID,
                              IMUtil.OnSuccessListener<List<GroupInfo>> onSuccessListener) {
        List<String> groupIds = new ArrayList<>();
        groupIds.add(groupID);
        OpenIMClient.getInstance().groupManager.getGroupsInfo(new OnBase<List<GroupInfo>>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + code);
            }

            @Override
            public void onSuccess(List<GroupInfo> data) {
                if (data.isEmpty()) return;
                if (null != onSuccessListener) {
                    onSuccessListener.onSuccess(data);
                    return;
                }
                groupInfo.setValue(data.get(0));

                getOneConversation(null);
            }
        }, groupIds);
    }

    private boolean getIsSuperGroup() {
        return groupInfo.getValue().getGroupType() == 2;
    }

    public void getOneConversation(IMUtil.OnSuccessListener<ConversationInfo> onSuccessListener) {
        OpenIMClient.getInstance().conversationManager.getOneConversation(new OnBase<ConversationInfo>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error);
            }

            @Override
            public void onSuccess(ConversationInfo data) {
                if (null != onSuccessListener) {
                    onSuccessListener.onSuccess(data);
                    return;
                }
                conversationInfo.setValue(data);
                loadHistory();
                getConversationRecvMessageOpt(data.getConversationID());
            }
        }, isSingleChat ? otherSideID : groupID, isSingleChat ? Constant.SessionType.SINGLE_CHAT
            : getIsSuperGroup() ? Constant.SessionType.SUPER_GROUP :
            Constant.SessionType.GROUP_CHAT);
    }

    private void loadHistory() {
        //加载消息记录
        if (fromChatHistory) loadHistoryMessageReverse();
        else loadHistoryMessage();
    }

    @Override
    protected void viewDestroy() {
        super.viewDestroy();
        IMEvent.getInstance().removeAdvanceMsgListener(this);
        IMEvent.getInstance().removeGroupListener(this);
        IMEvent.getInstance().removeConversationListener(this);
        inputMsg.removeObserver(inputObserver);

        for (Timer value : readVanishTimers.values()) {
            value.cancel();
        }
        readVanishTimers.clear();
    }

    /**
     * 标记已读
     *
     * @param msgs 为null 清除里列表小红点
     */
    public void markReaded(@Nullable Message... msgs) {
        List<String> msgIDs = new ArrayList<>();
        if (null != msgs) {
            for (Message msg : msgs) {
                msgIDs.add(msg.getClientMsgID());
            }
        }
        OnBase<String> callBack = new OnBase<String>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(String data) {
                if (null != msgs) {
                    long currentTimeMillis = System.currentTimeMillis();
                    for (Message msg : msgs) {
                        msg.setRead(true);
                        msg.getAttachedInfoElem().setHasReadTime(currentTimeMillis);

                        if (conversationInfo.getValue().isPrivateChat())
                            messageAdapter.notifyItemChanged(messages.getValue().indexOf(msg));
                    }
                }
            }
        };
        if (isSingleChat)
            OpenIMClient.getInstance().messageManager.markC2CMessageAsRead(callBack, otherSideID,
                msgIDs);
        else
            OpenIMClient.getInstance().messageManager.markGroupMessageAsRead(callBack, groupID,
                msgIDs);
    }

    /**
     * 标记已读
     * By conversationID
     *
     * @param msgIDs 为null 清除里列表小红点
     */
    public void markReadedByConID(String conversationID, List<String> msgIDs,
                                  IMUtil.OnSuccessListener onSuccessListener) {
        OpenIMClient.getInstance().messageManager.markMessageAsReadByConID(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + code);
            }

            @Override
            public void onSuccess(String data) {
                if (null != onSuccessListener) onSuccessListener.onSuccess(data);
            }
        }, conversationID, null == msgIDs ? new ArrayList<>() : msgIDs);
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
        });
    }


    public void setMessageAdapter(MessageAdapter messageAdapter) {
        this.messageAdapter = messageAdapter;
    }

    public void loadHistoryMessage() {
        if (null != groupInfo.getValue() && getIsSuperGroup()) {
            OpenIMClient.getInstance().messageManager.getAdvancedHistoryMessageList(new OnBase<AdvancedMessage>() {
                                                                                        @Override
                                                                                        public void onError(int code, String error) {
                                                                                            getIView().toast(error + code);
                                                                                        }

                                                                                        @Override
                                                                                        public void onSuccess(AdvancedMessage data) {
                                                                                            lastMinSeq = data.getLastMinSeq();
                                                                                            handleMessage(data.getMessageList(), false);
                                                                                        }
                                                                                    }, null, null
                , conversationInfo.getValue().getConversationID(), lastMinSeq, startMsg,
                count);
        } else {
            OpenIMClient.getInstance().messageManager.getHistoryMessageList(new OnBase<List<Message>>() {
                @Override
                public void onError(int code, String error) {

                }

                @Override
                public void onSuccess(List<Message> data) {
                    handleMessage(data, false);
                }

            }, otherSideID, groupID, conversationID, startMsg, count);
        }
    }

    private void handleMessage(List<Message> data, boolean isReverse) {
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
        if (isReverse) {
            list.addAll(0, data);
            IMUtil.calChatTimeInterval(list);
            messageAdapter.notifyItemRangeInserted(0, data.size());
            return;
        }
        removeLoading(list);
        list.addAll(data);
        IMUtil.calChatTimeInterval(list);
        list.add(loading);
        messageAdapter.notifyItemRangeChanged(list.size() - 1 - data.size(), list.size() - 1);
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
        lastVisiblePosition += 1;
        if (lastVisiblePosition > size || firstVisiblePosition < 0) return;
        List<Message> megs = new ArrayList<>();
        megs.addAll(messages.getValue().subList(firstVisiblePosition, lastVisiblePosition));
        Iterator<Message> iterator = megs.iterator();
        try {
            while (iterator.hasNext()) {
                Message meg = iterator.next();
                if (meg.isRead() || meg.getContentType() >= Constant.MsgType.NOTICE || meg.getContentType() == Constant.MsgType.VOICE || (null == meg.getSendID() || meg.getSendID().equals(BaseApp.inst().loginCertificate.userID)))
                    iterator.remove();
            }
        } catch (Exception ignored) {
        }
        if (!megs.isEmpty()) markReaded(megs.toArray(new Message[0]));

    }

    private Runnable typRunnable = () -> typing.set(false);

    /// 是当前聊天窗口
    Boolean isCurrentChat(Message message) {
        String senderId = message.getSendID();
        String receiverId = message.getRecvID();
        String groupId = message.getGroupID();
        boolean isCurSingleChat =
            message.getSessionType() == Constant.SessionType.SINGLE_CHAT && isSingleChat && (senderId.equals(otherSideID) || receiverId.equals(otherSideID));
        boolean isCurGroupChat =
            message.getSessionType() != Constant.SessionType.SINGLE_CHAT && !isSingleChat && groupID.equals(groupId);
        return isCurSingleChat || isCurGroupChat;
    }

    @Override
    public void onRecvNewMessage(Message msg) {
        if (!isCurrentChat(msg)) return;
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
            getIView().scrollToPosition(0);
            messageAdapter.notifyItemInserted(0);
        });

        //清除列表小红点
        markReaded(null);

        //标记本条消息已读 语音消息需要点播放才算读
        if (!viewPause && msg.getContentType() != Constant.MsgType.VOICE) markReaded(msg);

        statusUPDATE(msg);
    }

    private void statusUPDATE(Message msg) {
        try {
            int contentType = msg.getContentType();
            if (contentType == Constant.MsgType.BULLETIN) {
                NotificationMsg no = GsonHel.fromJson(msg.getNotificationElem().getDetail(),
                    NotificationMsg.class);
                if (!TextUtils.isEmpty(no.group.notification)) notificationMsg.setValue(no);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRecvC2CReadReceipt(List<ReadReceiptInfo> list) {
        try {
            for (ReadReceiptInfo readInfo : list) {
                if (readInfo.getUserID().equals(otherSideID)) {
                    for (Message message : messages.getValue()) {
                        if (readInfo.getMsgIDList().contains(message.getClientMsgID())) {
                            message.setRead(true);
                            message.getAttachedInfoElem().setHasReadTime(readInfo.getReadTime());
                            messageAdapter.notifyItemChanged(messages.getValue().indexOf(message));
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRecvGroupMessageReadReceipt(List<ReadReceiptInfo> list) {
        try {
            for (ReadReceiptInfo readInfo : list) {
                if (readInfo.getGroupID().equals(groupID)) {
                    for (Message e : messages.getValue()) {
                        List<String> uidList =
                            e.getAttachedInfoElem().getGroupHasReadInfo().getHasReadUserIDList();
                        if (null == uidList) uidList = new ArrayList<>();
                        if (!uidList.contains(readInfo.getUserID()) && (readInfo.getMsgIDList().contains(e.getClientMsgID()))) {
                            uidList.add(readInfo.getUserID());
                            e.getAttachedInfoElem().getGroupHasReadInfo().setHasReadUserIDList(uidList);
                            messageAdapter.notifyItemChanged(messages.getValue().indexOf(e));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRecvMessageRevoked(String msgId) {
        try {
            for (int i = 0; i < messageAdapter.getMessages().size(); i++) {
                Message message = messageAdapter.getMessages().get(i);
                if (TextUtils.isEmpty(message.getClientMsgID())) continue;
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
        try {
            for (Message message : messages.getValue()) {
                if (message.getClientMsgID().equals(info.getClientMsgID())) {
                    message.setContentType(Constant.MsgType.ADVANCED_REVOKE);
                    messageAdapter.notifyItemChanged(messages.getValue().indexOf(message));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRecvMessageExtensionsChanged(String msgID, List<KeyValue> list) {

    }

    @Override
    public void onRecvMessageExtensionsDeleted(String msgID, List<String> list) {

    }


    public void sendMsg(Message msg) {
        msg.setStatus(Constant.Send_State.SENDING);
        if (messages.getValue().contains(msg)) {
            messageAdapter.notifyItemChanged(messages.getValue().indexOf(msg));
        } else {
            messages.getValue().add(0, IMUtil.buildExpandInfo(msg));
            messageAdapter.notifyItemInserted(0);
            getIView().scrollToPosition(0);
        }
        UIHandler.post(() -> {
            msg.setExt(null);//必须重置
            OfflinePushInfo offlinePushInfo = new OfflinePushInfo();  // 离线推送的消息备注；不为null
            OpenIMClient.getInstance().messageManager.sendMessage(new OnMsgSendCallback() {
                @Override
                public void onError(int code, String error) {
                    if (code != 302) getIView().toast(error + code);
                    UIHandler.postDelayed(() -> {
                        msg.setStatus(Constant.Send_State.SEND_FAILED);
                        messageAdapter.notifyItemChanged(messages.getValue().indexOf(msg));
                    }, 500);
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
        });
    }

    public void aloneSendMsg(Message msg, String otherSideID, String otherSideGroupID) {
        aloneSendMsg(msg, otherSideID, otherSideGroupID, null);
    }

    /**
     * 独立发送
     */
    public void aloneSendMsg(Message msg, String otherSideID, String otherSideGroupID,
                             OnMsgSendCallback onMsgSendCallback) {
        if (this.otherSideID.equals(otherSideID) || groupID.equals(otherSideGroupID)) {
            //如果转发给本人/本群
            sendMsg(msg);
            return;
        }
        OfflinePushInfo offlinePushInfo = new OfflinePushInfo(); // 离线推送的消息备注；不为null
        if (null == onMsgSendCallback) {
            onMsgSendCallback = new OnMsgSendCallback() {
                @Override
                public void onError(int code, String error) {
                    getIView().toast(error + code);
                }

                @Override
                public void onProgress(long progress) {
                }

                @Override
                public void onSuccess(Message message) {
                    getIView().toast(getContext().getString(io.openim.android.ouicore.R.string.send_succ));
                }
            };
        }
        OpenIMClient.getInstance().messageManager.sendMessage(onMsgSendCallback, msg, otherSideID
            , otherSideGroupID, offlinePushInfo);
    }

    /**
     * 撤回消息
     *
     * @param message
     */
    public void revokeMessage(Message message) {
        OpenIMClient.getInstance().messageManager.revokeMessageV2(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + code);
            }

            @Override
            public void onSuccess(String data) {
                message.setContentType(Constant.MsgType.ADVANCED_REVOKE);
                if (hasPermission)
                    message.setSenderNickname(BaseApp.inst().loginCertificate.nickname);
                messageAdapter.notifyItemChanged(messageAdapter.getMessages().indexOf(message));
            }
        }, message);


    }

    public void deleteMessageFromLocalStorage(Message message) {
        message.setExt(null);
        OpenIMClient.getInstance().messageManager.deleteMessageFromLocalStorage(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + "(" + code + ")");
            }

            @Override
            public void onSuccess(String data) {
                removeMsList(message);
            }
        }, message);
    }

    private void removeMsList(Message message) {
        int index = messages.getValue().indexOf(message);
        messageAdapter.getMessages().remove(index);
        messageAdapter.notifyItemRemoved(index);
        enableMultipleSelect.setValue(false);
    }

    public void closePage() {
        getIView().closePage();
    }

    public void clearCHistory(String id) {
        WaitDialog waitDialog = new WaitDialog(getContext());
        waitDialog.show();
        if (isSingleChat) {
            OpenIMClient.getInstance().messageManager.clearC2CHistoryMessageFromLocalAndSvr(new OnBase<String>() {
                @Override
                public void onError(int code, String error) {
                    waitDialog.dismiss();
                    getIView().toast(error + code);
                }

                @Override
                public void onSuccess(String data) {
                    waitDialog.dismiss();
                    messages.getValue().clear();
                    messageAdapter.notifyDataSetChanged();
                    getIView().toast(getContext().getString(io.openim.android.ouicore.R.string.clear_succ));
                }
            }, id);
        } else {
            OpenIMClient.getInstance().messageManager.clearGroupHistoryMessageFromLocalAndSvr(new OnBase<String>() {
                @Override
                public void onError(int code, String error) {
                    waitDialog.dismiss();
                    getIView().toast(error + code);
                }

                @Override
                public void onSuccess(String data) {
                    waitDialog.dismiss();
                    messages.getValue().clear();
                    messageAdapter.notifyDataSetChanged();
                    getIView().toast(getContext().getString(io.openim.android.ouicore.R.string.clear_succ));
                }
            }, id);
        }


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

    public void searchLocalMessages(String key, int page, Integer... messageTypes) {
        List<String> keys = null;
        if (!TextUtils.isEmpty(key)) {
            keys = new ArrayList<>();
            keys.add(key);
        }
        List<Integer> messageTypeLists;
        if (0 == messageTypes.length) {
            messageTypeLists = new ArrayList<>();
            messageTypeLists.add(Constant.MsgType.TXT);
            messageTypeLists.add(Constant.MsgType.MENTION);
        } else messageTypeLists = Arrays.asList(messageTypes);

        String conversationId = conversationInfo.getValue().getConversationID();
        OpenIMClient.getInstance().messageManager.searchLocalMessages(new OnBase<SearchResult>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + code);
                L.e("");
            }

            @Override
            public void onSuccess(SearchResult data) {
                if (page == 1) {
                    searchMessageItems.getValue().clear();
                }
                if (data.getTotalCount() != 0) {
                    for (Message message : data.getSearchResultItems().get(0).getMessageList()) {
                        IMUtil.buildExpandInfo(message);
                    }
                    searchMessageItems.getValue().addAll(data.getSearchResultItems().get(0).getMessageList());
                    addSearchMessageItems.setValue(data.getSearchResultItems().get(0).getMessageList());
                }
                searchMessageItems.setValue(searchMessageItems.getValue());
            }
        }, conversationId, keys, 0, new ArrayList<>(), messageTypeLists, 0, 0, page, count);
    }

    public void loadHistoryMessageReverse() {
        OpenIMClient.getInstance().messageManager.getHistoryMessageListReverse(new OnBase<List<Message>>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(List<Message> data) {
                if (firstChatHistory) {
                    data.add(0, startMsg);
                    firstChatHistory = false;
                }
                handleMessage(data, true);
            }

        }, otherSideID, groupID, null, startMsg, count * 50);


    }

    /**
     * 单聊呼叫
     *
     * @param isVideoCalls
     */
    public void singleChatCall(boolean isVideoCalls) {
        if (null == callingService) return;
        List<String> ids = new ArrayList<>();
        ids.add(otherSideID);
        SignalingInfo signalingInfo = IMUtil.buildSignalingInfo(isVideoCalls, true, ids, null);
        callingService.call(signalingInfo);
    }

    public void toast(String tips) {
        getIView().toast(tips);
    }


    public interface ViewAction extends IView {
        void scrollToPosition(int position);

        void closePage();
    }
}
