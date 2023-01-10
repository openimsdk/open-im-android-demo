package io.openim.android.ouiconversation.adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.airbnb.lottie.LottieAnimationView;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.openim.android.ouiconversation.R;

import io.openim.android.ouiconversation.databinding.LayoutLoadingSmallBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgAudioLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgAudioRightBinding;


import io.openim.android.ouiconversation.databinding.LayoutMsgCardLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgCardRightBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgExMenuBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgFileLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgFileRightBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgImgLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgImgRightBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgLocation1Binding;
import io.openim.android.ouiconversation.databinding.LayoutMsgLocation2Binding;
import io.openim.android.ouiconversation.databinding.LayoutMsgMergeLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgMergeRightBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgNoticeLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgTxtLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgTxtRightBinding;
import io.openim.android.ouiconversation.ui.ChatHistoryDetailsActivity;
import io.openim.android.ouiconversation.ui.MsgReadStatusActivity;
import io.openim.android.ouiconversation.ui.PreviewActivity;
import io.openim.android.ouiconversation.widget.SendStateView;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouiconversation.widget.InputExpandFragment;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.IView;
import io.openim.android.ouicore.entity.CallHistory;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.ByteUtil;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.ouicore.voice.listener.PlayerListener;
import io.openim.android.ouicore.voice.player.SMediaPlayer;
import io.openim.android.ouicore.widget.AvatarImage;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.CustomElem;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.MergeElem;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.QuoteElem;
import io.openim.android.sdk.models.SignalingInfo;

public class MessageViewHolder {
    public static RecyclerView.ViewHolder createViewHolder(@NonNull ViewGroup parent,
                                                           int viewType) {
        if (viewType == Constant.LOADING) return new LoadingView(parent);

        if (viewType == Constant.MsgType.TXT) return new TXTView(parent);

        if (viewType == Constant.MsgType.PICTURE) return new IMGView(parent);

        if (viewType == Constant.MsgType.VOICE) return new AudioView(parent);

        if (viewType == Constant.MsgType.VIDEO) return new VideoView(parent);

        if (viewType == Constant.MsgType.FILE) return new FileView(parent);

        if (viewType == Constant.MsgType.LOCATION) return new LocationView(parent);

        if (viewType == Constant.MsgType.OA_NOTICE) return new NotificationItemHo(parent);

        if (viewType >= Constant.MsgType.NOTICE || viewType == Constant.MsgType.REVOKE || viewType == Constant.MsgType.ADVANCED_REVOKE)
            return new NoticeView(parent);

        if (viewType == Constant.MsgType.MERGE) return new MergeView(parent);

        if (viewType == Constant.MsgType.CARD) return new BusinessCardView(parent);

        if (viewType == Constant.MsgType.QUOTE) return new QuoteTXTView(parent);

        if (viewType == Constant.MsgType.CUSTOMIZE) return new CallHistoryView(parent);

        return new TXTView(parent);
    }

    public abstract static class MsgViewHolder extends RecyclerView.ViewHolder {
        protected RecyclerView recyclerView;
        protected MessageAdapter messageAdapter;

        private PopupWindow popupWindow;
        private Message message;
        private RecyclerViewAdapter adapter;
        protected ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);

        private boolean leftIsInflated = false, rightIsInflated = false;
        private final ViewStub right;
        private final ViewStub left;

        public MsgViewHolder(ViewGroup itemView) {
            super(buildRoot(itemView));
            left = this.itemView.findViewById(R.id.left);
            right = this.itemView.findViewById(R.id.right);

            left.setOnInflateListener((stub, inflated) -> leftIsInflated = true);
            right.setOnInflateListener((stub, inflated) -> rightIsInflated = true);
        }

