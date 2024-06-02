package io.openim.android.ouicore.im;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.google.gson.reflect.TypeToken;
import com.hjq.permissions.Permission;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.databinding.LayoutBottompopV3Binding;
import io.openim.android.ouicore.entity.AtMsgInfo;
import io.openim.android.ouicore.entity.BurnAfterReadingNotification;
import io.openim.android.ouicore.entity.CallHistory;
import io.openim.android.ouicore.entity.CustomEmojiEntity;
import io.openim.android.ouicore.entity.EnterGroupNotification;
import io.openim.android.ouicore.entity.GroupNotification;
import io.openim.android.ouicore.entity.GroupRightsTransferNotification;
import io.openim.android.ouicore.entity.JoinKickedGroupNotification;
import io.openim.android.ouicore.entity.LocationInfo;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.entity.MeetingInfo;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.entity.MuteMemberNotification;
import io.openim.android.ouicore.entity.NotificationMsg;
import io.openim.android.ouicore.entity.OANotification;
import io.openim.android.ouicore.entity.QuitGroupNotification;
import io.openim.android.ouicore.ex.AtUser;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.MediaPlayerUtil;
import io.openim.android.ouicore.utils.NotificationUtil;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.enums.GroupAtType;
import io.openim.android.sdk.enums.LoginStatus;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.AtUserInfo;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.NotificationElem;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.PictureElem;
import io.openim.android.sdk.models.QuoteElem;
import io.openim.android.sdk.models.RevokedInfo;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.SignalingInvitationInfo;
import io.openim.android.sdk.models.VideoElem;

public class IMUtil {
    //android PlatformID 2
    public static final int PLATFORM_ID = 2;
    public static final String AT_ALL = "AtAllTag";
    private static final String TAG = "IMUtil";

    public static String getFastVideoPath(VideoElem elem) {
        String videoPath = elem.getVideoPath();
        if (!GetFilePathFromUri.fileIsExists(videoPath)) videoPath = elem.getVideoUrl();
        return videoPath;
    }

    public static String getFastPicturePath(PictureElem elem) {
        String path = elem.getSourcePath();
        if (!GetFilePathFromUri.fileIsExists(path)) path = elem.getSourcePicture().getUrl();
        return path;
    }

    /**
     * 加载图片
     * 本地存在直接加载-》缩略图-》加载原图
     *
     * @return
     */
    public static RequestBuilder<?> loadPicture(PictureElem elem) {
        String url = "";
        String filePath = elem.getSourcePath();
        if (GetFilePathFromUri.fileIsExists(filePath)) url = filePath;
        if (TextUtils.isEmpty(url) && null != elem.getSnapshotPicture())
            url = elem.getSnapshotPicture().getUrl();
        if (TextUtils.isEmpty(url)) {
            url = elem.getSourcePicture().getUrl();
        }
        return Glide.with(BaseApp.inst()).load(url).placeholder(R.mipmap.ic_chat_photo).error(R.mipmap.ic_chat_photo);
    }

    /**
     * 加载视频缩略图
     * 判断本地是否存在 本地存在直接加载 不存在加载网络
     *
     * @return
     */
    public static RequestBuilder<?> loadVideoSnapshot(VideoElem elem) {
        //本地
        String path = elem.getSnapshotPath();
        if (!GetFilePathFromUri.fileIsExists(path)) {
            //远程
            path = elem.getSnapshotUrl();
        }
        return Glide.with(BaseApp.inst()).load(path).placeholder(R.mipmap.ic_chat_photo).error(R.mipmap.ic_chat_photo);
    }


    /**
     * 会话排序比较器
     */
    public static Comparator<MsgConversation> simpleComparator() {
        return (a, b) -> {
            if ((a.conversationInfo.isPinned() && b.conversationInfo.isPinned()) || (!a.conversationInfo.isPinned() && !b.conversationInfo.isPinned())) {
                long aCompare = Math.max(a.conversationInfo.getDraftTextTime(),
                    a.conversationInfo.getLatestMsgSendTime());
                long bCompare = Math.max(b.conversationInfo.getDraftTextTime(),
                    b.conversationInfo.getLatestMsgSendTime());
                return Long.compare(bCompare, aCompare);
            } else if (a.conversationInfo.isPinned() && !b.conversationInfo.isPinned()) {
                return -1;
            } else {
                return 1;
            }
        };
    }


