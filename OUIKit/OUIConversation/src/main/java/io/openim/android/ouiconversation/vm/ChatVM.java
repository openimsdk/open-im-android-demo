package io.openim.android.ouiconversation.vm;


import static io.openim.android.ouicore.utils.Common.UIHandler;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import javax.annotation.Nullable;

import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.entity.BurnAfterReadingNotification;
import io.openim.android.ouicore.entity.JoinKickedGroupNotification;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.entity.NotificationMsg;
import io.openim.android.ouicore.ex.AtUser;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.IView;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;
import io.openim.android.ouicore.vm.PreviewMediaVM;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;

import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.enums.GroupRole;
import io.openim.android.sdk.enums.GroupType;
import io.openim.android.sdk.enums.MessageStatus;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.enums.Platform;
import io.openim.android.sdk.enums.ViewType;
import io.openim.android.sdk.listener.OnAdvanceMsgListener;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConversationListener;
import io.openim.android.sdk.listener.OnFriendshipListener;
import io.openim.android.sdk.listener.OnGroupListener;
import io.openim.android.sdk.listener.OnMsgSendCallback;
import io.openim.android.sdk.listener.OnUserListener;
import io.openim.android.sdk.models.AdvancedMessage;
import io.openim.android.sdk.models.C2CReadReceiptInfo;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.ConversationReq;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.FriendshipInfo;
import io.openim.android.sdk.models.GroupHasReadInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.GroupMessageReadInfo;
import io.openim.android.sdk.models.GroupMessageReceipt;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.NotDisturbInfo;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.PictureElem;
import io.openim.android.sdk.models.PublicUserInfo;
import io.openim.android.sdk.models.QuoteElem;
import io.openim.android.sdk.models.RevokedInfo;
import io.openim.android.sdk.models.RoomCallingInfo;
import io.openim.android.sdk.models.SearchResult;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.TextElem;
import io.openim.android.sdk.models.UserInfo;
import io.openim.android.sdk.models.UsersOnlineStatus;
import io.openim.android.sdk.models.VideoElem;
import io.openim.android.sdk.utils.JsonUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.plugins.RxJavaPlugins;

