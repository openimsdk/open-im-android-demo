package io.openim.android.ouicore.im;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSONArray;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
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
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.MediaPlayerUtil;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.LoginStatus;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.AtUserInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.NotificationElem;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.RevokedInfo;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.SignalingInvitationInfo;
import io.openim.android.sdk.models.SoundElem;

public class IMUtil {
    //android PlatformID 2
    public static final int PLATFORM_ID = 2;
    private static final String TAG = "IMUtil";

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
            if (summaryList.size() >= 2) break;
        }

        return OpenIMClient.getInstance().messageManager.createMergerMessage(list, title,
            summaryList);
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
                if (map.containsKey("customType")) {
                    int customType = (int) map.get("customType");
                    if (customType == Constant.MsgType.CUSTOMIZE_MEETING) {
                        msg.setContentType(customType);
                        MeetingInfo meetingInfo = GsonHel.fromJson(map.get("data").toString(),
                            MeetingInfo.class);
                        meetingInfo.startTime = TimeUtil.getTime(meetingInfo.start * 1000,
                            TimeUtil.yearTimeFormat);
                        BigDecimal bigDecimal =
                            (BigDecimal.valueOf(meetingInfo.duration).divide(BigDecimal.valueOf(3600), 1, BigDecimal.ROUND_HALF_DOWN));
                        meetingInfo.durationStr =
                            bigDecimal.toString() + BaseApp.inst().getString(R.string.hour);
                        msgExpand.meetingInfo = meetingInfo;
                        return msg;
                    }
                } else {
                    msgExpand.callHistory = GsonHel.fromJson(msg.getCustomElem().getData(),
                        CallHistory.class);
                    if (TextUtils.isEmpty(msgExpand.callHistory.getRoomID())) return msg;
                    //当callHistory.getRoomID 不null 表示我们本地插入的呼叫记录
                    msg.setContentType(Constant.MsgType.LOCAL_CALL_HISTORY);

                    int second = msgExpand.callHistory.getDuration() / 1000;
                    String secondFormat = TimeUtil.secondFormat(second, TimeUtil.secondFormat);
                    msgExpand.callDuration =
                        BaseApp.inst().getString(io.openim.android.ouicore.R.string.call_time) + (second < 60 ? ("00:" + secondFormat) : secondFormat);
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
            }
            handleNotification(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        msg.setExt(msgExpand);

        return msg;
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
        Context ctx = BaseApp.inst();
        switch (msg.getContentType()) {
            case MessageType.REVOKE_MESSAGE_NTF: {
                RevokedInfo revokedInfo = GsonHel.fromJson(detail, RevokedInfo.class);
                //a 撤回了一条消息
                String txt = String.format(ctx.getString(R.string.revoke_tips),
                    revokedInfo.getRevokerNickname());
                tips = getSingleSequence(msg.getGroupID(),
                    revokedInfo.getRevokerNickname(),
                    revokedInfo.getRevokerID(), txt);
                break;
            }
            case MessageType.GROUP_CREATED_NTF: {
                GroupNotification groupNotification = GsonHel.fromJson(detail,
                    GroupNotification.class);
                //a 创建了群聊
                String txt = String.format(ctx.getString(R.string.created_group),
                    groupNotification.opUser.getNickname());

                tips = getSingleSequence(msg.getGroupID(), groupNotification.opUser.getNickname()
                    , groupNotification.opUser.getUserID(), txt);
                break;
            }
            case MessageType.GROUP_INFO_SET_NTF: {
                GroupNotification groupNotification = GsonHel.fromJson(detail,
                    GroupNotification.class);

                // a 修改了群资料
                String txt = String.format(ctx.getString(R.string.change_group_data),
                    groupNotification.opUser.getNickname());
                tips = getSingleSequence(msg.getGroupID(), groupNotification.opUser.getNickname()
                    , groupNotification.opUser.getUserID(), txt);
                break;
            }
            case MessageType.GROUP_NAME_CHANGED_NTF: {
                GroupNotification groupNotification = GsonHel.fromJson(detail,
                    GroupNotification.class);

                // a 修改了群名字
                String txt = String.format(ctx.getString(R.string.edit_group_name),
                    groupNotification.opUser.getNickname());
                tips = getSingleSequence(msg.getGroupID(), groupNotification.opUser.getNickname()
                    , groupNotification.opUser.getUserID(), txt);
                break;
            }
            case MessageType.MEMBER_QUIT_NTF: {
                QuitGroupNotification quitUser = GsonHel.fromJson(detail,
                    QuitGroupNotification.class);
                // a 退出了群聊
                tips = String.format(ctx.getString(R.string.quit_group2),
                    quitUser.quitUser.getNickname());
                break;
            }
            case MessageType.MEMBER_INVITED_NTF: {
                JoinKickedGroupNotification invitedUserList = GsonHel.fromJson(detail,
                    JoinKickedGroupNotification.class);
                // a 邀请 b 加入群聊
                StringBuilder stringBuffer = new StringBuilder();
                List<MultipleChoice> choices = new ArrayList<>();
                for (GroupMembersInfo groupMembersInfo : invitedUserList.invitedUserList) {
                    stringBuffer.append(groupMembersInfo.getNickname()).append(",");
                    MultipleChoice choice = new MultipleChoice();
                    choice.name = groupMembersInfo.getNickname();
                    choice.key = groupMembersInfo.getUserID();
                    choices.add(choice);
                }
                String a = stringBuffer.substring(0, stringBuffer.length() - 1);
                String txt = String.format(ctx.getString(R.string.invited_tips),
                    invitedUserList.opUser.getNickname(), a);

                MultipleChoice choice = new MultipleChoice(invitedUserList.opUser.getUserID());
                choice.name = invitedUserList.opUser.getNickname();
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
                    stringBuffer.append(groupMembersInfo.getNickname()).append(",");
                    MultipleChoice choice = new MultipleChoice();
                    choice.name = groupMembersInfo.getNickname().trim();
                    choice.key = groupMembersInfo.getUserID();
                    choices.add(choice);
                }
                String a = stringBuffer.substring(0, stringBuffer.length() - 1);
                String txt = String.format(ctx.getString(R.string.kicked_group_tips), a,
                    invitedUserList.opUser.getNickname());

                MultipleChoice choice = new MultipleChoice(invitedUserList.opUser.getUserID());
                choice.name = invitedUserList.opUser.getNickname().trim();
                choice.groupId = msg.getGroupID();
                choices.add(choice);

                tips = getMultipleSequence(new SpannableStringBuilder(txt), choices);
                break;
            }
            case MessageType.MEMBER_ENTER_NTF: {
                EnterGroupNotification entrantUser = GsonHel.fromJson(detail,
                    EnterGroupNotification.class);
                // a 加入了群聊
                String txt = String.format(ctx.getString(R.string.join_group2),
                    entrantUser.entrantUser.getNickname());

                tips = getSingleSequence(msg.getGroupID(), entrantUser.entrantUser.getNickname(),
                    entrantUser.entrantUser.getUserID(), txt);
                break;
            }
            case MessageType.GROUP_DISBAND_NTF: {
                GroupNotification groupNotification = GsonHel.fromJson(detail,
                    GroupNotification.class);
                // a 解散了群聊
                tips = String.format(ctx.getString(R.string.dismiss_group),
                    groupNotification.opUser.getNickname());
                break;
            }
            case MessageType.GROUP_OWNER_TRANSFERRED_NTF: {
                GroupRightsTransferNotification transferredGroupNotification =
                    GsonHel.fromJson(detail, GroupRightsTransferNotification.class);
                // a 将群转让给了 b
                String txt = String.format(ctx.getString(R.string.transferred_group),
                    transferredGroupNotification.opUser.getNickname(),
                    transferredGroupNotification.newGroupOwner.getNickname());

                MultipleChoice choice =
                    new MultipleChoice(transferredGroupNotification.newGroupOwner.getUserID());
                choice.name = transferredGroupNotification.newGroupOwner.getNickname();
                choice.groupId = msg.getGroupID();
                tips = getMultipleSequence(getSingleSequence(msg.getGroupID(),
                        transferredGroupNotification.opUser.getNickname(),
                        transferredGroupNotification.opUser.getUserID(), txt),
                    new ArrayList<>(Collections.singleton(choice)));
                break;
            }
            case MessageType.GROUP_MEMBER_MUTED_NTF: {
                MuteMemberNotification memberNotification = GsonHel.fromJson(detail,
                    MuteMemberNotification.class);
                // b 被 a 禁言
                String txt = String.format(ctx.getString(R.string.Muted_group),
                    memberNotification.mutedUser.getNickname(),
                    memberNotification.opUser.getNickname(),
                    TimeUtil.secondFormat(memberNotification.mutedSeconds,
                        TimeUtil.secondFormatZh));

                List<MultipleChoice> choices = new ArrayList<>();
                MultipleChoice choice1 = new MultipleChoice(memberNotification.opUser.getUserID());
                choice1.name = memberNotification.opUser.getNickname();
                choice1.groupId = msg.getGroupID();
                MultipleChoice choice2 =
                    new MultipleChoice(memberNotification.mutedUser.getUserID());
                choice2.name = memberNotification.mutedUser.getNickname();
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
                String txt = String.format(ctx.getString(R.string.cancel_muted),
                    memberNotification.mutedUser.getNickname(),
                    memberNotification.opUser.getNickname());

                MultipleChoice choice =
                    new MultipleChoice(memberNotification.mutedUser.getUserID());
                choice.name = memberNotification.mutedUser.getNickname();
                choice.groupId = msg.getGroupID();
                tips = getMultipleSequence(getSingleSequence(msg.getGroupID(),
                        memberNotification.opUser.getNickname(),
                        memberNotification.opUser.getUserID(), txt),
                    new ArrayList<>(Collections.singleton(choice)));
                break;
            }
            case MessageType.GROUP_MUTED_NTF: {
                MuteMemberNotification memberNotification = GsonHel.fromJson(detail,
                    MuteMemberNotification.class);
                // a 开起了群禁言
                String txt = String.format(ctx.getString(R.string.start_muted),
                    memberNotification.opUser.getNickname());

                tips = getSingleSequence(msg.getGroupID(),
                    memberNotification.opUser.getNickname(),
                    memberNotification.opUser.getUserID(), txt);
                break;
            }
            case MessageType.GROUP_CANCEL_MUTED_NTF: {
                MuteMemberNotification memberNotification = GsonHel.fromJson(detail,
                    MuteMemberNotification.class);
                // a 关闭了群禁言
                String txt = String.format(ctx.getString(R.string.close_muted),
                    memberNotification.opUser.getNickname());
                tips = getSingleSequence(msg.getGroupID(),
                    memberNotification.opUser.getNickname(),
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
                String txt = String.format(ctx.getString(R.string.edit_data),
                    groupNotification.opUser.getNickname());
                tips = getSingleSequence(msg.getGroupID(), groupNotification.opUser.getNickname()
                    , groupNotification.opUser.getUserID(), txt);
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
        ARouter.getInstance().build(Routes.Main.PERSON_DETAIL).withString(Constant.K_ID, uid).withString(Constant.K_GROUP_ID, groupId).navigation();
    }


    private static String atSelf(AtUserInfo atUsersInfo) {
        return "@" + (atUsersInfo.getAtUserID().equals(BaseApp.inst().loginCertificate.userID) ?
            BaseApp.inst().getString(R.string.you) : atUsersInfo.getGroupNickname());
    }

    private static void handleAt(MsgExpand msgExpand, String gid) {
        if (null == msgExpand.atMsgInfo) return;
        String atTxt = msgExpand.atMsgInfo.text;
        for (AtUserInfo atUsersInfo : msgExpand.atMsgInfo.atUsersInfo) {
            atTxt = atTxt.replace("@" + atUsersInfo.getAtUserID(), atSelf(atUsersInfo));
        }
        SpannableStringBuilder spannableString = new SpannableStringBuilder(atTxt);
        for (AtUserInfo atUsersInfo : msgExpand.atMsgInfo.atUsersInfo) {
            String tag = atSelf(atUsersInfo);
            buildClickAndColorSpannable(spannableString, tag, new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    toPersonDetail(atUsersInfo.getAtUserID(), gid);
                }
            });
        }
        msgExpand.sequence = spannableString;
    }

    public static CharSequence buildClickAndColorSpannable(@NotNull SpannableStringBuilder spannableString, String tag, ClickableSpan clickableSpan) {
        return buildClickAndColorSpannable(spannableString, tag, R.color.theme, clickableSpan);
    }

    public static CharSequence buildClickAndColorSpannable(@NotNull SpannableStringBuilder spannableString, String tag, @ColorRes int colorId, ClickableSpan clickableSpan) {
        ForegroundColorSpan colorSpan =
            new ForegroundColorSpan(BaseApp.inst().getResources().getColor(colorId));
        int start = spannableString.toString().indexOf(tag);
        int end = spannableString.toString().indexOf(tag) + tag.length();
        if (null != clickableSpan)
            spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    /**
     * 解析消息内容
     *
     * @param msg
     * @return
     */
    public static CharSequence getMsgParse(Message msg) {
        MsgExpand msgExpand = (MsgExpand) msg.getExt();
        CharSequence lastMsg = "";
        try {
            switch (msg.getContentType()) {
                default:
                    break;
                case MessageType.FRIEND_APPLICATION_APPROVED_NTF:
                    lastMsg =
                        new SpannableStringBuilder(BaseApp.inst().getString(R.string.start_chat_tips));
                    break;
                case MessageType.REVOKE_MESSAGE_NTF:
                    //撤回
                    String detail = msg.getNotificationElem().getDetail();
                    RevokedInfo revokedInfo = GsonHel.fromJson(detail, RevokedInfo.class);
                    lastMsg = String.format(BaseApp.inst().getString(R.string.revoke_tips),
                        revokedInfo.getRevokerNickname());
                    break;
                case MessageType.TEXT:
                    lastMsg = msg.getTextElem().getContent();
                    break;
                case MessageType.PICTURE:
                    lastMsg =
                        "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.picture) + "]";
                    break;
                case MessageType.VOICE:
                    lastMsg =
                        "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.voice) + "]";
                    break;
                case MessageType.VIDEO:
                    lastMsg =
                        "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.video) + "]";
                    break;
                case MessageType.FILE:
                    lastMsg =
                        "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.file) + "]";
                    break;
                case MessageType.LOCATION:
                    lastMsg =
                        "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.location) + "]";
                    break;
                case MessageType.AT_TEXT:
                    String atTxt = msgExpand.atMsgInfo.text;
                    for (AtUserInfo atUsersInfo : msgExpand.atMsgInfo.atUsersInfo) {
                        atTxt = atTxt.replace("@" + atUsersInfo.getAtUserID(), atSelf(atUsersInfo));
                    }
                    lastMsg = atTxt;
                    break;
                case MessageType.MERGER:
                    lastMsg =
                        "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.chat_history2) + "]";
                    break;
                case MessageType.CARD:
                    lastMsg =
                        "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.card) + "]";
                    break;
                case MessageType.OA_NTF:
                    lastMsg = ((MsgExpand) msg.getExt()).oaNotification.text;
                    break;
                case MessageType.QUOTE:
                    lastMsg = msg.getQuoteElem().getText();
                    break;
                case MessageType.GROUP_ANNOUNCEMENT_NTF:
                    String target =
                        "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.group_bulletin) + "]";
                    lastMsg = target + msgExpand.notificationMsg.group.notification;
                    lastMsg =
                        IMUtil.buildClickAndColorSpannable(new SpannableStringBuilder(lastMsg),
                            target, android.R.color.holo_red_dark, null);
                    break;
                case Constant.MsgType.LOCAL_CALL_HISTORY:
                    boolean isAudio = msgExpand.callHistory.getType().equals("audio");
                    lastMsg = "[" + (isAudio ? BaseApp.inst().getString(R.string.voice_calls) :
                        BaseApp.inst().getString(R.string.video_calls)) + "]";
                    break;
                case Constant.MsgType.CUSTOMIZE_MEETING:
                    lastMsg = "[" + BaseApp.inst().getString(R.string.video_meeting) + "]";
                    break;
            }
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
        SignalingInfo signalingInfo = new SignalingInfo();
        String inId = BaseApp.inst().loginCertificate.userID;
        signalingInfo.setOpUserID(inId);
        SignalingInvitationInfo signalingInvitationInfo = new SignalingInvitationInfo();
        signalingInvitationInfo.setInviterUserID(inId);
        signalingInvitationInfo.setInviteeUserIDList(inviteeUserIDs);
        signalingInvitationInfo.setRoomID(String.valueOf(UUID.randomUUID()));
        signalingInvitationInfo.setTimeout(30);

        signalingInvitationInfo.setMediaType(isVideoCalls ? Constant.MediaType.VIDEO :
            Constant.MediaType.AUDIO);
        signalingInvitationInfo.setPlatformID(IMUtil.PLATFORM_ID);
        signalingInvitationInfo.setSessionType(isSingleChat ? 1 : 2);
        signalingInvitationInfo.setGroupID(groupID);

        signalingInfo.setInvitation(signalingInvitationInfo);
        signalingInfo.setOfflinePushInfo(new OfflinePushInfo());
        return signalingInfo;
    }

    /**
     * 弹出底部菜单选择 音视通话
     */
    public static void showBottomPopMenu(Context context, View.OnKeyListener v) {
        BottomPopDialog dialog = new BottomPopDialog(context);
        dialog.show();
        dialog.getMainView().menu3.setOnClickListener(v1 -> dialog.dismiss());
        dialog.getMainView().menu1.setText(io.openim.android.ouicore.R.string.voice_calls);
        dialog.getMainView().menu2.setText(io.openim.android.ouicore.R.string.video_calls);

        dialog.getMainView().menu1.setOnClickListener(v1 -> {
            v.onKey(v1, 1, null);
            dialog.dismiss();
        });
        dialog.getMainView().menu2.setOnClickListener(v1 -> {
            v.onKey(v1, 2, null);
            dialog.dismiss();
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

    public static void sendNotice(long id) {
        NotificationManager manager =
            (NotificationManager) BaseApp.inst().getSystemService(Context.NOTIFICATION_SERVICE);

        Postcard postcard = ARouter.getInstance().build(Routes.Main.SPLASH);
        LogisticsCenter.completion(postcard);
        Intent hangIntent = new Intent(BaseApp.inst(), postcard.getDestination());
        PendingIntent hangPendingIntent = PendingIntent.getActivity(BaseApp.inst(), 1002,
            hangIntent, PendingIntent.FLAG_MUTABLE);

        String CHANNEL_ID = "msg_notification";
        String CHANNEL_NAME = BaseApp.inst().getString(R.string.msg_notification);
        Notification notification =
            new NotificationCompat.Builder(BaseApp.inst(), CHANNEL_ID).setContentTitle(BaseApp.inst().getString(R.string.app_name)).setContentText(BaseApp.inst().getString(R.string.a_message_is_received)).setSmallIcon(R.mipmap.ic_logo).setContentIntent(hangPendingIntent).setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setSound(Uri.parse("android.resource://" + BaseApp.inst().getPackageName() + "/" + R.raw.message_ring)).build();

        //Android 8.0 以上需包添加渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            AudioAttributes audioAttributes =
                new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
            notificationChannel.setSound(Uri.parse("android.resource://" + BaseApp.inst().getPackageName() + "/" + R.raw.message_ring), audioAttributes);
            manager.createNotificationChannel(notificationChannel);
        }
        manager.notify((int) id, notification);
    }

    //播放提示音
    public static void playPrompt() {
        MediaPlayerUtil.INSTANCE.initMedia(BaseApp.inst(), R.raw.message_ring);
        MediaPlayerUtil.INSTANCE.playMedia();
    }

    /**
     * 获取语音播放路径 本地没有取网络
     *
     * @param soundElem
     * @return
     */
    public static String getSoundPath(SoundElem soundElem) {
        String path = "";
        try {
            path = soundElem.getSoundPath();
            if (TextUtils.isEmpty(path)) path = soundElem.getSourceUrl();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
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
            Toast.makeText(BaseApp.inst(), error + "(" + code + ")", Toast.LENGTH_LONG).show();
        }

        public void onSuccess(T data) {

        }
    }
}