    /**
     * 设置时间显示
     *
     * @param list
     * @return
     */
    public static List<Message> calChatTimeInterval(List<Message> list) {
        long lastShowTimeStamp = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            Message message = list.get(i);
            MsgExpand msgExpand = (MsgExpand) message.getExt();
            if (null == msgExpand) msgExpand = new MsgExpand();
            //重置
            msgExpand.isShowTime = false;
            if (message.getContentType() >= MessageType.NTF_BEGIN || message.getContentType() == MessageType.REVOKE_MESSAGE_NTF)
                continue;

            if (lastShowTimeStamp == 0 || (message.getSendTime() - lastShowTimeStamp > (1000 * 60 * 10))) {
                lastShowTimeStamp = message.getSendTime();
                msgExpand.isShowTime = true;
            }
            message.setExt(msgExpand);
        }
        return list;
    }

    public static Message createMergerMessage(String title, List<Message> list) {
        List<String> summaryList = new ArrayList<>();
        for (Message message : list) {
            summaryList.add(message.getSenderNickname() + ":" + getMsgParse(message));
            if (summaryList.size() >= 4) break;
        }

        return OpenIMClient.getInstance().messageManager.createMergerMessage(list, title,
            summaryList);


    }

    /**
     * 是信令消息
     * @param msg
     * @return
     */
    public static boolean isSignalingMsg(Message msg) {
        if (msg.getContentType() == MessageType.CUSTOM) {
            Map map = JSONArray.parseObject(msg.getCustomElem().getData(), Map.class);
            if (map.containsKey(Constants.K_CUSTOM_TYPE)) {
                int customType = (int) map.get(Constants.K_CUSTOM_TYPE);
                return customType >= Constants.MsgType.callingInvite
                    && customType <= Constants.MsgType.callingHungup;
            }
        }
        return false;
    }

    /**
     * 解析扩展信息 避免在bindView时解析造成卡顿
     *
     * @param msg
     */
    public static Message buildExpandInfo(Message msg) {
        MsgExpand msgExpand = (MsgExpand) msg.getExt();
        if (null == msgExpand) msgExpand = new MsgExpand();
        msg.setExt(msgExpand);
        try {
            if (msg.getContentType() == MessageType.CUSTOM) {
                Map map = JSONArray.parseObject(msg.getCustomElem().getData(), Map.class);
                if (map.containsKey(Constants.K_CUSTOM_TYPE)) {
                    int customType = (int) map.get(Constants.K_CUSTOM_TYPE);
                    Object result = map.get(Constants.K_DATA);
                    msg.setContentType(customType);

                    if (customType == Constants.MsgType.CUSTOMIZE_MEETING) {
                        MeetingInfo meetingInfo =
                            GsonHel.fromJson(JSONObject.toJSONString(result), MeetingInfo.class);
                        meetingInfo.startTime = TimeUtil.getTime(meetingInfo.start * 1000,
                            TimeUtil.yearTimeFormat);
                        BigDecimal bigDecimal =
                            (BigDecimal.valueOf(meetingInfo.duration).divide(BigDecimal.valueOf(3600), 1, BigDecimal.ROUND_HALF_DOWN));
                        meetingInfo.durationStr =
                            bigDecimal.toString() + BaseApp.inst().getString(R.string.hour);
                        msgExpand.meetingInfo = meetingInfo;
                        return msg;
                    }

                    if (customType == Constants.MsgType.LOCAL_CALL_HISTORY) {
                        msgExpand.callHistory = GsonHel.fromJson(JSONObject.toJSONString(result),
                            CallHistory.class);
                        if (TextUtils.isEmpty(msgExpand.callHistory.getId())) return msg;
                        //当callHistory.getRoomID 不null 表示我们本地插入的呼叫记录
                        msg.setContentType(Constants.MsgType.LOCAL_CALL_HISTORY);

                        int second = msgExpand.callHistory.getDuration() / 1000;
                        String secondFormat = TimeUtil.secondFormat(second, TimeUtil.secondFormat);
                        msgExpand.callDuration =
                            BaseApp.inst().getString(io.openim.android.ouicore.R.string.call_time) + (second < 60 ? ("00:" + secondFormat) : secondFormat);
                    }
                }
            }
            if (msg.getContentType() == MessageType.QUOTE) {
                buildExpandInfo(msg.getQuoteElem().getQuoteMessage());
            }
            if (msg.getContentType() == MessageType.CUSTOM_FACE) {
                msgExpand.customEmoji = GsonHel.fromJson(msg.getFaceElem().getData(),
                    CustomEmojiEntity.class);
            }
            if (msg.getContentType() == MessageType.OA_NTF) {
                msgExpand.isShowTime = true;
                msgExpand.oaNotification = GsonHel.fromJson(msg.getNotificationElem().getDetail()
                    , OANotification.class);
            }
            if (msg.getContentType() == MessageType.LOCATION)
                msgExpand.locationInfo = GsonHel.fromJson(msg.getLocationElem().getDescription(),
                    LocationInfo.class);
            if (msg.getContentType() == MessageType.AT_TEXT) {
                AtMsgInfo atMsgInfo = new AtMsgInfo();
                atMsgInfo.atUsersInfo = msg.getAtTextElem().atUsersInfo;
                atMsgInfo.text = msg.getAtTextElem().getText();
                msgExpand.atMsgInfo = atMsgInfo;
                handleAt(msgExpand, msg.getGroupID());

                Message quoteMessage = msg.getAtTextElem().getQuoteMessage();
                if (null != quoteMessage) {
                    buildExpandInfo(quoteMessage);
                    QuoteElem quoteElem = new QuoteElem();
                    quoteElem.setText(atMsgInfo.text);
                    quoteElem.setQuoteMessage(quoteMessage);
                    msg.setQuoteElem(quoteElem);
                }
            }
            handleNotification(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        msg.setExt(msgExpand);

        return msg;
    }

    /**
     * 获取名字
     *
     * @return
     */
    public static String getSelfName(String uid, String nickName) {
        if (uid.equals(BaseApp.inst().loginCertificate.userID))
            return BaseApp.inst().getString(R.string.you);
        return nickName.trim();
    }

    /**
     * 处理通知
     */
    private static void handleNotification(Message msg) {
        NotificationElem notificationElem = msg.getNotificationElem();
        if (null == notificationElem) return;
        MsgExpand msgExpand = (MsgExpand) msg.getExt();
        String detail = notificationElem.getDetail();
        CharSequence tips = "";
        String target = "", target2 = "";
        Context ctx = BaseApp.inst();
        switch (msg.getContentType()) {
            case MessageType.REVOKE_MESSAGE_NTF: {
                RevokedInfo revokedInfo = GsonHel.fromJson(detail, RevokedInfo.class);
                String txt;
                //a 撤回了一条消息
                if (revokedInfo.getRevokerID().equals(revokedInfo.getSourceMessageSendID())) {
                    txt = String.format(ctx.getString(R.string.revoke_tips), target =
                        getSelfName(revokedInfo.getRevokerID(), revokedInfo.getRevokerNickname()));
                    tips = getSingleSequence(msg.getGroupID(), target, revokedInfo.getRevokerID()
                        , txt);
                } else {
                    txt = String.format(ctx.getString(R.string.revoke_tips2),
                        getSelfName(revokedInfo.getRevokerID(), revokedInfo.getRevokerNickname())
                        , getSelfName(revokedInfo.getSourceMessageSendID(),
                            revokedInfo.getSourceMessageSenderNickname()));

                    tips = twoPeopleRevoker(msg, revokedInfo, txt);
                }

                break;
            }
            case MessageType.GROUP_CREATED_NTF: {
                GroupNotification groupNotification = GsonHel.fromJson(detail,
                    GroupNotification.class);
                //a 创建了群聊
                String txt = String.format(ctx.getString(R.string.created_group), target =
                    getSelfName(groupNotification.opUser.getUserID(),
                        groupNotification.opUser.getNickname()));

                tips = getSingleSequence(msg.getGroupID(), target,
                    groupNotification.opUser.getUserID(), txt);
                break;
            }
            case MessageType.GROUP_INFO_SET_NTF: {
                GroupNotification groupNotification = GsonHel.fromJson(detail,
                    GroupNotification.class);

                // a 修改了群资料
                String txt = String.format(ctx.getString(R.string.change_group_data), target =
                    getSelfName(groupNotification.opUser.getUserID(),
                        groupNotification.opUser.getNickname()));
                tips = getSingleSequence(msg.getGroupID(), target,
                    groupNotification.opUser.getUserID(), txt);
                break;
            }
            case MessageType.GROUP_NAME_CHANGED_NTF: {
                GroupNotification groupNotification = GsonHel.fromJson(detail,
                    GroupNotification.class);

                // a 修改了群名字
                String txt = String.format(ctx.getString(R.string.edit_group_name), target =
                        getSelfName(groupNotification.opUser.getUserID(),
                            groupNotification.opUser.getNickname()),
                    groupNotification.group.getGroupName());
                tips = getSingleSequence(msg.getGroupID(), target,
                    groupNotification.opUser.getUserID(), txt);
                break;
            }
            case MessageType.MEMBER_QUIT_NTF: {
                QuitGroupNotification quitUser = GsonHel.fromJson(detail,
                    QuitGroupNotification.class);
                // a 退出了群聊
                tips = String.format(ctx.getString(R.string.quit_group2),
                    getSelfName(quitUser.quitUser.getUserID(), quitUser.quitUser.getNickname()));
                break;
            }
            case MessageType.MEMBER_INVITED_NTF: {
                JoinKickedGroupNotification invitedUserList = GsonHel.fromJson(detail,
                    JoinKickedGroupNotification.class);
                // a 邀请 b 加入群聊
                StringBuilder stringBuffer = new StringBuilder();
                List<MultipleChoice> choices = new ArrayList<>();
                for (GroupMembersInfo groupMembersInfo : invitedUserList.invitedUserList) {
                    MultipleChoice choice = new MultipleChoice();
                    choice.name = getSelfName(groupMembersInfo.getUserID(),
                        groupMembersInfo.getNickname());
                    choice.key = groupMembersInfo.getUserID();
                    choices.add(choice);
                    stringBuffer.append(choice.name).append(",");
                }
                String a = stringBuffer.substring(0, stringBuffer.length() - 1);
                String txt = String.format(ctx.getString(R.string.invited_tips), target =
                    getSelfName(invitedUserList.opUser.getUserID(),
                        invitedUserList.opUser.getNickname()), a);

                MultipleChoice choice = new MultipleChoice(invitedUserList.opUser.getUserID());
                choice.name = target;
                choice.groupId = msg.getGroupID();
                choices.add(choice);

                tips = getMultipleSequence(new SpannableStringBuilder(txt), choices);
                break;
            }
            case MessageType.MEMBER_KICKED_NTF: {
                JoinKickedGroupNotification invitedUserList = GsonHel.fromJson(detail,
                    JoinKickedGroupNotification.class);
                // b 被 a 踢出群聊
                StringBuilder stringBuffer = new StringBuilder();
                List<MultipleChoice> choices = new ArrayList<>();
                for (GroupMembersInfo groupMembersInfo : invitedUserList.kickedUserList) {
                    MultipleChoice choice = new MultipleChoice();
                    choice.name = getSelfName(groupMembersInfo.getUserID(),
                        groupMembersInfo.getNickname());
                    choice.key = groupMembersInfo.getUserID();
                    choices.add(choice);
                    stringBuffer.append(choice.name).append(",");
                }
                String a = stringBuffer.substring(0, stringBuffer.length() - 1);
                String txt = String.format(ctx.getString(R.string.kicked_group_tips), a, target =
                    getSelfName(invitedUserList.opUser.getUserID(),
                        invitedUserList.opUser.getNickname()));

                MultipleChoice choice = new MultipleChoice(invitedUserList.opUser.getUserID());
                choice.name = target;
                choice.groupId = msg.getGroupID();
                choices.add(choice);

                tips = getMultipleSequence(new SpannableStringBuilder(txt), choices);
                break;
            }
            case MessageType.MEMBER_ENTER_NTF: {
                EnterGroupNotification entrantUser = GsonHel.fromJson(detail,
                    EnterGroupNotification.class);
                // a 加入了群聊
                String txt = String.format(ctx.getString(R.string.join_group2), target =
                    getSelfName(entrantUser.entrantUser.getUserID(),
                        entrantUser.entrantUser.getNickname()));

                tips = getSingleSequence(msg.getGroupID(), target,
                    entrantUser.entrantUser.getUserID(), txt);
                break;
            }
            case MessageType.GROUP_DISBAND_NTF: {
                GroupNotification groupNotification = GsonHel.fromJson(detail,
                    GroupNotification.class);
                // a 解散了群聊
                tips = String.format(ctx.getString(R.string.dismiss_group),
                    getSelfName(groupNotification.opUser.getUserID(),
                        groupNotification.opUser.getNickname()));
                break;
            }
            case MessageType.GROUP_OWNER_TRANSFERRED_NTF: {
                GroupRightsTransferNotification transferredGroupNotification =
                    GsonHel.fromJson(detail, GroupRightsTransferNotification.class);
                // a 将群转让给了 b
                String txt = String.format(ctx.getString(R.string.transferred_group), target =
                    getSelfName(transferredGroupNotification.opUser.getUserID(),
                        transferredGroupNotification.opUser.getNickname()), target2 =
                    getSelfName(transferredGroupNotification.newGroupOwner.getUserID(),
                        transferredGroupNotification.newGroupOwner.getNickname()));

                MultipleChoice choice =
                    new MultipleChoice(transferredGroupNotification.newGroupOwner.getUserID());
                choice.name = target2;
                choice.groupId = msg.getGroupID();
                tips = getMultipleSequence(getSingleSequence(msg.getGroupID(), target,
                        transferredGroupNotification.opUser.getUserID(), txt),
                    new ArrayList<>(Collections.singleton(choice)));
                break;
            }
            case MessageType.GROUP_MEMBER_MUTED_NTF: {
                MuteMemberNotification memberNotification = GsonHel.fromJson(detail,
                    MuteMemberNotification.class);
                // b 被 a 禁言
                String txt = String.format(ctx.getString(R.string.Muted_group), target =
                        getSelfName(memberNotification.mutedUser.getUserID(),
                            memberNotification.mutedUser.getNickname()), target2 =
                        getSelfName(memberNotification.opUser.getUserID(),
                            memberNotification.opUser.getNickname()),
                    TimeUtil.secondFormat(memberNotification.mutedSeconds));

                List<MultipleChoice> choices = new ArrayList<>();
                MultipleChoice choice1 = new MultipleChoice(memberNotification.opUser.getUserID());
                choice1.name = target2;
                choice1.groupId = msg.getGroupID();
                MultipleChoice choice2 =
                    new MultipleChoice(memberNotification.mutedUser.getUserID());
                choice2.name = target;
                choice2.groupId = msg.getGroupID();
                choices.add(choice1);
                choices.add(choice2);

                tips = getMultipleSequence(new SpannableStringBuilder(txt), choices);
                break;
            }
            case MessageType.GROUP_MEMBER_CANCEL_MUTED_NTF: {
                MuteMemberNotification memberNotification = GsonHel.fromJson(detail,
                    MuteMemberNotification.class);
                // b 被 a 取消了禁言
                String txt = String.format(ctx.getString(R.string.cancel_muted), target =
                    getSelfName(memberNotification.mutedUser.getUserID(),
                        memberNotification.mutedUser.getNickname()), target2 =
                    getSelfName(memberNotification.opUser.getUserID(),
                        memberNotification.opUser.getNickname()));

                MultipleChoice choice =
                    new MultipleChoice(memberNotification.mutedUser.getUserID());
                choice.name = target;
                choice.groupId = msg.getGroupID();
                tips = getMultipleSequence(getSingleSequence(msg.getGroupID(), target2,
                        memberNotification.opUser.getUserID(), txt),
                    new ArrayList<>(Collections.singleton(choice)));
                break;
            }
            case MessageType.GROUP_MUTED_NTF: {
                MuteMemberNotification memberNotification = GsonHel.fromJson(detail,
                    MuteMemberNotification.class);
                // a 开起了群禁言
                String txt = String.format(ctx.getString(R.string.start_muted), target =
                    getSelfName(memberNotification.opUser.getUserID(),
                        memberNotification.opUser.getNickname()));

                tips = getSingleSequence(msg.getGroupID(), target,
                    memberNotification.opUser.getUserID(), txt);
                break;
            }
            case MessageType.GROUP_CANCEL_MUTED_NTF: {
                MuteMemberNotification memberNotification = GsonHel.fromJson(detail,
                    MuteMemberNotification.class);
                // a 关闭了群禁言
                String txt = String.format(ctx.getString(R.string.close_muted), target =
                    getSelfName(memberNotification.opUser.getUserID(),
                        memberNotification.opUser.getNickname()));
                tips = getSingleSequence(msg.getGroupID(), target,
                    memberNotification.opUser.getUserID(), txt);
                break;
            }

            case MessageType.BURN_AFTER_READING_NTF: {
                BurnAfterReadingNotification burnAfterReadingNotification =
                    GsonHel.fromJson(detail, BurnAfterReadingNotification.class);
                tips = burnAfterReadingNotification.isPrivate ?
                    ctx.getString(R.string.start_burn_after_read) :
                    ctx.getString(R.string.stop_burn_after_read);
                break;
            }

            case MessageType.GROUP_MEMBER_INFO_CHANGED_NTF: {
                GroupNotification groupNotification = GsonHel.fromJson(detail,
                    GroupNotification.class);
                String txt = String.format(ctx.getString(R.string.edit_data), target =
                    getSelfName(groupNotification.opUser.getUserID(),
                        groupNotification.opUser.getNickname()));
                tips = getSingleSequence(msg.getGroupID(), target,
                    groupNotification.opUser.getUserID(), txt);
                break;
            }
            case MessageType.GROUP_ANNOUNCEMENT_NTF:
                msgExpand.notificationMsg =
                    GsonHel.fromJson(msg.getNotificationElem().getDetail(), NotificationMsg.class);
                break;

            //单聊-------
            case MessageType.FRIEND_APPLICATION_APPROVED_NTF:
                tips =
                    new SpannableStringBuilder(BaseApp.inst().getString(R.string.start_chat_tips));
                break;

            //-------
            default:
                break;
        }
        msgExpand.tips = tips;
    }


    public static CharSequence twoPeopleRevoker(Message msg, RevokedInfo revokedInfo, String txt) {
        List<MultipleChoice> choices = new ArrayList<>();
        MultipleChoice choice = new MultipleChoice(revokedInfo.getRevokerID());
        choice.name = IMUtil.getSelfName(revokedInfo.getRevokerID(),
            revokedInfo.getRevokerNickname());
        choice.groupId = msg.getGroupID();
        choices.add(choice);

        MultipleChoice choice2 = new MultipleChoice(revokedInfo.getSourceMessageSendID());
        choice2.name = IMUtil.getSelfName(revokedInfo.getSourceMessageSendID(),
            revokedInfo.getSourceMessageSenderNickname());
        choice2.groupId = msg.getGroupID();
        choices.add(choice2);

        return getMultipleSequence(new SpannableStringBuilder(txt), choices);
    }

    /**
     * 获取多跳转的Spannable
     *
     * @param sequence
     * @param choices
     * @return
     */
    private static CharSequence getMultipleSequence(CharSequence sequence,
                                                    List<MultipleChoice> choices) {
        for (MultipleChoice choice : choices) {
            buildClickAndColorSpannable((SpannableStringBuilder) sequence, choice.name,
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        toPersonDetail(choice.key, choice.groupId);
                    }
                });
        }
        return sequence;
    }

    /**
     * 获取只有单跳转的Spannable
     *
     * @param nickName 点击跳转的名字
     * @param uid      用户id
     * @param txt      显示文本
     * @return
     */
    @NonNull
    public static CharSequence getSingleSequence(String groupId, String nickName, String uid,
                                                 String txt) {
        return buildClickAndColorSpannable(new SpannableStringBuilder(txt), nickName,
            new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    toPersonDetail(uid, groupId);
                }
            });
    }

    private static void toPersonDetail(String uid, String groupId) {
        ARouter.getInstance().build(Routes.Main.PERSON_DETAIL).withString(Constants.K_ID, uid).withString(Constants.K_GROUP_ID, groupId).navigation();
    }

    private static String atSelf(AtUserInfo atUsersInfo) {
        return atSelf(atUsersInfo, true);
    }

    private static String atSelf(AtUserInfo atUsersInfo, boolean isNickName) {
        if (isNickName) return "@" + atUsersInfo.getGroupNickname();
        return "@" + (atUsersInfo.getAtUserID().equals(BaseApp.inst().loginCertificate.userID) ?
            BaseApp.inst().getString(R.string.you) : atUsersInfo.getGroupNickname());
    }

    private static void handleAt(MsgExpand msgExpand, String gid) {
        if (null == msgExpand.atMsgInfo) return;
        String atTxt = msgExpand.atMsgInfo.text;
        SpannableStringBuilder spannableString = null;
        for (AtUserInfo atUsersInfo : msgExpand.atMsgInfo.atUsersInfo) {
            String atUid = "@" + atUsersInfo.getAtUserID();
            String tag = atSelf(atUsersInfo);

            if (null == spannableString) spannableString = new SpannableStringBuilder(atTxt);
            else spannableString = new SpannableStringBuilder(spannableString);
            atTxt = spannableString.toString();
            int start = atTxt.indexOf(atUid);
            int end = start + atUid.length();
            SpannableStringBuilder tagSpannable =
                (SpannableStringBuilder) buildClickAndColorSpannable
                    (new SpannableStringBuilder(tag), tag, new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            if (!atUsersInfo.getAtUserID().equals(IMUtil.AT_ALL))
                                toPersonDetail(atUsersInfo.getAtUserID(), gid);
                        }
                    });
            spannableString.replace(start, end, tagSpannable);
        }
        msgExpand.sequence = spannableString;
    }

    public static CharSequence buildClickAndColorSpannable(@NotNull SpannableStringBuilder spannableString, String tag, ClickableSpan clickableSpan) {
        return buildClickAndColorSpannable(spannableString, tag, R.color.theme, clickableSpan);
    }

    public static CharSequence buildClickAndColorSpannable(@NotNull SpannableStringBuilder spannableString, String tag, @ColorRes int colorId, ClickableSpan clickableSpan) {
        try {
            ForegroundColorSpan colorSpan =
                new ForegroundColorSpan(BaseApp.inst().getResources().getColor(colorId));
            int start = spannableString.toString().indexOf(tag);
            int end = spannableString.toString().indexOf(tag) + tag.length();
            if (null != clickableSpan)
                spannableString.setSpan(clickableSpan, start, end,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            spannableString.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception ignore) {
        }
        return spannableString;
    }


    /**
     * 获取草稿
     * [0] CharSequence草稿
     * [1] atUsers
     */
    public static Object[] getDraft(String conversationID) {
        String cacheKey = conversationID + "_draft";
        String atKey = cacheKey + "_at";
        String atJson = SharedPreferencesUtil.get(BaseApp.inst()).getString(atKey);
        String draft = SharedPreferencesUtil.get(BaseApp.inst()).getString(cacheKey);
        List<AtUser> atUsers = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(atJson)) {
                Type type = new TypeToken<List<AtUser>>() {
                }.getType();
                atUsers = GsonHel.getGson().fromJson(atJson, type);
                SpannableStringBuilder spannableString = null;
                for (AtUser atUser : atUsers) {
                    String atUid = IMUtil.atD(atUser.key);
                    String tag = IMUtil.atD(atUser.name);

                    if (null == spannableString)
                        spannableString = new SpannableStringBuilder(draft);
                    else spannableString = new SpannableStringBuilder(spannableString);
                    draft = spannableString.toString();
                    int start = draft.indexOf(atUid);
                    int end = start + atUid.length();

                    SpannableStringBuilder tagSpannable = new SpannableStringBuilder(tag);
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor(
                        "#009ad6"));
                    tagSpannable.setSpan(colorSpan, 0, tag.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    atUser.spanHashCode = colorSpan.hashCode();
                    spannableString.replace(start, end, tagSpannable);
                }
                return new Object[]{spannableString, atUsers};
            }
        } catch (Exception ignore) {
        }
        return new Object[]{draft, atUsers};
    }

    public static CharSequence getMsgParse(Message msg) {
        return getMsgParse(msg, false);
    }

    /**
     * 解析消息内容
     *
     * @param msg
     * @return
     */
    public static CharSequence getMsgParse(Message msg, boolean isShowSenderNickname) {
        MsgExpand msgExpand = (MsgExpand) msg.getExt();
        CharSequence lastMsg = isShowSenderNickname ? msg.getSenderNickname() + ": " : "";
        try {
            switch (msg.getContentType()) {
                default:
                    if (!TextUtils.isEmpty(msgExpand.tips)) lastMsg = msgExpand.tips.toString();
                    break;

                case MessageType.TEXT:
                    lastMsg += msg.getTextElem().getContent();
                    break;
                case MessageType.PICTURE:
                    lastMsg += "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.picture) + "]";
                    break;
                case MessageType.VOICE:
                    lastMsg += "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.voice) + "]";
                    break;
                case MessageType.VIDEO:
                    lastMsg += "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.video) + "]";
                    break;
                case MessageType.FILE:
                    lastMsg += "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.file) + "]";
                    break;
                case MessageType.LOCATION:
                    lastMsg += "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.location) + "]";
                    break;
                case MessageType.AT_TEXT:
                    String atTxt = msgExpand.atMsgInfo.text;
                    for (AtUserInfo atUsersInfo : msgExpand.atMsgInfo.atUsersInfo) {
                        atTxt = atTxt.replace("@" + atUsersInfo.getAtUserID(), atSelf(atUsersInfo));
                    }
                    lastMsg += atTxt;
                    break;

                case MessageType.MERGER:
                    lastMsg += "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.chat_history2) + "]";
                    break;
                case MessageType.CARD:
                    lastMsg += "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.card) + "]";
                    break;
                case MessageType.OA_NTF:
                    lastMsg += ((MsgExpand) msg.getExt()).oaNotification.text;
                    break;
                case MessageType.QUOTE:
                    lastMsg += msg.getQuoteElem().getText();
                    break;
                case MessageType.GROUP_ANNOUNCEMENT_NTF:
                    String target =
                        "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.group_bulletin) + "]";
                    lastMsg += target + msgExpand.notificationMsg.group.notification;
                    lastMsg =
                        IMUtil.buildClickAndColorSpannable(new SpannableStringBuilder(lastMsg),
                            target, android.R.color.holo_red_dark, null);
                    break;
                case Constants.MsgType.LOCAL_CALL_HISTORY:
                    boolean isAudio = msgExpand.callHistory.getType().equals("audio");
                    lastMsg += "[" + (isAudio ? BaseApp.inst().getString(R.string.voice_calls) :
                        BaseApp.inst().getString(R.string.video_calls)) + "]";
                    break;
                case Constants.MsgType.CUSTOMIZE_MEETING:
                    lastMsg += "[" + BaseApp.inst().getString(R.string.video_meeting) + "]";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastMsg;
    }

    /// 会话前缀标签
    public static CharSequence getPrefixTag(CharSequence lastMsg, ConversationInfo info) {
        String prefix = "";
        try {

            switch (info.getGroupAtType()) {
                case GroupAtType.AT_ALL:
                    prefix = "[" + BaseApp.inst().getString(R.string.at_all) + "]";
                    break;
                case GroupAtType.AT_ALL_AT_ME:
                    prefix = "[" + BaseApp.inst().getString(R.string.at_all_at_me) + "]";
                    break;
                case GroupAtType.AT_ME:
                    prefix = "[" + BaseApp.inst().getString(R.string.a_person_at_you) + "]";
                    break;
                case GroupAtType.GROUP_NOTIFICATION:
                    prefix = "[" + BaseApp.inst().getString(R.string.group_bulletin) + "]";
                    break;
            }
            if (TextUtils.isEmpty(prefix)) return prefix;
            lastMsg =
                IMUtil.buildClickAndColorSpannable(new SpannableStringBuilder(prefix + lastMsg),
                    prefix, R.color.theme, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastMsg;
    }

    /**
     * build SignalingInfo
     *
     * @param isVideoCalls   是否视频通话
     * @param isSingleChat   是否单聊
     * @param inviteeUserIDs 邀请ids
     * @param groupID        群id
     * @return
     */
    public static SignalingInfo buildSignalingInfo(boolean isVideoCalls, boolean isSingleChat,
                                                   List<String> inviteeUserIDs, String groupID) {
        boolean isGroupChat = !TextUtils.isEmpty(groupID);
        if (!isGroupChat) groupID = UUID.randomUUID().toString(); //单聊Id自动生成
        groupID = groupID.replaceAll("\u200B", "");

        SignalingInfo signalingInfo = new SignalingInfo();
        String inId = BaseApp.inst().loginCertificate.userID;
        signalingInfo.setOpUserID(inId);
        SignalingInvitationInfo signalingInvitationInfo = new SignalingInvitationInfo();
        signalingInvitationInfo.setInviterUserID(inId);
        signalingInvitationInfo.setInviteeUserIDList(inviteeUserIDs);
        signalingInvitationInfo.setRoomID(groupID);
        signalingInvitationInfo.setTimeout(30);
        signalingInvitationInfo.setInitiateTime(System.currentTimeMillis());

        signalingInvitationInfo.setMediaType(isVideoCalls ? Constants.MediaType.VIDEO :
            Constants.MediaType.AUDIO);
        signalingInvitationInfo.setPlatformID(IMUtil.PLATFORM_ID);
        signalingInvitationInfo.setSessionType(isSingleChat ? ConversationType.SINGLE_CHAT :
            ConversationType.SUPER_GROUP_CHAT);
        if (isGroupChat) signalingInvitationInfo.setGroupID(groupID);

        signalingInfo.setInvitation(signalingInvitationInfo);
        signalingInfo.setOfflinePushInfo(new OfflinePushInfo());
        return signalingInfo;
    }

    /**
     * 弹出底部菜单选择 音视通话
     */
    public static void showBottomCallsPopMenu(Context context, View.OnKeyListener v) {
        HasPermissions hasPermissions = new HasPermissions(context, Permission.CAMERA,
            Permission.RECORD_AUDIO);

        LayoutBottompopV3Binding v3Binding =
            LayoutBottompopV3Binding.inflate(LayoutInflater.from(context));
        BottomPopDialog dialog = new BottomPopDialog(context, v3Binding.getRoot());
        dialog.show();
        v3Binding.cancel.setOnClickListener(v1 -> dialog.dismiss());

        v3Binding.voiceCall.setOnClickListener(v1 -> {
            hasPermissions.safeGo(() -> {
                v.onKey(v1, 1, null);
                dialog.dismiss();
            });
        });
        v3Binding.videoCall.setOnClickListener(v1 -> {
            hasPermissions.safeGo(() -> {
                v.onKey(v1, 2, null);
                dialog.dismiss();
            });
        });
    }

    /**
     * 已登录或登录中
     *
     * @return
     */
    public static boolean isLogged() {
        long status = OpenIMClient.getInstance().getLoginStatus();
        return status == LoginStatus.Logging || status == LoginStatus.Logged;
    }

    /**
     * 退出
     *
     * @param from
     * @param to
     */
    public static void logout(AppCompatActivity from, Class<?> to) {
        from.startActivity(new Intent(from, to));
        LoginCertificate.clear();
        BaseApp.inst().loginCertificate = null;
        CallingService callingService =
            (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
        if (null != callingService) callingService.stopAudioVideoService(from);
        from.finish();
    }

    public static void sendNotice(int id) {
        Postcard postcard = ARouter.getInstance().build(Routes.Main.SPLASH);
        LogisticsCenter.completion(postcard);
        Intent hangIntent = new Intent(BaseApp.inst(), postcard.getDestination());
        PendingIntent hangPendingIntent = PendingIntent.getActivity(BaseApp.inst(), 1002,
            hangIntent, PendingIntent.FLAG_MUTABLE);

        NotificationUtil.sendNotify(id,
            NotificationUtil.builder(NotificationUtil.MSG_NOTIFICATION).setContentTitle(BaseApp.inst().getString(R.string.app_name)).setContentText(BaseApp.inst().getString(R.string.a_message_is_received)).setSmallIcon(R.mipmap.ic_logo).setContentIntent(hangPendingIntent).setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setSound(Uri.parse("android.resource://" + BaseApp.inst().getPackageName() + "/" + R.raw.message_ring)).build());
    }

    //播放提示音
    public static void playPrompt() {
        MediaPlayerUtil.INSTANCE.initMedia(BaseApp.inst(), R.raw.message_ring);
        MediaPlayerUtil.INSTANCE.playMedia();
    }

    //震动milliseconds毫秒
    public static void vibrate(long milliseconds) {
        Vibrator vib = (Vibrator) BaseApp.inst().getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    public static String atD(String str) {
        return "@" + str + "\t";
    }

    /**
     * 成功监听
     *
     * @param <T>
     */
    public interface OnSuccessListener<T> {
        void onSuccess(T data);
    }

    public static class IMCallBack<T> implements OnBase<T> {
        @Override
        public void onError(int code, String error) {
            L.e("IMCallBack", "onError:(" + code + ")" + error);
        }

        public void onSuccess(T data) {

        }
    }
}