public class ChatVM extends BaseViewModel<ChatVM.ViewAction> implements OnAdvanceMsgListener,
    OnGroupListener, OnConversationListener, OnUserListener, OnFriendshipListener {
    private final String TAG = "ChatVM";
    public static final String REEDIT_MSG = "reeditMsg";
    //图片、视频消息 用于预览
    public List<PreviewMediaVM.MediaData> mediaDataList = new ArrayList<>();

    public CallingService callingService =
        (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
    //禁言timer
    private Timer banTimer;
    //阅后即焚计时器
    private final Map<String, Timer> vanishTimerMap = new HashMap<>();
    //回复消息
    public State<Message> replyMessage = new State<>();
    //通知消息
    public State<ConversationInfo> conversationInfo = new State<>();
    public State<GroupInfo> groupInfo = new State<>();
    public State<GroupMembersInfo> memberInfo = new State<>();
    public State<NotificationMsg> notificationMsg = new State<>();
    public State<List<Message>> messages = new State<>(new ArrayList<>());
    public ObservableBoolean typing = new ObservableBoolean(false);
    public State<CharSequence> inputMsg = new State<>("");
    public State<Boolean> isNoData = new State<>(false);

    //开启多选
    public State<Boolean> enableMultipleSelect = new State<>();
    //是否加入群
    public State<Boolean> isJoinGroup = new State<>(true);
    public String userOriginAvatar = "";
    public State<String> userAvatar = new State<>(null);

    private UserOnlineStatusListener userOnlineStatusListener;

    public boolean viewPause = false;
    private MessageAdapter messageAdapter;
    private Observer<CharSequence> inputObserver;
    public Message startMsg = null; // 消息体，取界面上显示的消息体对象/搜索时的起始坐标
    //userID 与 GROUP_ID 互斥
    public String userID = ""; // 接受消息的用户ID
    public String groupID = ""; // 接受消息的群ID
    public String conversationID; //会话id
    public boolean isSingleChat = true; //是否单聊 false 群聊
    public boolean isVideoCall = true;//是否是视频通话

    public boolean fromChatHistory = false;//从查看聊天记录跳转过来
    public boolean firstChatHistory = true;// //用于第一次消息定位
    public boolean isAdminOrCreator = false;// 为true 则是管理员或群主

    public int count = 20; //条数
    public Message loading;
    private final List<Message> mMsgList = new ArrayList<>();
    public final MutableLiveData<Boolean> mTypingState = new MutableLiveData<>(false);
    public final Runnable finishInputting = () -> mTypingState.postValue(false);

    public void init() {
        loading = new Message();
        loading.setContentType(Constants.LOADING);
        //获取会话信息
        getConversationInfo();

        IMEvent.getInstance().addAdvanceMsgListener(this);
        IMEvent.getInstance().addConversationListener(this);
        if (isSingleChat) {
            listener();
            IMEvent.getInstance().addFriendListener(this);
        } else {
            getGroupPermissions();
            IMEvent.getInstance().addGroupListener(this);
        }
    }

    /**
     *
     * 获取自己在这个群的权限
     */
    private void getGroupPermissions() {
        List<String> uid = new ArrayList<>();
        if (null == BaseApp.inst().loginCertificate) return;
        uid.add(BaseApp.inst().loginCertificate.userID);
        OpenIMClient.getInstance().groupManager.getGroupMembersInfo(new OnBase<List<GroupMembersInfo>>() {
            @Override
            public void onError(int code, String error) {
                toast(error + "(" + code + ")");
            }

            @Override
            public void onSuccess(List<GroupMembersInfo> data) {
                if (data.isEmpty()) return;
                GroupMembersInfo membersInfo = data.get(0);
                isAdminOrCreator = membersInfo.getRoleLevel() != GroupRole.MEMBER;
                ban(getMuteEndTime(membersInfo));
                memberInfo.setValue(membersInfo);
            }
        }, groupID, uid);
    }

    public long getMuteEndTime(GroupMembersInfo membersInfo) {
        return membersInfo.getMuteEndTime();
//        return 1705999140000L;
    }

    @Override
    protected void viewPause() {
        viewPause = true;
    }

    @Override
    protected void viewResume() {
        viewPause = false;
    }

    /**
     * 清空选择的msg
     */
    public void clearSelectMsg() {
        for (Message message : messageAdapter.getMessages()) {
            MsgExpand msgExpand = (MsgExpand) message.getExt();
            if (null != msgExpand) {
                msgExpand.isChoice = false;
                message.setExt(msgExpand);
            }
        }
    }

    @Override
    public void onGroupInfoChanged(GroupInfo info) {
        if (info.getGroupID().equals(groupID)) groupInfo.setValue(info);
    }


    @Override
    public void onGroupMemberInfoChanged(GroupMembersInfo info) {
        if (info.getGroupID().equals(groupID) && info.getUserID().equals(BaseApp.inst().loginCertificate.userID)) {
            isAdminOrCreator = info.getRoleLevel() != GroupRole.MEMBER;
            memberInfo.setValue(info);
            ban(getMuteEndTime(info));
        }
        updateMemberInfo(info);
    }

    //禁言或取消禁言
    private void ban(long muteEndTime) {
        long endTime = muteEndTime - System.currentTimeMillis();
        cancelBanTimer();
        if (endTime > 0) {
            banTimer = new Timer();
            banTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    cancelBanTimer();
                    memberInfo.postValue(memberInfo.val());
                }
            }, endTime);
        }
    }

    private void cancelBanTimer() {
        if (null != banTimer) {
            banTimer.cancel();
            banTimer = null;
        }
    }

    private void updateMemberInfo(GroupMembersInfo info) {
        for (Message message : messages.val()) {
            try {
                if (message.getContentType() >= MessageType.NTF_BEGIN) continue;
                if (message.getSendID().equals(info.getUserID())) {
                    message.setSenderNickname(info.getNickname());
                    message.setSenderFaceUrl(info.getFaceURL());
                    int index;
                    if ((index = messages.val().indexOf(message)) != -1) {
                        messageAdapter.notifyItemChanged(index);
                    }
                }
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void onConversationChanged(List<ConversationInfo> list) {
        try {
            for (ConversationInfo info : list) {
                if (info.getConversationID().equals(conversationInfo.val().getConversationID()))
                    conversationInfo.setValue(info);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onUserStatusChanged(UsersOnlineStatus onlineStatus) {
        if (onlineStatus.getUserID().equals(userID) && null != userOnlineStatusListener) {
            userOnlineStatusListener.onResult(onlineStatus);
        }
    }

    public void deleteMessageFromLocalAndSvr(Message message) {
        OpenIMClient.getInstance().messageManager.deleteMessageFromLocalAndSvr(conversationID,
            message.getClientMsgID(), new OnBase<String>() {
                @Override
                public void onError(int code, String error) {
                    deleteMessageFromLocalStorage(message);
                }

                @Override
                public void onSuccess(String data) {
                    removeMsList(message);
                }
            });
    }

    public interface UserOnlineStatusListener {
        void onResult(UsersOnlineStatus onlineStatus);
    }

    public void getConversationInfo() {
        if (isSingleChat) {
            getOneConversation(null);
        } else {
            getGroupsInfo(groupID, null);
            OpenIMClient.getInstance().groupManager.isJoinGroup(groupID, new OnBase<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    isJoinGroup.setValue(data);
                }
            });
        }
    }

    public void getGroupsInfo(String groupID,
                              IMUtil.OnSuccessListener<List<GroupInfo>> OnSuccessListener) {
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
                if (null != OnSuccessListener) {
                    OnSuccessListener.onSuccess(data);
                    return;
                }
                groupInfo.setValue(data.get(0));
                getOneConversation(null);
            }
        }, groupIds);
    }

    private boolean isWordGroup() {
        return groupInfo.getValue().getGroupType() == GroupType.WORK;
    }

    public void getOneConversation(IMUtil.OnSuccessListener<ConversationInfo> OnSuccessListener) {
        OpenIMClient.getInstance().conversationManager.getOneConversation(new OnBase<ConversationInfo>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error);
            }

            @Override
            public void onSuccess(ConversationInfo data) {
                if (null != OnSuccessListener) {
                    OnSuccessListener.onSuccess(data);
                    return;
                }
                conversationID = data.getConversationID();
                conversationInfo.setValue(data);
                loadHistory();
                if (data.getUnreadCount() > 0)
                    markRead();
            }
        }, isSingleChat ? userID : groupID, isSingleChat ? ConversationType.SINGLE_CHAT :
            isWordGroup() ? ConversationType.SUPER_GROUP_CHAT : ConversationType.GROUP_CHAT);
    }

    private void loadHistory() {
        //加载消息记录
        if (fromChatHistory) loadHistoryMessageReverse();
        else loadHistoryMessage();
    }

    @Override
    protected void releaseRes() {
        super.releaseRes();
        IMEvent.getInstance().removeAdvanceMsgListener(this);
        IMEvent.getInstance().removeGroupListener(this);
        IMEvent.getInstance().removeConversationListener(this);
        IMEvent.getInstance().removeUserListener(this);
        for (Timer t : vanishTimerMap.values()) t.cancel();
        vanishTimerMap.clear();
        inputMsg.removeObserver(inputObserver);
    }

    /**
     * 标记已读
     *
     * @param msgList 为null 清除里列表小红点
     */
    public void markRead(@Nullable Message... msgList) {
        RxJavaPlugins.setErrorHandler(handler -> {
            if (handler.getCause() instanceof UndeliverableException) {
                L.e(handler.getMessage());
            }
        });
        markReadWithObservable(msgList).subscribe(new DisposableObserver<String>() {
            @Override
            public void onNext(String string) {
                try {
                    if (null != msgList) {
                        long currentTimeMillis = System.currentTimeMillis();
                        for (Message msg : msgList) {
                            msg.setRead(true);

                            if (null != msg.getAttachedInfoElem() && msg.getAttachedInfoElem().isPrivateChat()) {
                                msg.getAttachedInfoElem().setHasReadTime(currentTimeMillis);
                            }
                            messageAdapter.notifyItemChanged(messages.val().indexOf(msg));
                            scheduleVanish(msg);
                        }
                    }
                } catch (Exception e) {
                    L.e(e.getMessage());
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public Observable<String> markReadWithObservable(@Nullable Message... msgList) {
        RxJavaPlugins.setErrorHandler(handler -> {});
        return Observable.create(emitter -> {
            if (TextUtils.isEmpty(conversationID)) emitter.onError(new Exception("the conversationId is null value. "));

            List<String> msgIDs = new ArrayList<>();
            if (null != msgList) {
                for (Message msg : msgList) {
                    if (msg.getSeq() != 0) {
                        msgIDs.add(msg.getClientMsgID());
                        Log.d(TAG, "msg.getSenderNickname() = " + msg.getSenderNickname());
                    }
                }
            }
            OnBase<String> callBack = new IMUtil.IMCallBack<String>() {

                @Override
                public void onSuccess(String data) {
                    if (!emitter.isDisposed()) {
                        emitter.onNext(data);
                        emitter.onComplete();
                    }
                }

                @Override
                public void onError(int code, String error) {
                    if (!emitter.isDisposed())
                        emitter.onError(new Exception(code+error));
                }
            };
            try {
                if (null == msgList || 0 == msgList.length || isSingleChat) {
                    OpenIMClient.getInstance().messageManager.markConversationMessageAsRead(conversationID, callBack);
                } else {
                    NotificationManager manager =
                        (NotificationManager) BaseApp.inst().getSystemService(Context.NOTIFICATION_SERVICE);
                    for (String msgID : msgIDs) {
                        manager.cancel(msgID.hashCode());
                    }
                }
            } catch (Exception e) {
                if (!emitter.isDisposed()) emitter.onError(e);
            }
        });
    }

    /**
     * 标记已读
     * By conversationID
     */
    public void markReadedByConID(String conversationID,
                                  IMUtil.OnSuccessListener OnSuccessListener) {
        OpenIMClient.getInstance().messageManager.markMessageAsReadByConID(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                if (!TextUtils.isEmpty(error + code))
                    getIView().toast(error + code);
            }

            @Override
            public void onSuccess(String data) {
                if (null != OnSuccessListener) OnSuccessListener.onSuccess(data);
            }
        }, conversationID);
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
            }, userID, "");
        });
        IMEvent.getInstance().addUserListener(this);
    }

    public void setMessageAdapter(MessageAdapter messageAdapter) {
        this.messageAdapter = messageAdapter;
    }

    public void loadHistoryMessage() {
        OpenIMClient.getInstance().messageManager.getAdvancedHistoryMessageList(new OnBase<AdvancedMessage>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + code);
            }

            @Override
            public void onSuccess(AdvancedMessage data) {
                handleMessage(data.getMessageList(), false);
            }
        }, conversationID, startMsg, count, ViewType.History);
    }

    private final List<Message> fullMessages = new ArrayList<>();
    public void loadHistoryMessageInsertFull(int count, boolean isReverse) {
        OpenIMClient.getInstance().messageManager.getAdvancedHistoryMessageList(new OnBase<AdvancedMessage>() {
            @Override
            public void onError(int code, String error) {
                if (getIView() != null) getIView().toast(error+code);
                if (!fullMessages.isEmpty()) {
                    handleMessage(fullMessages, isReverse);
                    fullMessages.clear();
                }
            }

            @Override
            public void onSuccess(AdvancedMessage data) {
                fullMessages.addAll(data.getMessageList());
                if (!data.isEnd()) {
                    loadHistoryMessageInsertFull(count - data.getMessageList().size(), isReverse);
                } else {
                    handleMessage(fullMessages, isReverse);
                    fullMessages.clear();
                }
            }
        }, conversationID, startMsg, count, ViewType.History);
    }

    private void handleMessage(List<Message> data, boolean isReverse) {
        Iterator<Message> iterator = data.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (IMUtil.isSignalingMsg(message))
                iterator.remove();
            else
                IMUtil.buildExpandInfo(message);
        }

        List<Message> list = messages.val();
        if (data.isEmpty()) {
            if (!messages.val().isEmpty()) {
                isNoData.setValue(true);
                removeLoading(list);
            }
            return;
        }
        startMsg = data.get(0);
        Collections.reverse(data);
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
        if (!new HashSet<>(list).containsAll(data)) list.addAll(data);
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
        if (fromChatHistory) return;
        int size = messages.getValue().size();
        lastVisiblePosition += 1;
        if (lastVisiblePosition > size || firstVisiblePosition < 0) return;
        List<Message> megs = new ArrayList<>();
        megs.addAll(messages.getValue().subList(firstVisiblePosition, lastVisiblePosition));
        Iterator<Message> iterator = megs.iterator();
        try {
            while (iterator.hasNext()) {
                Message meg = iterator.next();
                if (meg.isRead() || meg.getContentType() >= MessageType.NTF_BEGIN || meg.getContentType() == MessageType.VOICE || (null == meg.getSendID() || meg.getSendID().equals(BaseApp.inst().loginCertificate.userID)))
                    iterator.remove();
            }
        } catch (Exception ignored) {
        }
        if (!megs.isEmpty()) {
            markRead(megs.toArray(new Message[0]));
        }

    }

    private final Runnable typRunnable = () -> typing.set(false);

    /// 是当前聊天窗口
    Boolean isCurrentChat(Message message) {
        String senderId = message.getSendID();
        String receiverId = message.getRecvID();
        String groupId = message.getGroupID();
        boolean isCurSingleChat =
            message.getSessionType() == ConversationType.SINGLE_CHAT && isSingleChat && (senderId.equals(userID) || receiverId.equals(userID));
        boolean isCurGroupChat =
            message.getSessionType() != ConversationType.SINGLE_CHAT && !isSingleChat && groupID.equals(groupId);
        return isCurSingleChat || isCurGroupChat;
    }


    /**
     * 接收到新消息
     *
     * @param msg
     */
    @Override
    public void onRecvNewMessage(Message msg) {
        if (msg.getContentType() == 1701) {
            BurnAfterReadingNotification notification = GsonHel.fromJson(
                msg.getNotificationElem().getDetail(), BurnAfterReadingNotification.class);
            SharedPreferencesUtil.get(BaseApp.inst()).setCache(Constants.SP_Prefix_ReadVanish + notification.conversationID, notification.isPrivate ? 1 : 0);
        }
        if (!isCurrentChat(msg) || IMUtil.isSignalingMsg(msg)) return;
        boolean isTyp = msg.getContentType() == MessageType.TYPING;
        if (isSingleChat) {
            if (msg.getSendID().equals(userID)) {
                getUserInputState(isTyping -> {
                    if (isTyping) {
                        typing.set(true);
                        UIHandler.removeCallbacks(typRunnable);
                        UIHandler.postDelayed(typRunnable, 3000L);
                    }

                });
            }
        }
        if (isTyp) return;
        messages.val().add(0, IMUtil.buildExpandInfo(msg));
        startMsg = msg;
        int[] positions = messageAdapter.getCurrentPageItemPositions();
        if (positions[0] <= 0) {
            UIHandler.post(() -> {
                getIView().scrollToPosition(0);
                messageAdapter.notifyItemInserted(0);
            });
        } else {
            UIHandler.post(() -> {
                messageAdapter.notifyItemInserted(0);
            });
        }
        if (isSingleChat) {
            //标记本条消息已读 语音消息需要点播放才算读
            if (!viewPause && msg.getContentType() != MessageType.VOICE) {
                UIHandler.post(() -> markRead(msg));
            }
        }
        else {
            int last = positions[1];
            for (int i = positions[0]; i < last; i++) {
                if (messageAdapter.getMessages().get(i).getClientMsgID() == msg.getClientMsgID()) {
                    mMsgList.add(msg);
                }
            }
            //标记本条消息已读 语音消息需要点播放才算读
            if (mMsgList.size() > 0 && !viewPause && msg.getContentType() != MessageType.VOICE) {
                markRead(mMsgList.toArray(new Message[0]));
                mMsgList.clear();
            }
        }
    }

    private void getUserInputState(Consumer<Boolean> callback) {
        OpenIMClient.getInstance().conversationManager.getInputStates(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                callback.accept(false);
            }

            @Override
            public void onSuccess(String data) {
                callback.accept(true);
            }
        }, conversationID, userID);
    }

    @Override
    public void onRecvC2CReadReceipt(List<C2CReadReceiptInfo> list) {
        try {
            for (C2CReadReceiptInfo readInfo : list) {
                if (readInfo.getUserID().equals(userID)) {
                    for (int i = 0; i < messages.val().size(); i++) {
                        Message message = messages.val().get(i);
                        if (readInfo.getMsgIDList().contains(message.getClientMsgID())) {
                            message.setRead(true);
                            if (null != message.getAttachedInfoElem() && message.getAttachedInfoElem().isPrivateChat()) {
                                message.getAttachedInfoElem().setHasReadTime(readInfo.getReadTime());
                            }
                            messageAdapter.notifyItemChanged(i);
                            scheduleVanish(message);
                        }
                    }

                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRecvGroupMessageReadReceipt(GroupMessageReceipt receipt) {
        try {
            if (receipt.getConversationID().equals(conversationID)) {
                List<GroupMessageReadInfo> groupMessageReadInfo = receipt.getGroupMessageReadInfo();
                for (GroupMessageReadInfo messageReadInfo : groupMessageReadInfo) {
                    Message message = new Message();
                    message.setClientMsgID(messageReadInfo.getClientMsgID());
                    int index = messages.val().indexOf(message);
                    if (index == -1) continue;
                    Message localMsg = messages.val().get(index);
                    GroupHasReadInfo hasReadInfo =
                        localMsg.getAttachedInfoElem().getGroupHasReadInfo();
                    hasReadInfo.setHasReadCount(messageReadInfo.getHasReadCount());
                    hasReadInfo.setUnreadCount(messageReadInfo.getUnreadCount());
                    Log.d(TAG, "onRecvGroupMessageReadReceipt unread:" + messageReadInfo.getUnreadCount() +
                        ",read:" + messageReadInfo.getHasReadCount() + ",msgid:" + message.getClientMsgID() + "groupid:" + message.getGroupID() +
                        ", recvid:" + message.getRecvID() + ",nickname:" + message.getSenderNickname());
                    messageAdapter.notifyItemChanged(index);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void sendMsg(Message msg) {
        sendMsg(msg, false);
    }

    public void sendMsg(Message msg, boolean isResend) {
        if (isResend) {
            int orIndex = messages.val().indexOf(msg);
            messages.val().remove(orIndex);
            messageAdapter.notifyItemRemoved(orIndex);
            messages.val().add(0, msg);
            messageAdapter.notifyItemInserted(0);
        }
        //这里最好不要改变msg其他的变量
        msg.setStatus(MessageStatus.SENDING);
        if (messages.val().contains(msg)) {
            messageAdapter.notifyItemChanged(messages.val().indexOf(msg));
        } else {
            messages.val().add(0, IMUtil.buildExpandInfo(msg));
            messageAdapter.notifyItemInserted(0);
            getIView().scrollToPosition(0);
        }
        final MsgExpand ext = (MsgExpand) msg.getExt();
        OfflinePushInfo offlinePushInfo = new OfflinePushInfo();  // 离线推送的消息备注；不为null
        OpenIMClient.getInstance().messageManager.sendMessage(new OnMsgSendCallback() {
            @Override
            public void onError(int code, String error) {
                UIHandler.postDelayed(() -> {
                    msg.setExt(ext);
                    msg.setStatus(MessageStatus.FAILED);
                    messageAdapter.notifyItemChanged(messages.val().indexOf(msg));
                }, 500);
            }

            @Override
            public void onProgress(long progress) {
            }

            @Override
            public void onSuccess(Message message) {
                UIHandler.post(() -> {
                    // 返回新的消息体；替换发送传入的，不然撤回消息会有bug
                    int index = messages.val().indexOf(msg);
                    messages.val().remove(index);
                    messages.val().add(index, IMUtil.buildExpandInfo(message));
                    IMUtil.calChatTimeInterval(messages.val());
                    messageAdapter.notifyItemChanged(index);
                });
            }
        }, msg, userID, groupID, offlinePushInfo);
    }

    public void aloneSendMsg(Message msg, String userID, String otherSideGroupID) {
        aloneSendMsg(msg, userID, otherSideGroupID, null);
    }

    /**
     * 独立发送
     */
    public void aloneSendMsg(Message msg, String userID, String otherSideGroupID,
                             OnMsgSendCallback onMsgSendCallback) {
        if (this.userID.equals(userID) || groupID.equals(otherSideGroupID)) {
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
                    try {
                        getIView().toast(getContext().getString(io.openim.android.ouicore.R.string.send_succ));
                    } catch (Exception ignored) {
                    }
                }
            };
        }
        OpenIMClient.getInstance().messageManager.sendMessage(onMsgSendCallback, msg, userID,
            otherSideGroupID, offlinePushInfo);
    }

    @Override
    public void onRecvMessageRevokedV2(RevokedInfo info) {

    }

    public void deleteMessageFromLocalStorage(Message message) {
        OpenIMClient.getInstance().messageManager.deleteMessageFromLocalStorage(conversationID,
            message.getClientMsgID(), new OnBase<String>() {
                @Override
                public void onError(int code, String error) {
                    getIView().toast(error + "(" + code + ")");
                }

                @Override
                public void onSuccess(String data) {
                    removeMsList(message);
                }
            });
    }

    private void removeMsList(Message message) {
        if (messageAdapter.getMessages().contains(message)) {
            int index = messages.getValue().indexOf(message);
            messageAdapter.getMessages().remove(index);
            messageAdapter.notifyItemRemoved(index);
            enableMultipleSelect.setValue(false);
        }
    }

    private void scheduleVanish(Message message) {
        try {
            if (null == message.getAttachedInfoElem() || !message.getAttachedInfoElem().isPrivateChat())
                return;
            long sec = SharedPreferencesUtil.get(BaseApp.inst())
                .getLong(Constants.SP_Prefix_ReadVanishTime + conversationID);
            if (sec <= 0) sec = Constants.DEFAULT_VANISH_SECOND;

            if (sec <= 0) return;
            Timer timer = new Timer();
            vanishTimerMap.put(message.getClientMsgID(), timer);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    UIHandler.post(() -> deleteMessageFromLocalStorage(message));
                }
            }, sec * 1000);
        } catch (Exception ignored) {
        }
    }


    public void closePage() {
        getIView().closePage();
    }

    public void loadHistoryMessageReverse() {
        OpenIMClient.getInstance().messageManager.getAdvancedHistoryMessageListReverse(new OnBase<AdvancedMessage>() {
            @Override
            public void onError(int code, String error) {
            }

            @Override
            public void onSuccess(AdvancedMessage data) {
                List<Message> messageList = data.getMessageList();
                if (firstChatHistory) {
                    messageList.add(0, startMsg);
                    firstChatHistory = false;
                }
                handleMessage(messageList, true);
            }

        }, conversationID, startMsg, count * 80, ViewType.History);
    }


    @Override
    public void onGroupMemberAdded(GroupMembersInfo info) {
        if (info.getGroupID().equals(groupID) && info.getUserID().equals(BaseApp.inst().loginCertificate.userID)) {
            isJoinGroup.setValue(true);
            getGroupsInfo(groupID, null);
        }
    }

    @Override
    public void onGroupMemberDeleted(GroupMembersInfo info) {
        if (info.getGroupID().equals(groupID) && info.getUserID().equals(BaseApp.inst().loginCertificate.userID)) {
            isJoinGroup.setValue(false);
        }
    }

    public void singleChatCall(boolean isVideoCalls) {
        if (null == callingService) return;
        List<String> ids = new ArrayList<>();
        ids.add(userID);
        SignalingInfo signalingInfo = IMUtil.buildSignalingInfo(isVideoCalls, ids);
        callingService.call(signalingInfo);
    }

    public void toast(String tips) {
        if (getIView() != null)
            getIView().toast(tips);
        else
            Toast.makeText(BaseApp.inst(), tips, Toast.LENGTH_SHORT).show();
    }


    public interface ViewAction extends IView {
        void scrollToPosition(int position);

        void closePage();
    }

    @Override
    public void onSyncServerFailed(boolean reinstall) {
    }

    @Override
    public void onSyncServerFinish(boolean reinstall) {
    }

    @Override
    public  void onSyncServerStart(boolean reinstall) {
    }

    public void groupNotificationOperator(ConversationInfo conversationInfo) {
        if (conversationInfo.getConversationType() != ConversationType.SUPER_GROUP_CHAT || TextUtils.isEmpty(conversationInfo.getLatestMsg())) {
            return;
        }
        Message latestMsg = JsonUtil.toObj(conversationInfo.getLatestMsg(), Message.class);
        switch (latestMsg.getContentType()) {
            // 群成员被踢通知
            case MessageType.MEMBER_KICKED_NTF:
                JoinKickedGroupNotification kickedNotification = GsonHel.fromJson(latestMsg.getNotificationElem().getDetail(), JoinKickedGroupNotification.class);
                List<GroupMembersInfo> kickedUsers = kickedNotification.kickedUserList;
                for (GroupMembersInfo kickedUser : kickedUsers) {
                    if (BaseApp.inst().loginCertificate.userID.equals(kickedUser.getUserID())) {
                        isJoinGroup.setValue(false);
                        return;
                    }
                }
                break;
            // 群成员被邀请加入通知
            case MessageType.MEMBER_INVITED_NTF:
                JoinKickedGroupNotification joinGroupNotification = GsonHel.fromJson(latestMsg.getNotificationElem().getDetail(), JoinKickedGroupNotification.class);
                List<GroupMembersInfo> joinUsers = joinGroupNotification.invitedUserList;
                for (GroupMembersInfo joinUser : joinUsers) {
                    if (BaseApp.inst().loginCertificate.userID.equals(joinUser.getUserID())) {
                        isJoinGroup.setValue(true);
                        return;
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onFriendInfoChanged(FriendInfo u) {
        if (!TextUtils.isEmpty(u.getFaceURL()) && !TextUtils.isEmpty(userOriginAvatar) && !userOriginAvatar.equals(u.getFaceURL()))
            userAvatar.setValue(u.getFaceURL());
    }
}