        public static View buildRoot(ViewGroup parent) {
            return LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_msg, parent,
                false);
        }

        protected abstract int getLeftInflatedId();

        protected abstract int getRightInflatedId();

        protected abstract void bindLeft(View itemView, Message message);

        protected abstract void bindRight(View itemView, Message message);

        /**
         * 是否是自己发的消息
         */
        protected boolean isOwn = false;

        //绑定数据
        public void bindData(Message message, int position) {
            this.message = message;
            try {
                if (isOwn = message.getSendID().equals(BaseApp.inst().loginCertificate.userID)) {
                    if (leftIsInflated) left.setVisibility(View.GONE);
                    if (rightIsInflated) right.setVisibility(View.VISIBLE);
                    if (!rightIsInflated) {
                        right.setLayoutResource(getRightInflatedId());
                        right.inflate();
                    }
                    bindRight(itemView, message);
                } else {
                    if (leftIsInflated) left.setVisibility(View.VISIBLE);
                    if (rightIsInflated) right.setVisibility(View.GONE);
                    if (!leftIsInflated) {
                        left.setLayoutResource(getLeftInflatedId());
                        left.inflate();
                    }
                    bindLeft(itemView, message);
                }
                unite();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int getHaveReadCount() {
            List<String> hasReadUserIDList =
                message.getAttachedInfoElem().getGroupHasReadInfo().getHasReadUserIDList();
            return null == hasReadUserIDList ? 0 : hasReadUserIDList.size();
        }

        int getNeedReadCount() {
            return message.getAttachedInfoElem().getGroupHasReadInfo().getGroupMemberCount();
        }

        /**
         * 统一处理
         */
        private void unite() {
            MsgExpand msgExpand = (MsgExpand) message.getExt();
            TextView notice = itemView.findViewById(R.id.notice);
            AvatarImage avatarImage = itemView.findViewById(R.id.avatar);
            CheckBox checkBox = itemView.findViewById(R.id.choose);
            TextView unRead = itemView.findViewById(R.id.unRead);
            View contentView = itemView.findViewById(R.id.content);
            if (null == contentView) contentView = itemView.findViewById(R.id.content2);
            showMsgExMenu(contentView);

            readVanishShow(msgExpand);

            if (msgExpand.isShowTime) {
                //显示时间
                String time = TimeUtil.getTimeString(message.getSendTime());
                notice.setVisibility(View.VISIBLE);
                notice.setText(time);
            } else notice.setVisibility(View.GONE);

            if (null != avatarImage) {
                AtomicBoolean isLongClick = new AtomicBoolean(false);
                avatarImage.setOnLongClickListener(v -> {
                    if (chatVM.isSingleChat) return false;
                    isLongClick.set(true);
                    List<Message> atMessages = chatVM.atMessages.getValue();
                    for (Message atMessage : atMessages) {
                        if (atMessage.getSendID().equals(message.getSendID())) return false;
                    }
                    atMessages.add(message);
                    chatVM.atMessages.setValue(atMessages);
                    return false;
                });
                avatarImage.setOnClickListener(v -> {
                    if (isLongClick.get()) {
                        isLongClick.set(false);
                        return;
                    }
                    ARouter.getInstance().build(Routes.Main.PERSON_DETAIL).withString(Constant.K_ID, message.getSendID()).withString(Constant.K_GROUP_ID, message.getGroupID()).navigation();
                });
            }
            if (null != chatVM.enableMultipleSelect.getValue() && chatVM.enableMultipleSelect.getValue() && message.getContentType() != Constant.MsgType.NOTICE) {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setChecked(msgExpand.isChoice);
                checkBox.setOnClickListener((buttonView) -> {
                    msgExpand.isChoice = checkBox.isChecked();
                });
            } else {
                checkBox.setVisibility(View.GONE);
            }
            ((LinearLayout.LayoutParams) checkBox.getLayoutParams()).topMargin =
                msgExpand.isShowTime ? Common.dp2px(15) : 0;

            if (isOwn) {
                SendStateView sendStateView = itemView.findViewById(R.id.sendState2);
                sendStateView.setOnClickListener(v -> {
                    chatVM.sendMsg(message);
                });
            }

            int viewType = message.getContentType();
            unRead.setVisibility(View.GONE);
            if (isOwn && message.getStatus() == Constant.Send_State.SEND_SUCCESS && viewType < Constant.MsgType.NOTICE && viewType != Constant.MsgType.REVOKE && viewType != Constant.MsgType.CUSTOMIZE && viewType != Constant.MsgType.ADVANCED_REVOKE) {
                unRead.setVisibility(View.VISIBLE);
                if (chatVM.isSingleChat) {
                    String unread =
                        String.format(chatVM.getContext().getString(io.openim.android.ouicore.R.string.unread), "");
                    String readed =
                        String.format(chatVM.getContext().getString(io.openim.android.ouicore.R.string.readed), "");
                    unRead.setText(message.isRead() ? readed : unread);
                    unRead.setTextColor(Color.parseColor(message.isRead() ? "#ff999999" :
                        "#ff5496eb"));
                } else {
                    int unreadCount = getNeedReadCount() - getHaveReadCount() - 1;
                    if (unreadCount > 0) {
                        unRead.setTextColor(Color.parseColor("#ff999999"));
                        unRead.setText(unreadCount + chatVM.getContext().getString(io.openim.android.ouicore.R.string.person_unRead));
                        unRead.setOnClickListener(v -> {
                            v.getContext().startActivity(new Intent(v.getContext(),
                                MsgReadStatusActivity.class).putExtra(Constant.K_GROUP_ID,
                                message.getGroupID()).putStringArrayListExtra(Constant.K_ID,
                                (ArrayList<String>) message.getAttachedInfoElem().getGroupHasReadInfo().getHasReadUserIDList()));
                        });
                    }
                }
            }

        }

        //阅后即焚显示与添加timer
        private void readVanishShow(MsgExpand msgExpand) {
            TextView readVanishNum;
            if (isOwn) readVanishNum = itemView.findViewById(R.id.readVanishNum2);
            else readVanishNum = itemView.findViewById(R.id.readVanishNum);

            readVanishNum.setVisibility(View.GONE);
            if (!chatVM.conversationInfo.getValue().isPrivateChat()) return;
            if (!message.getAttachedInfoElem().isPrivateChat()) return;
            if (!message.isRead()) return;

            chatVM.addReadVanish(message);
            if (msgExpand.readVanishNum > 0) {
                readVanishNum.setVisibility(View.VISIBLE);
                readVanishNum.setText(msgExpand.readVanishNum + "s");
            }

        }

        /***
         * 长按显示扩展菜单
         * @param view
         */
        protected void showMsgExMenu(View view) {
            view.setOnLongClickListener(v -> {
                if (null != chatVM.enableMultipleSelect.getValue() && chatVM.enableMultipleSelect.getValue())
                    return true;
                List<Integer> menuIcons = new ArrayList<>();
                List<String> menuTitles = new ArrayList<>();

                if (null == popupWindow) {
                    popupWindow = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                    LayoutMsgExMenuBinding view1 =
                        LayoutMsgExMenuBinding.inflate(LayoutInflater.from(itemView.getContext()));
                    popupWindow.setContentView(view1.getRoot());
                    popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
                    popupWindow.setOutsideTouchable(true);
                    adapter =
                        new RecyclerViewAdapter<Object, InputExpandFragment.ExpandHolder>(InputExpandFragment.ExpandHolder.class) {

                            @Override
                            public void onBindView(@NonNull InputExpandFragment.ExpandHolder holder,
                                                   Object data, int position) {
                                int iconRes = menuIcons.get(position);
                                holder.v.menu.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                                    v.getContext().getDrawable(iconRes), null, null);
                                holder.v.menu.setText(menuTitles.get(position));
                                holder.v.menu.setTextColor(Color.WHITE);
                                holder.v.menu.setOnClickListener(v1 -> {
                                    popupWindow.dismiss();
                                    if (iconRes == R.mipmap.ic_reply) {
                                        chatVM.replyMessage.setValue(message);
                                    }
                                    if (iconRes == R.mipmap.ic_c_copy) {
                                        Common.copy(message.getContent());
                                        chatVM.toast(BaseApp.inst().getString(io.openim.android.ouicore.R.string.copy_succ));
                                    }
                                    if (iconRes == R.mipmap.ic_withdraw) {
                                        chatVM.revokeMessage(message);
                                    }
                                    if (iconRes == R.mipmap.ic_delete) {
                                        chatVM.deleteMessageFromLocalStorage(message);
                                    }
                                    if (iconRes == R.mipmap.ic_forward) {
                                        Message forwardMessage =
                                            OpenIMClient.getInstance().messageManager.createForwardMessage(message);
                                        chatVM.forwardMsg = forwardMessage;
                                        ARouter.getInstance().build(Routes.Contact.FORWARD).navigation((Activity) view.getContext(), Constant.Event.FORWARD);
                                    }
                                    if (iconRes == R.mipmap.ic_multiple_choice) {
                                        chatVM.enableMultipleSelect.setValue(true);
                                        ((MsgExpand) message.getExt()).isChoice = true;
                                        messageAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        };
                    view1.recyclerview.setAdapter(adapter);
                }
                if (message.getContentType() == Constant.MsgType.TXT) {
                    menuIcons.add(R.mipmap.ic_c_copy);
                    menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.copy));
                }
                menuIcons.add(R.mipmap.ic_delete);
                menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.delete));

                if (chatVM.hasPermission) {
                    menuIcons.add(R.mipmap.ic_withdraw);
                    menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.withdraw));
                } else if (message.getSendID().equals(BaseApp.inst().loginCertificate.userID)) {
                    //5分钟内可以撤回
                    if (System.currentTimeMillis() - message.getSendTime() < (1000 * 60 * 5)) {
                        menuIcons.add(R.mipmap.ic_withdraw);
                        menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.withdraw));
                    }
                }
                menuIcons.add(R.mipmap.ic_forward);
                menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.forward));

                if (message.getContentType() != Constant.MsgType.VOICE && message.getContentType() != Constant.MsgType.MERGE) {
                    menuIcons.add(R.mipmap.ic_reply);
                    menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.reply));
                }
                menuIcons.add(R.mipmap.ic_multiple_choice);
                menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.multiple_choice));

                LayoutMsgExMenuBinding vb =
                    LayoutMsgExMenuBinding.bind(popupWindow.getContentView());
                vb.recyclerview.setLayoutManager(new GridLayoutManager(view.getContext(),
                    menuIcons.size() < 4 ? menuIcons.size() : 4));
                adapter.setItems(menuIcons);

                int yDelay = Common.dp2px(5);
                popupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
                Rect globalVisibleRect = new Rect();
                v.getGlobalVisibleRect(globalVisibleRect);
                int y =
                    (popupWindow.getContentView().getMeasuredHeight() + v.getMeasuredHeight() + yDelay);

                if (globalVisibleRect.top - BaseApp.inst().getResources().getDimension(io.openim.android.ouicore.R.dimen.comm_title_high) > y) {
                    y = -y;
                    vb.downArrow.setVisibility(View.VISIBLE);
                    vb.topArrow.setVisibility(View.GONE);
                } else {
                    y = yDelay;
                    vb.topArrow.setVisibility(View.VISIBLE);
                    vb.downArrow.setVisibility(View.GONE);
                }
                popupWindow.showAsDropDown(v,
                    -(popupWindow.getContentView().getMeasuredWidth() - v.getMeasuredWidth()) / 2
                    , y);
                return true;
            });
        }

        /**
         * 处理at、emoji
         *
         * @return true 处理了
         */
        protected boolean handleSequence(TextView showView, Message message) {
            MsgExpand msgExpand = (MsgExpand) message.getExt();
            if (null != msgExpand.sequence) {
                showView.setText(msgExpand.sequence);
                if (null != msgExpand.atMsgInfo)
                    showView.setMovementMethod(LinkMovementMethod.getInstance());
                return true;
            }
            return false;
        }


        public void bindRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        public void setMessageAdapter(MessageAdapter messageAdapter) {
            this.messageAdapter = messageAdapter;
        }


        /**
         * 预览图片或视频
         *
         * @param view
         * @param url           地址
         * @param firstFrameUrl 缩略图
         */
        public void toPreview(View view, String url, String firstFrameUrl) {
            view.setOnClickListener(v -> view.getContext().startActivity(new Intent(view.getContext(), PreviewActivity.class).putExtra(PreviewActivity.MEDIA_URL, url).putExtra(PreviewActivity.FIRST_FRAME, firstFrameUrl)));
        }

    }


    //加载中...
    public static class LoadingView extends RecyclerView.ViewHolder {
        public LoadingView(ViewGroup parent) {
            super(LayoutLoadingSmallBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false).getRoot());
        }
    }

    //通知消息
    public static class NoticeView extends MessageViewHolder.MsgViewHolder {

        public NoticeView(ViewGroup itemView) {
            super(itemView);
        }

        @SuppressLint({"SetTextI18n", "StringFormatInvalid"})
        @Override
        public void bindData(Message message, int position) {
            TextView textView = itemView.findViewById(R.id.notice);
            textView.setVisibility(View.VISIBLE);
            if (message.getContentType() >= Constant.MsgType.NOTICE) {
                String tips = message.getNotificationElem().getDefaultTips();
                textView.setText(tips);
            } else
                textView.setText(String.format(textView.getContext().getString(io.openim.android.ouicore.R.string.revoke_tips), message.getSenderNickname()));
        }

        @Override
        protected int getLeftInflatedId() {
            return 0;
        }

        @Override
        protected int getRightInflatedId() {
            return 0;
        }

        @Override
        protected void bindLeft(View itemView, Message message) {

        }

        @Override
        protected void bindRight(View itemView, Message message) {

        }
    }

    //文本消息
    public static class TXTView extends MessageViewHolder.MsgViewHolder {

        public TXTView(ViewGroup parent) {
            super(parent);
        }

        @Override
        protected int getLeftInflatedId() {
            return R.layout.layout_msg_txt_left;
        }

        @Override
        protected int getRightInflatedId() {
            return R.layout.layout_msg_txt_right;
        }

        @Override
        protected void bindLeft(View itemView, Message message) {
            LayoutMsgTxtLeftBinding v = LayoutMsgTxtLeftBinding.bind(itemView);
            v.avatar.load(message.getSenderFaceUrl());
            v.content.setText(message.getContent());

            if (message.getSessionType() != io.openim.android.ouicore.utils.Constant.SessionType.SINGLE_CHAT) {
                v.nickName.setVisibility(View.VISIBLE);
                v.nickName.setText(message.getSenderNickname());
            } else v.nickName.setVisibility(View.GONE);

            if (!handleSequence(v.content, message)) v.content.setText(message.getContent());

        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgTxtRightBinding v = LayoutMsgTxtRightBinding.bind(itemView);
            v.avatar2.load(message.getSenderFaceUrl());
            v.sendState2.setSendState(message.getStatus());

            if (!handleSequence(v.content2, message)) v.content2.setText(message.getContent());
        }

    }

    public static class IMGView extends MessageViewHolder.MsgViewHolder {

        public IMGView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        protected int getLeftInflatedId() {
            return R.layout.layout_msg_img_left;
        }

        @Override
        protected int getRightInflatedId() {
            return R.layout.layout_msg_img_right;
        }

        @Override
        protected void bindLeft(View itemView, Message message) {
            LayoutMsgImgLeftBinding v = LayoutMsgImgLeftBinding.bind(itemView);

            v.nickName.setVisibility(View.VISIBLE);
            v.nickName.setText(message.getSenderNickname());
            v.avatar.load(message.getSenderFaceUrl());
            Common.loadPicture(v.content, message.getPictureElem());


            v.sendState.setSendState(message.getStatus());
            String url = message.getPictureElem().getSourcePicture().getUrl();
            toPreview(v.content, url, null);
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgImgRightBinding v = LayoutMsgImgRightBinding.bind(itemView);
            v.avatar2.load(message.getSenderFaceUrl());
            Common.loadPicture(v.content2, message.getPictureElem());

            v.sendState2.setSendState(message.getStatus());
            String url = message.getPictureElem().getSourcePicture().getUrl();
            toPreview(v.content2, url, null);
        }

    }

    public static class AudioView extends MessageViewHolder.MsgViewHolder {
        private Message playingMessage;

        public AudioView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        public void bindRecyclerView(RecyclerView recyclerView) {
            super.bindRecyclerView(recyclerView);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (null != playingMessage) {
                        int index = messageAdapter.getMessages().indexOf(playingMessage);
                        LinearLayoutManager linearLayoutManager =
                            (LinearLayoutManager) recyclerView.getLayoutManager();
                        int firstVisiblePosition =
                            linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                        int lastVisiblePosition =
                            linearLayoutManager.findLastCompletelyVisibleItemPosition();
//                        L.e("--------firstVisiblePosition-------="+firstVisiblePosition);
//                        L.e("--------lastVisiblePosition--------="+lastVisiblePosition);
//                        L.e("--------index--------="+index);

                        if (index < firstVisiblePosition || index > lastVisiblePosition) {
                            SPlayer.instance().stop();
                            playingMessage = null;
                        }
                    }

                }
            });
        }

        @Override
        protected int getLeftInflatedId() {
            return R.layout.layout_msg_audio_left;
        }

        @Override
        protected int getRightInflatedId() {
            return R.layout.layout_msg_audio_right;
        }

        @Override
        protected void bindLeft(View itemView, Message message) {
            final LayoutMsgAudioLeftBinding view = LayoutMsgAudioLeftBinding.bind(itemView);
            TextView badge = itemView.findViewById(io.openim.android.ouicore.R.id.badge);
            badge.setVisibility(message.isRead() ? View.GONE : View.VISIBLE);
            view.avatar.load(message.getSenderFaceUrl());
            if (message.getSessionType() != Constant.SessionType.SINGLE_CHAT) {
                view.sendNickName.setVisibility(View.VISIBLE);
                view.sendNickName.setText(message.getSenderNickname());
            }
            view.duration.setText(message.getSoundElem().getDuration() + "``");
            view.content.setOnClickListener(v -> clickPlay(message, view.lottieView));
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            final LayoutMsgAudioRightBinding view = LayoutMsgAudioRightBinding.bind(itemView);
            view.avatar2.load(message.getSenderFaceUrl());
            view.sendState2.setSendState(message.getStatus());
            view.duration2.setText(message.getSoundElem().getDuration() + "``");

            view.content2.setOnClickListener(v -> clickPlay(message, view.lottieView2));
        }

        private void markRead(Message message, boolean isPrivateChat) {
            if (isPrivateChat && !isOwn) chatVM.markReaded(message);
        }

        private void clickPlay(Message message, LottieAnimationView lottieView) {
            SPlayer.instance().getMediaPlayer();
            SPlayer.instance().stop();
            SPlayer.instance().playByUrl(message.getSoundElem().getSourceUrl(),
                new PlayerListener() {
                    @Override
                    public void LoadSuccess(SMediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }

                    @Override
                    public void Loading(SMediaPlayer mediaPlayer, int i) {

                    }

                    @Override
                    public void onCompletion(SMediaPlayer mediaPlayer) {
                        mediaPlayer.stop();
                        markRead(message, chatVM.conversationInfo.getValue().isPrivateChat());
                    }

                    @Override
                    public void onError(Exception e) {
                        lottieView.cancelAnimation();
                        lottieView.setProgress(1);
                    }
                });

            SPlayer.instance().getMediaPlayer().setOnPlayStateListener(new SMediaPlayer.OnPlayStateListener() {
                @Override
                public void started() {
                    markRead(message, !chatVM.conversationInfo.getValue().isPrivateChat());

                    playingMessage = message;
                    lottieView.playAnimation();
                }


                @Override
                public void paused() {
                }

                @Override
                public void stopped() {
                    lottieView.cancelAnimation();
                    lottieView.setProgress(1);
                }

                @Override
                public void completed() {

                }
            });
        }
    }

    public static class VideoView extends IMGView {

        public VideoView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgImgRightBinding view = LayoutMsgImgRightBinding.bind(itemView);
            view.avatar2.load(message.getSenderFaceUrl());
            view.sendState2.setSendState(message.getStatus());
            view.videoPlay2.setVisibility(View.VISIBLE);

            Common.loadVideoSnapshot(view.content2, message.getVideoElem());
            String snapshotUrl = message.getVideoElem().getSnapshotUrl();
            toPreview(view.videoPlay2, message.getVideoElem().getVideoUrl(), snapshotUrl);
        }


        @Override
        protected void bindLeft(View itemView, Message message) {
            LayoutMsgImgLeftBinding view = LayoutMsgImgLeftBinding.bind(itemView);
            view.avatar.load(message.getSenderFaceUrl());
            if (message.getSessionType() != Constant.SessionType.SINGLE_CHAT) {
                view.sendNickName.setVisibility(View.VISIBLE);
                view.sendNickName.setText(message.getSenderNickname());
            }
            view.sendState.setSendState(message.getStatus());
            view.videoPlay.setVisibility(View.VISIBLE);

            Common.loadVideoSnapshot(view.content, message.getVideoElem());
            String snapshotUrl = message.getVideoElem().getSnapshotUrl();
            toPreview(view.videoPlay, message.getVideoElem().getVideoUrl(), snapshotUrl);
        }
    }


    public static class FileView extends MessageViewHolder.MsgViewHolder {

        public FileView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        protected int getLeftInflatedId() {
            return R.layout.layout_msg_file_left;
        }

        @Override
        protected int getRightInflatedId() {
            return R.layout.layout_msg_file_right;
        }

        @Override
        protected void bindLeft(View itemView, Message message) {
            LayoutMsgFileLeftBinding view = LayoutMsgFileLeftBinding.bind(itemView);
            view.avatar.load(message.getSenderFaceUrl());

            view.title.setText(message.getFileElem().getFileName());
            Long size = message.getFileElem().getFileSize();
            view.size.setText(ByteUtil.bytes2kb(size) + "");

            view.sendState.setSendState(message.getStatus());

            view.content.setOnClickListener(v -> {
                GetFilePathFromUri.openFile(v.getContext(), message);
            });
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgFileRightBinding view = LayoutMsgFileRightBinding.bind(itemView);
            view.avatar2.load(message.getSenderFaceUrl());

            view.title2.setText(message.getFileElem().getFileName());
            Long size = message.getFileElem().getFileSize();
            view.size2.setText(ByteUtil.bytes2kb(size) + "");

            view.sendState2.setSendState(message.getStatus());

            view.content2.setOnClickListener(v -> {
                GetFilePathFromUri.openFile(v.getContext(), message);
            });
        }


    }

    public static class LocationView extends MessageViewHolder.MsgViewHolder {

        public LocationView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        protected int getLeftInflatedId() {
            return R.layout.layout_msg_location1;
        }

        @Override
        protected int getRightInflatedId() {
            return R.layout.layout_msg_location2;
        }

        @Override
        protected void bindLeft(View itemView, Message message) {
            LayoutMsgLocation1Binding view = LayoutMsgLocation1Binding.bind(itemView);
            view.avatar.load(message.getSenderFaceUrl());
            if (message.getSessionType() != Constant.SessionType.SINGLE_CHAT) {
                view.nickName.setVisibility(View.VISIBLE);
                view.nickName.setText(message.getSenderNickname());
            }
            try {
                MsgExpand msgExpand = (MsgExpand) message.getExt();
                view.title.setText(msgExpand.locationInfo.name);
                view.address.setText(msgExpand.locationInfo.addr);
                Glide.with(itemView.getContext()).load(msgExpand.locationInfo.url).into(view.map);
            } catch (Exception e) {
            }
            view.sendState.setSendState(message.getStatus());
            view.content.setOnClickListener(v -> Common.toMap(message, v));

        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgLocation2Binding view = LayoutMsgLocation2Binding.bind(itemView);
            view.avatar2.load(message.getSenderFaceUrl());
            try {
                MsgExpand msgExpand = (MsgExpand) message.getExt();
                view.title2.setText(msgExpand.locationInfo.name);
                view.address2.setText(msgExpand.locationInfo.addr);
                Glide.with(itemView.getContext()).load(msgExpand.locationInfo.url).into(view.map2);
            } catch (Exception e) {
            }
            view.sendState2.setSendState(message.getStatus());
            view.content2.setOnClickListener(v -> Common.toMap(message, v));
        }
    }


    public static class MergeView extends MessageViewHolder.MsgViewHolder {

        public MergeView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        protected int getLeftInflatedId() {
            return R.layout.layout_msg_merge_left;
        }

        @Override
        protected int getRightInflatedId() {
            return R.layout.layout_msg_merge_right;
        }


        @Override
        protected void bindLeft(View itemView, Message message) {
            LayoutMsgMergeLeftBinding view = LayoutMsgMergeLeftBinding.bind(itemView);
            view.sendState.setSendState(message.getStatus());
            view.avatar.load(message.getSenderFaceUrl());
            if (message.getSessionType() != Constant.SessionType.SINGLE_CHAT) {
                view.sendNickName.setVisibility(View.VISIBLE);
                view.sendNickName.setText(message.getSenderNickname());
            }
            MergeElem mergeElem = message.getMergeElem();
            view.title1.setText(mergeElem.getTitle());
            try {
                view.history11.setText(mergeElem.getAbstractList().get(0));
                view.history12.setText(mergeElem.getAbstractList().get(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
            view.content.setOnClickListener(new ClickJumpDetail(mergeElem));
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgMergeRightBinding view = LayoutMsgMergeRightBinding.bind(itemView);
            view.avatar2.load(message.getSenderFaceUrl());
            view.sendState2.setSendState(message.getStatus());
            MergeElem mergeElem = message.getMergeElem();
            view.title2.setText(mergeElem.getTitle());
            try {
                view.history21.setText(mergeElem.getAbstractList().get(0));
                view.history22.setText(mergeElem.getAbstractList().get(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
            view.content2.setOnClickListener(new ClickJumpDetail(mergeElem));
        }

        private static class ClickJumpDetail implements View.OnClickListener {
            MergeElem mergeElem;

            public ClickJumpDetail(MergeElem mergeElem) {
                this.mergeElem = mergeElem;
            }

            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(),
                    ChatHistoryDetailsActivity.class).putExtra(Constant.K_RESULT,
                    GsonHel.toJson(mergeElem.getMultiMessage())));
            }
        }

    }

    public static class BusinessCardView extends MessageViewHolder.MsgViewHolder {

        public BusinessCardView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        protected int getLeftInflatedId() {
            return R.layout.layout_msg_card_left;
        }

        @Override
        protected int getRightInflatedId() {
            return R.layout.layout_msg_card_right;
        }

        @Override
        protected void bindLeft(View itemView, Message message) {
            LayoutMsgCardLeftBinding view = LayoutMsgCardLeftBinding.bind(itemView);
            view.sendState.setSendState(message.getStatus());
            view.avatar.load(message.getSenderFaceUrl());
            if (message.getSessionType() != Constant.SessionType.SINGLE_CHAT) {
                view.sendNickName.setVisibility(View.VISIBLE);
                view.sendNickName.setText(message.getSenderNickname());
            }
            String friendInfo = message.getContent();
            FriendInfo friendInfoBean = GsonHel.fromJson(friendInfo, FriendInfo.class);
            view.nickName.setText(friendInfoBean.getNickname());
            view.otherAvatar.load(friendInfoBean.getFaceURL());
            jump(view.content, friendInfoBean.getUserID());
        }

        void jump(View view, String uid) {
            view.setOnClickListener(v -> ARouter.getInstance().build(Routes.Main.PERSON_DETAIL).withString(Constant.K_ID, uid).navigation(view.getContext()));
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgCardRightBinding view = LayoutMsgCardRightBinding.bind(itemView);
            view.sendState2.setSendState(message.getStatus());
            view.avatar2.load(message.getSenderFaceUrl());
            String friendInfo = message.getContent();
            FriendInfo friendInfoBean = GsonHel.fromJson(friendInfo, FriendInfo.class);
            view.nickName2.setText(friendInfoBean.getNickname());
            view.otherAvatar2.load(friendInfoBean.getFaceURL());
            jump(view.content2, friendInfoBean.getUserID());
        }
    }

    public static class NotificationItemHo extends MessageViewHolder.MsgViewHolder {


        public NotificationItemHo(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        protected int getLeftInflatedId() {
            return R.layout.layout_msg_notice_left;
        }

        @Override
        protected int getRightInflatedId() {
            return 0;
        }

        @Override
        protected void bindLeft(View itemView, Message message) {
            LayoutMsgNoticeLeftBinding v = LayoutMsgNoticeLeftBinding.bind(itemView);
            MsgExpand msgExpand = (MsgExpand) message.getExt();
            v.avatar.load(msgExpand.oaNotification.notificationFaceURL);
            v.nickName.setText(msgExpand.oaNotification.notificationName);
            v.title.setText(msgExpand.oaNotification.notificationName);
            v.content.setText(msgExpand.oaNotification.text);
            try {
                if (msgExpand.oaNotification.mixType == 1) {
                    v.picture.setVisibility(View.VISIBLE);
                    Glide.with(v.getRoot().getContext()).load(msgExpand.oaNotification.pictureElem.getBigPicture().getUrl()).into(v.picture);
                } else {
                    v.picture.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void bindRight(View itemView, Message message) {

        }
    }

    public static class CallHistoryView extends AudioView {

        private CallHistory callHistory;
        private MsgExpand msgExpand;
        private boolean isAudio;

        public CallHistoryView(ViewGroup parent) {
            super(parent);
        }

        @Override
        public void bindData(Message message, int position) {
            msgExpand = (MsgExpand) message.getExt();
            callHistory = msgExpand.callHistory;
            isAudio = callHistory.getType().equals("audio");
            super.bindData(message, position);

        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void bindLeft(View itemView, Message message) {
            final LayoutMsgAudioLeftBinding v = LayoutMsgAudioLeftBinding.bind(itemView);
            v.avatar.load(message.getSenderFaceUrl());
            v.lottieView.setImageResource(isAudio ?
                io.openim.android.ouicore.R.mipmap.ic_voice_call :
                io.openim.android.ouicore.R.mipmap.ic_video_call);

            if (callHistory.isSuccess()) v.duration.setText(msgExpand.callDuration);
            else {
                if (callHistory.getFailedState() == 0)
                    v.duration.setText(io.openim.android.ouicore.R.string.conn_failed);
                if (callHistory.getFailedState() == 1)
                    v.duration.setText(io.openim.android.ouicore.R.string.cancelled);
                if (callHistory.getFailedState() == 2)
                    v.duration.setText(io.openim.android.ouicore.R.string.ot_refuses);
            }
            v.content.setOnClickListener(v1 -> chatVM.singleChatCall(!isAudio));
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void bindRight(View itemView, Message message) {
            final LayoutMsgAudioRightBinding v = LayoutMsgAudioRightBinding.bind(itemView);
            v.avatar2.load(message.getSenderFaceUrl());
            v.sendState2.setSendState(message.getStatus());
            v.lottieView2.setVisibility(View.GONE);
            v.icon2.setVisibility(View.VISIBLE);
            v.icon2.setImageResource(isAudio ? io.openim.android.ouicore.R.mipmap.ic_voice_call :
                io.openim.android.ouicore.R.mipmap.ic_video_call);
            if (callHistory.isSuccess()) v.duration2.setText(msgExpand.callDuration);
            else {
                if (callHistory.getFailedState() == 0)
                    v.duration2.setText(io.openim.android.ouicore.R.string.conn_failed);
                if (callHistory.getFailedState() == 1)
                    v.duration2.setText(io.openim.android.ouicore.R.string.cancelled);
                if (callHistory.getFailedState() == 2)
                    v.duration2.setText(io.openim.android.ouicore.R.string.ot_refuses);
            }
            v.content2.setOnClickListener(v1 -> chatVM.singleChatCall(!isAudio));
        }


    }

    public static class QuoteTXTView extends TXTView {

        public QuoteTXTView(ViewGroup parent) {
            super(parent);
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void bindLeft(View itemView, Message message) {
            super.bindLeft(itemView, message);
            LayoutMsgTxtLeftBinding v = LayoutMsgTxtLeftBinding.bind(itemView);
            v.quoteLy1.setVisibility(View.VISIBLE);
            QuoteElem quoteElem = message.getQuoteElem();
            if (!handleSequence(v.content, message)) v.content.setText(quoteElem.getText());

            message = quoteElem.getQuoteMessage();
            int contentType = message.getContentType();
            if (contentType == Constant.MsgType.TXT) {
                v.quoteContent1.setText(message.getSenderNickname() + ":" + IMUtil.getMsgParse(message));
                v.picture1.setVisibility(View.GONE);
            } else {
                v.picture1.setVisibility(View.VISIBLE);
                if (contentType == Constant.MsgType.PICTURE) {
                    v.quoteContent1.setText(message.getSenderNickname() + ":");
                    Common.loadPicture(v.picture1, message.getPictureElem());
                    toPreview(v.quoteLy1, message.getPictureElem().getSourcePicture().getUrl(),
                        null);
                }
                if (contentType == Constant.MsgType.VIDEO) {
                    v.quoteContent1.setText(message.getSenderNickname() + ":");
                    Common.loadVideoSnapshot(v.picture1, message.getVideoElem());
                    toPreview(v.quoteLy1, message.getVideoElem().getVideoUrl(),
                        message.getVideoElem().getSnapshotUrl());
                }
                if (contentType == Constant.MsgType.LOCATION) {
                    try {
                        MsgExpand msgExpand = (MsgExpand) message.getExt();
                        v.quoteContent1.setText(message.getSenderNickname() + ":" + msgExpand.locationInfo.name + "(" + msgExpand.locationInfo.addr + ")");
                        Glide.with(itemView.getContext()).load(msgExpand.locationInfo.url).into(v.picture1);
                        Message finalMessage = message;
                        v.quoteLy1.setOnClickListener(v1 -> Common.toMap(finalMessage, v1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void bindRight(View itemView, Message message) {
            super.bindRight(itemView, message);
            LayoutMsgTxtRightBinding v = LayoutMsgTxtRightBinding.bind(itemView);
            v.quoteLy2.setVisibility(View.VISIBLE);
            QuoteElem quoteElem = message.getQuoteElem();
            if (!handleSequence(v.content2, message)) v.content2.setText(quoteElem.getText());

            message = quoteElem.getQuoteMessage();
            int contentType = message.getContentType();
            if (contentType == Constant.MsgType.TXT) {
                v.quoteContent2.setText(message.getSenderNickname() + ":" + IMUtil.getMsgParse(message));
                v.picture2.setVisibility(View.GONE);
            } else {
                v.picture2.setVisibility(View.VISIBLE);
                if (contentType == Constant.MsgType.PICTURE) {
                    v.quoteContent2.setText(message.getSenderNickname() + ":" + IMUtil.getMsgParse(message));
                    Common.loadPicture(v.picture2, message.getPictureElem());
                    toPreview(v.quoteLy2, message.getPictureElem().getSourcePicture().getUrl(),
                        null);
                }
                if (contentType == Constant.MsgType.VIDEO) {
                    v.quoteContent2.setText(message.getSenderNickname() + ":" + IMUtil.getMsgParse(message));
                    Common.loadVideoSnapshot(v.picture2, message.getVideoElem());
                    toPreview(v.quoteLy2, message.getVideoElem().getVideoUrl(),
                        message.getVideoElem().getSnapshotUrl());
                }
                if (contentType == Constant.MsgType.LOCATION) {
                    try {
                        MsgExpand msgExpand = (MsgExpand) message.getExt();
                        v.quoteContent2.setText(message.getSenderNickname() + ":" + msgExpand.locationInfo.name + "(" + msgExpand.locationInfo.addr + ")");

                        Glide.with(itemView.getContext()).load(msgExpand.locationInfo.url).into(v.picture2);

                        Message finalMessage = message;
                        v.quoteLy2.setOnClickListener(v1 -> Common.toMap(finalMessage, v1));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
