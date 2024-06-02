package io.openim.android.ouiconversation.vm;


import static io.openim.android.ouicore.utils.Common.UIHandler;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.Observer;


import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import javax.annotation.Nullable;

import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.State;
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
import io.openim.android.sdk.listener.BaseImpl;
import io.openim.android.sdk.listener.OnAdvanceMsgListener;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConversationListener;
import io.openim.android.sdk.listener.OnGroupListener;
import io.openim.android.sdk.listener.OnMsgSendCallback;
import io.openim.android.sdk.listener.OnSignalingListener;
import io.openim.android.sdk.listener.OnUserListener;
import io.openim.android.sdk.models.AdvancedMessage;
import io.openim.android.sdk.models.C2CReadReceiptInfo;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.GroupHasReadInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.GroupMessageReadInfo;
import io.openim.android.sdk.models.GroupMessageReceipt;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.NotDisturbInfo;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.PictureElem;
import io.openim.android.sdk.models.QuoteElem;
import io.openim.android.sdk.models.RevokedInfo;
import io.openim.android.sdk.models.RoomCallingInfo;
import io.openim.android.sdk.models.SearchResult;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.TextElem;
import io.openim.android.sdk.models.UsersOnlineStatus;
import io.openim.android.sdk.models.VideoElem;
import io.openim.android.sdk.utils.ParamsUtil;
import open_im_sdk.Open_im_sdk;
import open_im_sdk_callback.Base;

public class ChatVM extends BaseViewModel<ChatVM.ViewAction> implements OnAdvanceMsgListener,
    OnGroupListener, OnConversationListener, OnSignalingListener, OnUserListener {

    public static final String REEDIT_MSG = "reeditMsg";
    //图片、视频消息 用于预览
    public List<PreviewMediaVM.MediaData> mediaDataList = new ArrayList<>();

    public CallingService callingService =
        (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
    //阅后即焚Timers
    HashMap<String, Timer> readVanishTimers = new HashMap<>();
    //禁言timer
    private Timer banTimer;

    //搜索的本地消息
    public State<List<Message>> searchMessageItems = new State<>(new ArrayList<>());
    public State<List<Message>> addSearchMessageItems = new State<>(new ArrayList<>());
    //回复消息
    public State<Message> replyMessage = new State<>();
    //免打扰状态
    public State<Integer> notDisturbStatus = new State<>(0);
    //通知消息
    public State<ConversationInfo> conversationInfo = new State<>();
    public State<GroupInfo> groupInfo = new State<>();
    public State<GroupMembersInfo> memberInfo = new State<>();
    public State<NotificationMsg> notificationMsg = new State<>();
    public State<List<Message>> messages = new State<>(new ArrayList<>());
    //@消息
    public State<List<AtUser>> atUsers = new State<>(new ArrayList<>());
    //表情
    public State<List<String>> emojiMessages = new State<>(new ArrayList<>());
    //会议流
    public State<RoomCallingInfo> roomCallingInfo = new State<>();
    public ObservableBoolean typing = new ObservableBoolean(false);
    public State<CharSequence> inputMsg = new State<>("");
    public State<Boolean> isNoData = new State<>(false);

    //开启多选
    public State<Boolean> enableMultipleSelect = new State<>();
    //是否加入群
    public State<Boolean> isJoinGroup = new State<>(true);

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


    public void init() {
        loading = new Message();
        loading.setContentType(Constants.LOADING);
        //获取会话信息
        getConversationInfo();

        IMEvent.getInstance().addAdvanceMsgListener(this);
        IMEvent.getInstance().addConversationListener(this);
        IMEvent.getInstance().addSignalingListener(this);
        if (isSingleChat) {
            listener();
        } else {
            getGroupPermissions();
            IMEvent.getInstance().addGroupListener(this);
            signalingGetRoomByGroupID();
        }
    }

    private void signalingGetRoomByGroupID() {
        OpenIMClient.getInstance().signalingManager.signalingGetRoomByGroupID(new IMUtil.IMCallBack<RoomCallingInfo>() {
            @Override
            public void onError(int code, String error) {
            }

            @Override
            public void onSuccess(RoomCallingInfo data) {
                roomCallingInfo.setValue(data);
            }
        }, groupID);
    }

    public void signalingGetTokenByRoomID(String roomID) {
        OpenIMClient.getInstance().signalingManager.signalingGetRoomByGroupID(new OnBase<RoomCallingInfo>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(RoomCallingInfo data) {
                if (null == data.getInvitation()) {
                    getIView().toast(BaseApp.inst().getString(io.openim.android.ouicore.R.string.not_err));
                    return;
                }
                SignalingInfo signalingInfo = new SignalingInfo();
                signalingInfo.setInvitation(data.getInvitation());
                callingService.join(signalingInfo);
            }
        }, roomID);
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


    //获取在线状态
    public void getUserOnlineStatus(UserOnlineStatusListener userOnlineStatusListener) {
        this.userOnlineStatusListener = userOnlineStatusListener;
        List<String> uIds = new ArrayList<>();
        uIds.add(userID);
        OpenIMClient.getInstance().userInfoManager.subscribeUsersOnlineStatus(new OnBase<List<UsersOnlineStatus>>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(List<UsersOnlineStatus> data) {
                if (null == data || data.isEmpty()) return;
                userOnlineStatusListener.onResult(data.get(0));
            }
        }, uIds);

    }

    public String handlePlatformCode(List<Integer> platformList) {
        List<String> pList = new ArrayList<>();
        for (int platform : platformList) {
            if (platform == Platform.ANDROID) {
                pList.add(getContext().getString(io.openim.android.ouicore.R.string.android));
            } else if (platform == Platform.IOS) {
                pList.add(getContext().getString(io.openim.android.ouicore.R.string.ios));
            } else if (platform == Platform.XOS) {
                pList.add("Mac");
            } else if (platform == Platform.WIN) {
                pList.add(getContext().getString(io.openim.android.ouicore.R.string.pc));
            } else if (platform == Platform.WEB) {
                pList.add(getContext().getString(io.openim.android.ouicore.R.string.Web));
            } else if (platform == Platform.MINI_WEB) {
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

    /**
     * 存储草稿
     */
    public void cacheDraft(String inputMsg, String conversationID) {
        String cacheKey = conversationID + "_draft";
        String atKey = cacheKey + "_at";
        if (TextUtils.isEmpty(inputMsg)) {
            SharedPreferencesUtil.remove(BaseApp.inst(), cacheKey);
            SharedPreferencesUtil.remove(BaseApp.inst(), atKey);
            return;
        }
        if (!Common.isBlank(inputMsg)) {
            SharedPreferencesUtil.get(BaseApp.inst()).setCache(cacheKey, inputMsg);
        }
        if (!atUsers.val().isEmpty()) {
            String atUser = GsonHel.toJson(atUsers.val());
            SharedPreferencesUtil.get(BaseApp.inst()).setCache(atKey, atUser);
        }
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

    int getReadCountdown(Message message) {
        int burnDuration = message.getAttachedInfoElem().getBurnDuration();
        long hasReadTime = message.getAttachedInfoElem().getHasReadTime();
        if (burnDuration == 0) burnDuration = 30;
        if (hasReadTime > 0) {
            long end = hasReadTime + (burnDuration * 1000L);
            long diff = (end - System.currentTimeMillis()) / 1000;
            return diff < 0 ? 0 : (int) diff;
        }
        return 0;
    }

    @Override
    public void onRoomParticipantConnected(RoomCallingInfo s) {
        if (groupID.equals(s.getGroupID())) {
            roomCallingInfo.setValue(s);
        }
    }

    @Override
    public void onRoomParticipantDisconnected(RoomCallingInfo s) {
        if (groupID.equals(s.getGroupID())) {
            roomCallingInfo.setValue(s);
        }
    }


    public String getRoomCallingInfoRoomID() {
        String roomID = "";
        try {
            roomID = roomCallingInfo.getValue().getRoomID();
            if (TextUtils.isEmpty(roomID))
                roomID = roomCallingInfo.getValue().getInvitation().getRoomID();
        } catch (Exception e) {
        }
        return roomID;
    }

    /**
     * 重新编辑消息
     */
    public void reeditMsg(String content) {
        subject(REEDIT_MSG, content);
    }


    @Override
    public void onUserStatusChanged(UsersOnlineStatus onlineStatus) {
        if (onlineStatus.getUserID().equals(userID) && null != userOnlineStatusListener) {
            userOnlineStatusListener.onResult(onlineStatus);
        }
    }

    public interface UserOnlineStatusListener {
        void onResult(UsersOnlineStatus onlineStatus);
    }

    private void getConversationInfo() {
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
                changeInputStates();
                loadHistory();
                getConversationRecvMessageOpt(data.getConversationID());

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
        IMEvent.getInstance().removeSignalingListener(this);
        IMEvent.getInstance().removeUserListener(this);
        inputMsg.removeObserver(inputObserver);

        cancelBanTimer();
        for (Timer value : readVanishTimers.values()) {
            value.cancel();
        }
        readVanishTimers.clear();
    }

    /**
     * 标记已读
     *
     * @param msgList 为null 清除里列表小红点
     */
    public void markRead(@Nullable Message... msgList) {
        if (TextUtils.isEmpty(conversationID)) return;

        List<String> msgIDs = new ArrayList<>();
        if (null != msgList) {
            for (Message msg : msgList) {
                if (msg.getSeq() != 0) {
                    msgIDs.add(msg.getClientMsgID());
                }
            }
        }
        OnBase<String> callBack = new IMUtil.IMCallBack<String>() {

            @Override
            public void onSuccess(String data) {
                if (null != msgList) {
                    long currentTimeMillis = System.currentTimeMillis();
                    for (Message msg : msgList) {
                        msg.setRead(true);

                        if (null != msg.getAttachedInfoElem() && msg.getAttachedInfoElem().isPrivateChat()) {
                            msg.getAttachedInfoElem().setHasReadTime(currentTimeMillis);
                        }
                        messageAdapter.notifyItemChanged(messages.val().indexOf(msg));
                    }
                }
            }
        };
        if (null == msgList || msgList.length == 0) {
            OpenIMClient.getInstance().messageManager.markConversationMessageAsRead(conversationID, callBack);
        } else {
            if (isSingleChat) {
                OpenIMClient.getInstance().messageManager.markMessagesAsReadByMsgID(conversationID, msgIDs, callBack);
            } else {
                OpenIMClient.getInstance().messageManager.sendGroupMessageReadReceipt(conversationID, msgIDs, callBack);
            }

            NotificationManager manager =
                (NotificationManager) BaseApp.inst().getSystemService(Context.NOTIFICATION_SERVICE);
            for (String msgID : msgIDs) {
                manager.cancel(msgID.hashCode());
            }
        }

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

    private void changeInputStates() {
        Open_im_sdk.changeInputStates(
            new Base() {
                @Override
                public void onError(int i, String s) {
                    L.e("");
                }

                @Override
                public void onSuccess(String s) {
                    L.e("");
                }
            }
            , ParamsUtil.buildOperationID(), conversationID,true);
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
        }, conversationID, startMsg, count);
    }

    private void handleMessage(List<Message> data, boolean isReverse) {
        for (Message datum : data) {
            IMUtil.buildExpandInfo(datum);
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
            lastHandleMsg(data);
            messages.setValue(data);
            return;
        }
        if (isReverse) {
            list.addAll(0, data);
            lastHandleMsg(list);
            messageAdapter.notifyItemRangeInserted(0, data.size());
            return;
        }
        removeLoading(list);
        if (!new HashSet<>(list).containsAll(data)) list.addAll(data);
        lastHandleMsg(list);
        list.add(loading);
        messageAdapter.notifyItemRangeChanged(list.size() - 1 - data.size(), list.size() - 1);
    }

    private void lastHandleMsg(List<Message> list) {
        IMUtil.calChatTimeInterval(list);
        mediaDataList.clear();
        for (Message message : list) {
            addMediaList(message, true);
        }
    }

    /**
     * 添加到媒体集合
     *
     * @param datum
     */
    private void addMediaList(Message datum, boolean isPour) {
        try {
            PreviewMediaVM.MediaData mediaData = null;
            if (datum.getContentType() == MessageType.PICTURE) {
                PictureElem pictureElem = datum.getPictureElem();
                mediaData = new PreviewMediaVM.MediaData(datum.getClientMsgID());
                mediaData.mediaUrl = IMUtil.getFastPicturePath(pictureElem);
                if (null != pictureElem.getSnapshotPicture())
                    mediaData.thumbnail = pictureElem.getSnapshotPicture().getUrl();
            }
            if (datum.getContentType() == MessageType.VIDEO) {
                VideoElem videoElem = datum.getVideoElem();
                mediaData = new PreviewMediaVM.MediaData(datum.getClientMsgID());
                mediaData.isVideo = true;
                mediaData.mediaUrl = IMUtil.getFastVideoPath(videoElem);
                mediaData.thumbnail = videoElem.getSnapshotUrl();
            }
            if (null != mediaData && !mediaDataList.contains(mediaData)) {
                if (isPour)//反着
                    mediaDataList.add(0, mediaData);
                else mediaDataList.add(mediaData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (!megs.isEmpty()) markRead(megs.toArray(new Message[0]));

    }

    private Runnable typRunnable = () -> typing.set(false);

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
        if (!isCurrentChat(msg)) return;
        boolean isTyp = msg.getContentType() == MessageType.TYPING;
        if (isSingleChat) {
            if (msg.getSendID().equals(userID)) {
                typing.set(isTyp);
                if (isTyp) {
                    UIHandler.removeCallbacks(typRunnable);
                    UIHandler.postDelayed(typRunnable, 5000);
                }
            }
        }
        if (isTyp) return;
        messages.val().add(0, IMUtil.buildExpandInfo(msg));
        addMediaList(msg, false);

        UIHandler.post(() -> {
            getIView().scrollToPosition(0);
            messageAdapter.notifyItemInserted(0);
        });

        //标记本条消息已读 语音消息需要点播放才算读
        if (!viewPause && msg.getContentType() != MessageType.VOICE) markRead(msg);

        statusUpdate(msg);
    }

    private void statusUpdate(Message msg) {
        try {
            int contentType = msg.getContentType();
            if (contentType == MessageType.GROUP_ANNOUNCEMENT_NTF) {
                MsgExpand msgExpand = (MsgExpand) msg.getExt();
                if (!TextUtils.isEmpty(msgExpand.notificationMsg.group.notification))
                    notificationMsg.setValue(msgExpand.notificationMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            addMediaList(msg, false);
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
                    ext.sendProgress = 0;
                    messageAdapter.notifyItemChanged(messages.val().indexOf(msg));
                }, 500);
            }

            @Override
            public void onProgress(long progress) {
                UIHandler.post(() -> {
                    msg.setExt(ext);
                    ext.sendProgress = progress;
                    messageAdapter.notifyItemChanged(messages.val().indexOf(msg));
                });
            }

            @Override
            public void onSuccess(Message message) {
                // 返回新的消息体；替换发送传入的，不然撤回消息会有bug
                int index = messages.val().indexOf(msg);
                messages.val().remove(index);
                messages.val().add(index, IMUtil.buildExpandInfo(message));
                lastHandleMsg(messages.val());
                messageAdapter.notifyItemChanged(index);
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
        try {
            for (Message message : messages.val()) {
                QuoteElem quoteElem = message.getQuoteElem();
                Message quoteMessage;
                if (null != quoteElem && null != (quoteMessage = quoteElem.getQuoteMessage()) && quoteMessage.getClientMsgID().equals(info.getClientMsgID())) {
                    //引用消息被删除
                    quoteMessage.setContentType(MessageType.REVOKE_MESSAGE_NTF);
                    messageAdapter.notifyItemChanged(messages.val().indexOf(message));
                }
                if (message.getClientMsgID().equals(info.getClientMsgID())) {
                    message.setContentType(MessageType.REVOKE_MESSAGE_NTF);
                    //a 撤回了一条消息
                    String txt, target;
                    CharSequence tips;
                    if (info.getRevokerID().equals(info.getSourceMessageSendID())) {
                        txt =
                            String.format(BaseApp.inst().getString(io.openim.android.ouicore.R.string.revoke_tips), target = IMUtil.getSelfName(info.getRevokerID(), info.getRevokerNickname()));
                        if (message.getSendID().equals(BaseApp.inst().loginCertificate.userID) && null == message.getSoundElem()) {
                            //只有是自己发的文本才支持重新编辑
                            String reedit =
                                BaseApp.inst().getString(io.openim.android.ouicore.R.string.re_edit);
                            txt += "\t" + reedit;
                            tips =
                                IMUtil.buildClickAndColorSpannable((SpannableStringBuilder) IMUtil.getSingleSequence(message.getGroupID(), target, message.getSendID(), txt), reedit, new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        TextElem txt = message.getTextElem();
                                        if (null != txt) {
                                            reeditMsg(txt.getContent());
                                        }
                                    }
                                });
                        } else {
                            tips = IMUtil.getSingleSequence(message.getGroupID(), target,
                                info.getRevokerID(), txt);
                        }
                    } else {
                        txt =
                            String.format(BaseApp.inst().getString(io.openim.android.ouicore.R.string.revoke_tips2), info.getRevokerNickname(), info.getSourceMessageSenderNickname());

                        tips = IMUtil.twoPeopleRevoker(message, info, txt);
                    }
                    ((MsgExpand) message.getExt()).tips = tips;
                    messageAdapter.notifyItemChanged(messages.getValue().indexOf(message));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 撤回消息
     *
     * @param message
     */
    public void revokeMessage(Message message) {
        OpenIMClient.getInstance().messageManager.revokeMessageV2(new IMUtil.IMCallBack<>(),
            conversationID, message.getClientMsgID());

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

        OpenIMClient.getInstance().messageManager.clearConversationAndDeleteAllMsg(id,
            new OnBase<String>() {
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
                    toast(BaseApp.inst().getString(io.openim.android.ouicore.R.string.clear_succ));
                }
            });
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

    public void setConversationRecvMessageOpt(int status, String cid) {
        OpenIMClient.getInstance().conversationManager.setConversationRecvMessageOpt(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                toast(error + code);
            }

            @Override
            public void onSuccess(String data) {
                notDisturbStatus.setValue(status);
            }
        }, cid, status);
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
            messageTypeLists.add(MessageType.TEXT);
            messageTypeLists.add(MessageType.AT_TEXT);
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

        }, conversationID, startMsg, count * 80);
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


    /**
     * 单聊呼叫
     *
     * @param isVideoCalls
     */
    public void singleChatCall(boolean isVideoCalls) {
        if (null == callingService) return;
        List<String> ids = new ArrayList<>();
        ids.add(userID);
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
