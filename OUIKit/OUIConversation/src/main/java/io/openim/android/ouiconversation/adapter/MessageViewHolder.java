package io.openim.android.ouiconversation.adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.ImageView;
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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
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
import io.openim.android.ouiconversation.databinding.LayoutMsgGroupAnnouncementLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgGroupAnnouncementRightBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgImgLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgImgRightBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgLocation1Binding;
import io.openim.android.ouiconversation.databinding.LayoutMsgLocation2Binding;
import io.openim.android.ouiconversation.databinding.LayoutMsgMergeLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgMergeRightBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgNoticeLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgTxtLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgTxtRightBinding;
import io.openim.android.ouiconversation.ui.ChatActivity;
import io.openim.android.ouiconversation.ui.PreviewMediaActivity;
import io.openim.android.ouiconversation.vm.CustomEmojiVM;
import io.openim.android.ouiconversation.widget.SendStateView;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouiconversation.ui.fragment.InputExpandFragment;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.CallHistory;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.ex.AtUser;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.ByteUtil;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.MediaFileUtil;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.vm.ForwardVM;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.vm.SelectTargetVM;
import io.openim.android.ouicore.vm.PreviewMediaVM;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.ouicore.voice.listener.PlayerListener;
import io.openim.android.ouicore.voice.player.SMediaPlayer;
import io.openim.android.ouicore.widget.AvatarImage;
import io.openim.android.ouicore.widget.PlaceHolderDrawable;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.enums.MessageStatus;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.models.AttachedInfoElem;
import io.openim.android.sdk.models.CardElem;
import io.openim.android.sdk.models.MergeElem;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.QuoteElem;
import io.openim.android.sdk.models.VideoElem;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.plugins.RxJavaPlugins;

public class MessageViewHolder {
    public static RecyclerView.ViewHolder createViewHolder(@NonNull ViewGroup parent,
                                                           int viewType) {
        if (viewType == Constants.LOADING) return new LoadingView(parent);
        if (viewType == MessageType.TEXT) return new TXTView(parent);
        if (viewType == MessageType.PICTURE || viewType == MessageType.CUSTOM_FACE)
            return new IMGView(parent);
        if (viewType == MessageType.VOICE) return new AudioView(parent);
        if (viewType == MessageType.VIDEO) return new VideoView(parent);
        if (viewType == MessageType.FILE) return new FileView(parent);
        if (viewType == MessageType.LOCATION) return new LocationView(parent);
        if (viewType == MessageType.OA_NTF) return new NotificationItemView(parent);
        if (viewType == MessageType.GROUP_ANNOUNCEMENT_NTF)
            return new GroupAnnouncementView(parent);
        if (viewType >= MessageType.NTF_BEGIN) return new NoticeView(parent);
        if (viewType == Constants.MsgType.LOCAL_CALL_HISTORY) return new CallHistoryView(parent);
        if (viewType == MessageType.CARD) return new BusinessCardView(parent);
        if (viewType == MessageType.QUOTE) return new QuoteTXTView(parent);

        return new TXTView(parent);
    }

    public abstract static class MsgViewHolder extends RecyclerView.ViewHolder {
        protected RecyclerView recyclerView;
        protected MessageAdapter messageAdapter;

        protected PopupWindow popupWindow;
        protected Message message;
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
                isOwn = getSendWay(message);
                if (isOwn) {
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
                unifiedProcess(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected boolean getSendWay(Message message) {
            return message.getSendID().equals(BaseApp.inst().loginCertificate.userID);
        }

        /**
         * 统一处理
         */
        protected void unifiedProcess(int position) {
            MsgExpand msgExpand = (MsgExpand) message.getExt();
            hFirstItem(position);

            hAvatar();
            hName();
            hContentView();
            showTime(msgExpand);
            hSendState();
        }

        public void hFirstItem(int position) {
            View root = itemView.findViewById(R.id.root);
            root.setPadding(0, position == 0 ? Common.dp2px(15) : 0, 0, 0);
        }

        /**
         * 处理发送状态
         */
        private void hSendState() {
            if (isOwn) {
                SendStateView sendStateView = itemView.findViewById(R.id.sendState2);
                if (null == sendStateView) return;
                sendStateView.setOnClickListener(new OnDedrepClickListener() {
                    @Override
                    public void click(View v) {
                        chatVM.sendMsg(message, true);
                    }
                });
            }
        }

        /**
         * 处理名字
         */
        @SuppressLint("SetTextI18n")
        public void hName() {
            TextView nickName;
            if (isOwn) nickName = itemView.findViewById(R.id.nickName2);
            else nickName = itemView.findViewById(R.id.nickName);
            if (null != nickName) {
                nickName.setVisibility(View.VISIBLE);
                nickName.setMaxLines(1);
                nickName.setMaxEms(18);
                nickName.setEllipsize(TextUtils.TruncateAt.MIDDLE);

                boolean isSending = message.getStatus() == MessageStatus.SENDING;
                String time = TimeUtil.getTimeString(isSending ? System.currentTimeMillis() :
                    message.getSendTime());
                if (isSending || message.getSessionType() == ConversationType.SINGLE_CHAT) {
                    nickName.setText(time);
                } else nickName.setText(message.getSenderNickname() + "  " + time);
            }
        }

        /**
         * 处理头像
         */
        public void hAvatar() {
            AvatarImage avatarImage = itemView.findViewById(R.id.avatar);
            AvatarImage avatarImage2 = itemView.findViewById(R.id.avatar2);
            if (null != avatarImage) {
                avatarImage.load(message.getSenderFaceUrl(), message.getSenderNickname());
                AtomicBoolean isLongClick = new AtomicBoolean(false);
                avatarImage.setOnClickListener(v -> {
                    if (isLongClick.get()) {
                        isLongClick.set(false);
                        return;
                    }
                    ARouter.getInstance().build(Routes.Main.PERSON_DETAIL).withString(Constants.K_ID, message.getSendID()).withString(Constants.K_GROUP_ID, message.getGroupID()).navigation();
                });
            }
            if (null != avatarImage2) {
                avatarImage2.load(message.getSenderFaceUrl(), message.getSenderNickname());
                avatarImage2.setOnClickListener(v -> ARouter.getInstance().build(Routes.Main.PERSON_DETAIL).withString(Constants.K_ID, message.getSendID()).withString(Constants.K_GROUP_ID, message.getGroupID()).navigation());
            }
        }

        public void hContentView() {
            View contentView;
            if (isOwn) contentView = itemView.findViewById(R.id.content2);
            else contentView = itemView.findViewById(R.id.content);
            if (null == contentView) return;

            showMsgExMenu(contentView);
        }

        private void showTime(MsgExpand msgExpand) {
            TextView notice = itemView.findViewById(R.id.notice);
            if (msgExpand.isShowTime) {
                //显示时间
                String time = TimeUtil.getTimeString(message.getSendTime());
                notice.setVisibility(View.VISIBLE);
                notice.setText(time);
            } else notice.setVisibility(View.GONE);
        }


        /***
         * 长按显示扩展菜单
         * @param view
         */
        protected void showMsgExMenu(View view) {
            final float[] touchY = new float[1];
            view.setOnTouchListener((v, event) -> {
                touchY[0] = event.getY();
                return false;
            });
            view.setOnLongClickListener(v -> {
                if (null != chatVM.enableMultipleSelect.val() && chatVM.enableMultipleSelect.val())
                    return true;
                List<Integer> menuIcons = new ArrayList<>();
                List<String> menuTitles = new ArrayList<>();
                final ChatActivity.LinearLayoutMg linearLayoutManager =
                    (ChatActivity.LinearLayoutMg) recyclerView.getLayoutManager();
                if (null == popupWindow) {
                    popupWindow = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (null != linearLayoutManager)
                        popupWindow.setOnDismissListener(() -> linearLayoutManager.setCanScrollVertically(true));
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
                                int iconRes = (int) getItems().get(position);
                                List<String> menuTitles =
                                    (List<String>) popupWindow.getContentView().getTag();

                                holder.v.menu.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                                    v.getContext().getDrawable(iconRes), null, null);
                                holder.v.menu.setText(menuTitles.get(position));
                                holder.v.menu.setTextColor(Color.WHITE);
                                holder.v.menu.setOnClickListener(v1 -> {
                                    popupWindow.dismiss();
                                    if (iconRes == R.mipmap.ic_c_copy) {
                                        TextView textView;
                                        if (message.getContentType() == MessageType.GROUP_ANNOUNCEMENT_NTF) {
                                            textView = view.findViewById(R.id.detail);
                                            if (null == textView)
                                                textView = view.findViewById(R.id.detail2);
                                        } else {
                                            textView = view.findViewById(R.id.content);
                                            if (null == textView)
                                                textView = view.findViewById(R.id.content2);
                                        }
                                        Common.copy(textView.getText().toString());
                                        chatVM.toast(BaseApp.inst().getString(io.openim.android.ouicore.R.string.copy_succ));
                                    }
                                    if (iconRes == R.mipmap.ic_withdraw) {
                                        chatVM.revokeMessage(message);
                                    }
                                    if (iconRes == R.mipmap.ic_delete) {
                                        if (message.getStatus() != MessageStatus.SENDING)
                                            chatVM.deleteMessageFromLocalAndSvr(message);
                                    }
                                    if (iconRes == R.mipmap.ic_forward) {
                                        Easy.find(ForwardVM.class).createForwardMessage(message);

                                        Easy.installVM(SelectTargetVM.class);
                                        ARouter.getInstance().build(Routes.Group.SELECT_TARGET).navigation((Activity) view.getContext(), Constants.Event.FORWARD);
                                    }
                                });
                            }
                        };
                    view1.recyclerview.setAdapter(adapter);
                }
                AttachedInfoElem attachedInfoElem = message.getAttachedInfoElem() != null ? message.getAttachedInfoElem() : new AttachedInfoElem();
                if (message.getContentType() == MessageType.TEXT
                    || message.getContentType() == MessageType.AT_TEXT
                    || message.getContentType() == MessageType.GROUP_ANNOUNCEMENT_NTF
                    || message.getContentType() == MessageType.QUOTE) {
                    menuIcons.add(R.mipmap.ic_c_copy);
                    menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.copy));
                }
                if (message.getStatus() != MessageStatus.SENDING && !attachedInfoElem.isPrivateChat()) {
                    menuIcons.add(R.mipmap.ic_delete);
                    menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.delete));
                }

                boolean canWithdraw = message.getContentType()
                    != MessageType.GROUP_ANNOUNCEMENT_NTF;
                if (canWithdraw && message.getStatus() == MessageStatus.SUCCEEDED) {
                    if (chatVM.isAdminOrCreator) {
                        //群
                        if (isOwn || !message.getSendID().equals(chatVM.groupInfo.val().getOwnerUserID())) {
                            menuIcons.add(R.mipmap.ic_withdraw);
                            menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.withdraw));
                        }
                    } else if (isOwn) {
                        //5分钟内可以撤回
                        if (System.currentTimeMillis() - message.getSendTime() < (1000 * 60 * 5)) {
                            menuIcons.add(R.mipmap.ic_withdraw);
                            menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.withdraw));
                        }
                    }
                }

                if (message.getContentType() < MessageType.NTF_BEGIN
                    && message.getStatus() == MessageStatus.SUCCEEDED
                    && !attachedInfoElem.isPrivateChat()) {
                    menuIcons.add(R.mipmap.ic_forward);
                    menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.forward));
                }
                LayoutMsgExMenuBinding vb =
                    LayoutMsgExMenuBinding.bind(popupWindow.getContentView());

                if (!menuIcons.isEmpty()) {
                    vb.recyclerview.setLayoutManager(new GridLayoutManager(view.getContext(),
                        Math.min(menuIcons.size(), 4)));
                    popupWindow.getContentView().setTag(menuTitles);
                    adapter.setItems(menuIcons);

                    int yDelay = Common.dp2px(5);
                    popupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED,
                        View.MeasureSpec.UNSPECIFIED);
                    Rect globalVisibleRect = new Rect();
                    v.getGlobalVisibleRect(globalVisibleRect);
                    int popupHeight = popupWindow.getContentView().getMeasuredHeight();
                    int screenH = BaseApp.inst().getResources().getDisplayMetrics().heightPixels;
                    int y = (popupHeight + v.getMeasuredHeight() + yDelay);
                    float titleHeight =
                        BaseApp.inst().getResources().getDimension(io.openim.android.ouicore.R.dimen.comm_title_high);
                    int downMenuHeight = Common.dp2px(50);
                    if (globalVisibleRect.top - titleHeight > (popupHeight + yDelay)) {
                        y = -y;
                        vb.downArrow.setVisibility(View.VISIBLE);
                        vb.topArrow.setVisibility(View.GONE);
                    } else if (screenH - globalVisibleRect.bottom - downMenuHeight > (popupHeight + yDelay)) {
                        y = yDelay;
                        vb.topArrow.setVisibility(View.VISIBLE);
                        vb.downArrow.setVisibility(View.GONE);
                    } else {
                        vb.topArrow.setVisibility(View.VISIBLE);
                        vb.downArrow.setVisibility(View.GONE);
                        y = (int) touchY[0];
                    }
                    popupWindow.showAsDropDown(v,
                        -(popupWindow.getContentView().getMeasuredWidth() - v.getMeasuredWidth()) / 2
                        , y);
                    if (null != linearLayoutManager)
                        linearLayoutManager.setCanScrollVertically(false);
                }
                return true;
            });
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
            view.setOnClickListener(v -> {
                PreviewMediaVM previewMediaVM = Easy.installVM(PreviewMediaVM.class);
                PreviewMediaVM.MediaData mediaData =
                    new PreviewMediaVM.MediaData(message.getClientMsgID());
                mediaData.mediaUrl = url;
                mediaData.isVideo = MediaFileUtil.isVideoType(url);
                mediaData.thumbnail = firstFrameUrl;
                previewMediaVM.preview(mediaData);
                view.getContext().startActivity(new Intent(view.getContext(),
                    PreviewMediaActivity.class));
            });
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
            hFirstItem(position);
            TextView textView = itemView.findViewById(R.id.notice);
            textView.setVisibility(View.VISIBLE);

            MsgExpand msgExpand = (MsgExpand) message.getExt();
            textView.setText(msgExpand.tips);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
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
            v.avatar.load(message.getSenderFaceUrl(), message.getSenderNickname());
            // 防止错误信息所导致的空指针
            if (message.getContentType() == MessageType.AT_TEXT && message.getTextElem() == null) {
                String content = message.getAtTextElem().getText();
                v.content.setText(content);
                return;
            }
            String content = message.getTextElem().getContent();
            v.content.setText(content);
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgTxtRightBinding v = LayoutMsgTxtRightBinding.bind(itemView);
            v.avatar2.load(message.getSenderFaceUrl(), message.getSenderNickname());
            v.sendState2.setSendState(message.getStatus());
            // 防止错误信息所导致的空指针
            if (message.getContentType() == MessageType.AT_TEXT && message.getTextElem() == null) {
                String content = message.getAtTextElem().getText();
                v.content2.setText(content);
                return;
            }

            String content = message.getTextElem().getContent();
            v.content2.setText(content);
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

            v.sendState.setSendState(message.getStatus());
            String url = loadIMG(v.content, message);
            toPreview(v.content, url, null);
        }

        private String loadIMG(ImageView img, Message message) {
            String url = message.getPictureElem().getSourcePicture().getUrl();
            int w = message.getPictureElem().getSourcePicture().getWidth();
            int h = message.getPictureElem().getSourcePicture().getHeight();
            scale(img, w, h);
            Glide.with(BaseApp.inst()).load(message.getPictureElem().getSnapshotPicture().getUrl()).placeholder(new PlaceHolderDrawable(BaseApp.inst())).error(io.openim.android.ouicore.R.mipmap.ic_chat_photo).fitCenter().transform(new RoundedCorners(15)).into(img);
            return url;
        }

        public void scale(View img, int sourceW, int sourceH) {
            int pictureWidth = Common.dp2px(180);
            int _trulyWidth;
            int _trulyHeight;
            if (sourceW == 0) {
                sourceW = 1;
            }
            if (sourceH == 0) {
                sourceH = 1;
            }
            if (pictureWidth > sourceW) {
                _trulyWidth = sourceW;
                _trulyHeight = sourceH;
            } else {
                _trulyWidth = pictureWidth;
                _trulyHeight = _trulyWidth * sourceH / sourceW;
            }
            ViewGroup.LayoutParams params = img.getLayoutParams();
            params.width = _trulyWidth;
            params.height = _trulyHeight;
            img.setLayoutParams(params);
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgImgRightBinding v = LayoutMsgImgRightBinding.bind(itemView);
            v.avatar2.load(message.getSenderFaceUrl(), message.getSenderNickname());
            v.videoPlay2.setVisibility(View.GONE);
            v.mask2.setVisibility(View.GONE);

            v.sendState2.setSendState(message.getStatus());
            String url = loadIMG(v.content2, message);
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
            view.duration.setText(message.getSoundElem().getDuration() + "``");
            view.content.setOnClickListener(v -> clickPlay(message, view.lottieView));
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            final LayoutMsgAudioRightBinding view = LayoutMsgAudioRightBinding.bind(itemView);
            view.avatar2.load(message.getSenderFaceUrl(), message.getSenderNickname());
            view.sendState2.setSendState(message.getStatus());
            view.duration2.setText(message.getSoundElem().getDuration() + "``");

            view.content2.setOnClickListener(v -> clickPlay(message, view.lottieView2));
        }


        public void clickPlay(Message message, LottieAnimationView lottieView) {
            String sourceUrl = message.getSoundElem().getSourceUrl();
            if (TextUtils.isEmpty(sourceUrl)) return;
            SPlayer.instance().getMediaPlayer();
            if (SPlayer.instance().isPlaying()) {
                SPlayer.instance().stop();
            } else {
                SPlayer.instance().playByUrl(sourceUrl, new PlayerListener() {
                    @Override
                    public void LoadSuccess(SMediaPlayer mediaPlayer) {
                        playingMessage = message;
                        lottieView.playAnimation();
                        mediaPlayer.start();
                    }

                    @Override
                    public void Loading(SMediaPlayer mediaPlayer, int i) {

                    }

                    @Override
                    public void onCompletion(SMediaPlayer mediaPlayer) {
                        mediaPlayer.stop();
                    }

                    @Override
                    public void onError(Exception e) {
                        lottieView.cancelAnimation();
                        lottieView.setProgress(1);
                    }
                });
            }

            SPlayer.instance().getMediaPlayer().setOnPlayStateListener(new SMediaPlayer.OnPlayStateListener() {
                @Override
                public void started() {
                    if (!isOwn) {
                        RxJavaPlugins.setErrorHandler(handler -> {
                            if (handler.getCause() instanceof UndeliverableException) {
                                L.e(handler.getMessage());
                            }
                        });
                        chatVM.markReadWithObservable(message)
                            .subscribe(new DisposableObserver<String>() {
                                @Override
                                public void onNext(String result) {

                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {
                                    try {
                                        if (!message.isRead()) {
                                            message.setRead(true);
                                            message.getAttachedInfoElem().setHasReadTime(System.currentTimeMillis());
                                            Common.UIHandler.post(() -> {
                                                messageAdapter.notifyItemChanged(chatVM.messages.val().indexOf(message));
                                            });
                                        }
                                    }catch (Exception e) {
                                        L.e(e.getMessage());
                                    }
                                }
                            });
                    }
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
        public void hContentView() {
            View contentView;
            if (isOwn) contentView = itemView.findViewById(R.id.videoPlay2);
            else contentView = itemView.findViewById(R.id.contentGroup);
            if (null == contentView) return;

            showMsgExMenu(contentView);
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgImgRightBinding view = LayoutMsgImgRightBinding.bind(itemView);
            view.sendState2.setSendState(message.getStatus());
            view.mask2.setVisibility(View.VISIBLE);
            view.videoPlay2.setVisibility(View.VISIBLE);
            boolean sendSuccess = message.getStatus() == MessageStatus.SUCCEEDED;
            if (sendSuccess) view.circleBar2.reset();
            view.mask2.setVisibility(sendSuccess ? View.GONE : View.VISIBLE);

            VideoElem videoElem = message.getVideoElem();
            String secondFormat = TimeUtil.getTime(videoElem.getDuration() * 1000,
                TimeUtil.minuteTimeFormat);
            view.duration2.setText(secondFormat);
            scale((View) view.content2.getParent(), videoElem.getSnapshotWidth(),
                videoElem.getSnapshotHeight());
            scale(view.content2, videoElem.getSnapshotWidth(), videoElem.getSnapshotHeight());
            Glide.with(BaseApp.inst()).load(message.getVideoElem().getSnapshotUrl()).placeholder(new PlaceHolderDrawable(BaseApp.inst())).error(io.openim.android.ouicore.R.mipmap.ic_chat_photo).fitCenter().transform(new RoundedCorners(15)).into(view.content2);
            preview(message, view.videoPlay2);
        }


        private void preview(Message message, View view) {
            String snapshotUrl = message.getVideoElem().getSnapshotUrl();
            toPreview(view, message.getVideoElem().getVideoUrl(), snapshotUrl);
        }


        @Override
        protected void bindLeft(View itemView, Message message) {
            LayoutMsgImgLeftBinding view = LayoutMsgImgLeftBinding.bind(itemView);

            view.sendState.setSendState(message.getStatus());
            view.playBtn.setVisibility(View.VISIBLE);
            view.circleBar.setVisibility(View.VISIBLE);
            view.durationLeft.setText(TimeUtil.getTime(message.getVideoElem().getDuration() * 1000,
                TimeUtil.minuteTimeFormat));

            int w = message.getVideoElem().getSnapshotWidth();
            int h = message.getVideoElem().getSnapshotHeight();
            scale(view.content, w, h);
            Glide.with(BaseApp.inst()).load(message.getVideoElem().getSnapshotUrl()).placeholder(new PlaceHolderDrawable(BaseApp.inst())).error(io.openim.android.ouicore.R.mipmap.ic_chat_photo).fitCenter().transform(new RoundedCorners(15)).into(view.content);
            preview(message, view.contentGroup);
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

            view.title.setText(message.getFileElem().getFileName());
            Long size = message.getFileElem().getFileSize();
            view.size.setText(ByteUtil.bytes2kb(size) + "");

            view.sendState.setSendState(message.getStatus());
            view.content.setOnClickListener(v -> {
                if (!view.downloadView.completed) {
                    chatVM.toast(v.getContext().getString(io.openim.android.ouicore.R.string.file_download));
                    return;
                }
                GetFilePathFromUri.openFile(v.getContext(), message);
            });
            String path = message.getFileElem().getSourceUrl();
            view.downloadView.setRes(path);
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgFileRightBinding view = LayoutMsgFileRightBinding.bind(itemView);
            view.avatar2.load(message.getSenderFaceUrl(), message.getSenderNickname());

            view.title2.setText(message.getFileElem().getFileName());
            Long size = message.getFileElem().getFileSize();
            view.size2.setText(ByteUtil.bytes2kb(size) + "");

            view.sendState2.setSendState(message.getStatus());

            String path = message.getFileElem().getFilePath();
            boolean isLocal = GetFilePathFromUri.fileIsExists(path);
            if (!isLocal) path = message.getFileElem().getSourceUrl();
            if (isLocal) {
                view.downloadView.setVisibility(View.GONE);
                view.fileUploadView.setVisibility(View.VISIBLE);
                view.fileUploadView.setRes(path);
                view.fileUploadView.setForegroundVisibility(message.getStatus() == MessageStatus.SUCCEEDED);
            } else {
                view.downloadView.setVisibility(View.VISIBLE);
                view.fileUploadView.setVisibility(View.GONE);
                view.downloadView.setRes(path);
            }

            view.content2.setOnClickListener(v -> {
                if (!isLocal) {
                    if (!view.downloadView.completed) {
                        chatVM.toast(v.getContext().getString(io.openim.android.ouicore.R.string.file_download));
                        return;
                    }
                }
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
            view.avatar2.load(message.getSenderFaceUrl(), message.getSenderNickname());
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

            CardElem cardElem = message.getCardElem();
            view.cardNickName.setText(cardElem.getNickname());
            view.otherAvatar.load(cardElem.getFaceURL(), cardElem.getNickname());
            jump(view.content, cardElem.getUserID());
        }

        void jump(View view, String uid) {
            view.setOnClickListener(v -> ARouter.getInstance().build(Routes.Main.PERSON_DETAIL).withString(Constants.K_ID, uid).navigation(view.getContext()));
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgCardRightBinding view = LayoutMsgCardRightBinding.bind(itemView);
            view.sendState2.setSendState(message.getStatus());
            view.avatar2.load(message.getSenderFaceUrl(), message.getSenderNickname());

            CardElem cardElem = message.getCardElem();
            view.cardNickName2.setText(cardElem.getNickname());
            view.otherAvatar2.load(cardElem.getFaceURL(), cardElem.getNickname());
            jump(view.content2, cardElem.getUserID());
        }
    }

    public static class NotificationItemView extends MessageViewHolder.MsgViewHolder {


        public NotificationItemView(ViewGroup itemView) {
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
            v.noticeAvatar.load(msgExpand.oaNotification.notificationFaceURL,
                msgExpand.oaNotification.notificationName);
            v.noticeNickName.setText(msgExpand.oaNotification.notificationName);
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
        private boolean isAudio = false;

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
            v.lottieView.setImageResource(isAudio ?
                io.openim.android.ouicore.R.mipmap.ic_voice_call :
                io.openim.android.ouicore.R.mipmap.ic_video_call);

            if (callHistory.isSuccess()) v.duration.setText(msgExpand.callDuration);
            else {
                if (callHistory.getFailedState() == 0)
                    v.duration.setText(io.openim.android.ouicore.R.string.conn_failed);
                if (callHistory.getFailedState() == 1)
                    v.duration.setText(io.openim.android.ouicore.R.string.cancelled);
                if (callHistory.getFailedState() == 3)
                    v.duration.setText(io.openim.android.ouicore.R.string.declined);
            }
            v.content.setOnClickListener(v1 -> chatVM.singleChatCall(!isAudio));
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void bindRight(View itemView, Message message) {
            final LayoutMsgAudioRightBinding v = LayoutMsgAudioRightBinding.bind(itemView);
            v.avatar2.load(message.getSenderFaceUrl(), message.getSenderNickname());
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
            LayoutMsgTxtLeftBinding v = LayoutMsgTxtLeftBinding.bind(itemView);
            v.quoteLy1.setVisibility(View.VISIBLE);
            QuoteElem quoteElem = message.getQuoteElem();

            message = quoteElem.getQuoteMessage();
            int contentType = message.getContentType();
            if (contentType == MessageType.REVOKE_MESSAGE_NTF) {
                v.quoteContent1.setText(message.getSenderNickname() + ":" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.quote_delete_tips));
                v.picture1.setVisibility(View.GONE);
                return;
            }
            v.downloadView1.setVisibility(View.GONE);
            if (contentType == MessageType.FILE) {
                v.downloadView1.setVisibility(View.VISIBLE);
                String tips =
                    message.getSenderNickname() + ":" + message.getFileElem().getFileName();
                String path = message.getFileElem().getFilePath();
                boolean isLocal = GetFilePathFromUri.fileIsExists(path);
                if (!isLocal) path = message.getFileElem().getSourceUrl();
                v.downloadView1.setRes(path);
                v.quoteContent1.setText(tips);
                Message finalMessage1 = message;
                v.quoteLy1.setOnClickListener(v1 -> {
                    GetFilePathFromUri.openFile(itemView.getContext(), finalMessage1);
                });
            } else if (contentType == MessageType.CARD
                && null != message.getCardElem()) {
                String tips =
                    message.getSenderNickname() + ":" + IMUtil.getMsgParse(message) + message.getCardElem().getNickname();
                v.quoteContent1.setText(tips);
                String uid = message.getCardElem().getUserID();
                v.quoteLy1.setOnClickListener(v1 -> {
                    ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                        .withString(Constants.K_ID, uid).navigation(v.quoteContent1.getContext());
                });
            } else if (contentType == MessageType.TEXT || contentType == MessageType.AT_TEXT) {
                v.quoteContent1.setText(message.getSenderNickname() + ":" + IMUtil.getMsgParse(message));
                v.picture1.setVisibility(View.GONE);
            } else {
                v.picture1.setVisibility(View.VISIBLE);
                v.playBtn1.setVisibility(View.GONE);
                if (contentType == MessageType.PICTURE) {
                    v.quoteContent1.setText(message.getSenderNickname() + ":");
                    Glide.with(BaseApp.inst()).load(message.getPictureElem().getSnapshotPicture().getUrl()).placeholder(new PlaceHolderDrawable(BaseApp.inst())).error(io.openim.android.ouicore.R.mipmap.ic_chat_photo).centerCrop().into(v.picture1);
                    toPreview(v.quoteLy1, message.getPictureElem().getSourcePicture().getUrl(),
                        message.getPictureElem().getSnapshotPicture().getUrl());
                } else if (contentType == MessageType.VIDEO) {
                    v.playBtn1.setVisibility(View.VISIBLE);
                    v.quoteContent1.setText(message.getSenderNickname() + ":");
                    Glide.with(BaseApp.inst()).load(message.getVideoElem().getSnapshotUrl()).placeholder(new PlaceHolderDrawable(BaseApp.inst())).error(io.openim.android.ouicore.R.mipmap.ic_chat_photo).centerInside().into(v.picture1);
                    previewVideo(v.quoteLy1, message);
                } else {
                    String content = BaseApp.inst().getString(io.openim.android.ouicore.R.string.unsupported_type);
                    v.quoteContent1.setText(message.getSenderNickname() + ":" + "[" + content + "]");
                    v.picture1.setVisibility(View.GONE);
                }
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgTxtRightBinding v = LayoutMsgTxtRightBinding.bind(itemView);
            v.quoteLy2.setVisibility(View.VISIBLE);
            v.sendState2.setSendState(message.getStatus());
            QuoteElem quoteElem = message.getQuoteElem();

            message = quoteElem.getQuoteMessage();
            int contentType = message.getContentType();
            if (contentType == MessageType.REVOKE_MESSAGE_NTF) {
                v.quoteContent2.setText(message.getSenderNickname() + ":" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.quote_delete_tips));
                v.picture2.setVisibility(View.GONE);
                v.playBtn2.setVisibility(View.GONE);
                return;
            }
            v.downloadView.setVisibility(View.GONE);
            if (contentType == MessageType.FILE) {
                v.downloadView.setVisibility(View.VISIBLE);
                String tips =
                    message.getSenderNickname() + ":" + message.getFileElem().getFileName();
                String path = message.getFileElem().getFilePath();
                boolean isLocal = GetFilePathFromUri.fileIsExists(path);
                if (!isLocal) path = message.getFileElem().getSourceUrl();
                v.downloadView.setRes(path);
                v.quoteContent2.setText(tips);
                Message finalMessage1 = message;
                v.quoteLy2.setOnClickListener(v1 -> {
                    GetFilePathFromUri.openFile(itemView.getContext(), finalMessage1);
                });
            } else if (contentType == MessageType.CARD
                && null != message.getCardElem()) {
                String tips =
                    message.getSenderNickname() + ":" + IMUtil.getMsgParse(message) + message.getCardElem().getNickname();
                v.quoteContent2.setText(tips);
                String uid = message.getCardElem().getUserID();
                v.quoteLy2.setOnClickListener(v1 -> {
                    ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                        .withString(Constants.K_ID, uid).navigation(v.quoteContent2.getContext());
                });
            } else if (contentType == MessageType.TEXT
                || contentType == MessageType.AT_TEXT) {
                v.quoteContent2.setText(message.getSenderNickname() + ":" + IMUtil.getMsgParse(message));
                v.picture2.setVisibility(View.GONE);
            } else {
                v.picture2.setVisibility(View.VISIBLE);
                v.playBtn2.setVisibility(View.GONE);
                if (contentType == MessageType.PICTURE) {
                    v.quoteContent2.setText(message.getSenderNickname() + ":" + IMUtil.getMsgParse(message));
                    Glide.with(BaseApp.inst()).load(message.getPictureElem().getSnapshotPicture().getUrl()).placeholder(new PlaceHolderDrawable(BaseApp.inst())).error(io.openim.android.ouicore.R.mipmap.ic_chat_photo).centerCrop().into(v.picture2);
                    toPreview(v.quoteLy2, message.getPictureElem().getSourcePicture().getUrl(),
                        message.getPictureElem().getSnapshotPicture().getUrl());
                } else if (contentType == MessageType.VIDEO) {
                    v.playBtn2.setVisibility(View.VISIBLE);
                    v.quoteContent2.setText(message.getSenderNickname() + ":" + IMUtil.getMsgParse(message));
                    Glide.with(BaseApp.inst()).load(message.getVideoElem().getSnapshotUrl()).placeholder(new PlaceHolderDrawable(BaseApp.inst())).error(io.openim.android.ouicore.R.mipmap.ic_chat_photo).centerInside().into(v.picture2);
                    previewVideo(v.quoteLy2, message);
                } else {
                    String content = BaseApp.inst().getString(io.openim.android.ouicore.R.string.unsupported_type);
                    v.quoteContent2.setText(message.getSenderNickname() + ":" + "[" + content + "]");
                    v.picture2.setVisibility(View.GONE);
                }
            }
        }

        private static void previewVideo(View itemView, Message message) {
            itemView.setOnClickListener(new OnDedrepClickListener() {
                @Override
                public void click(View v) {
                    PreviewMediaVM previewMediaVM = Easy.installVM(PreviewMediaVM.class);
                    PreviewMediaVM.MediaData mediaData =
                        new PreviewMediaVM.MediaData(message.getVideoElem().getVideoUrl());
                    mediaData.mediaUrl = message.getVideoElem().getVideoUrl();
                    mediaData.thumbnail = message.getVideoElem().getSnapshotUrl();
                    mediaData.isVideo = true;
                    previewMediaVM.preview(mediaData);
                    itemView.getContext().startActivity(new Intent(itemView.getContext(),
                        PreviewMediaActivity.class));
                }
            });
        }
    }

    public static class GroupAnnouncementView extends MessageViewHolder.MsgViewHolder {

        public GroupAnnouncementView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        protected int getLeftInflatedId() {
            return R.layout.layout_msg_group_announcement_left;
        }

        @Override
        protected int getRightInflatedId() {
            return R.layout.layout_msg_group_announcement_right;
        }

        @Override
        protected void bindLeft(View itemView, Message message) {
            LayoutMsgGroupAnnouncementLeftBinding v =
                LayoutMsgGroupAnnouncementLeftBinding.bind(itemView);
            MsgExpand msgExpand = (MsgExpand) message.getExt();
            v.detail.setText(msgExpand.notificationMsg.group.notification);

            v.content.setOnClickListener(new OnDedrepClickListener() {
                @Override
                public void click(View v) {
                    toDetail();
                }
            });
        }

        private void toDetail() {
            GroupVM groupVM = BaseApp.inst().getVMByCache(GroupVM.class);
            if (null == groupVM) {
                groupVM = new GroupVM();
                groupVM.groupId = chatVM.groupID;
                BaseApp.inst().putVM(groupVM);
            }
            groupVM.getGroupsInfo();
            groupVM.getMyMemberInfo();
            ARouter.getInstance().build(Routes.Group.GROUP_BULLETIN).navigation();
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgGroupAnnouncementRightBinding v =
                LayoutMsgGroupAnnouncementRightBinding.bind(itemView);
            MsgExpand msgExpand = (MsgExpand) message.getExt();
            v.detail2.setText(msgExpand.notificationMsg.group.notification);
            v.content2.setOnClickListener(new OnDedrepClickListener() {
                @Override
                public void click(View v) {
                    toDetail();
                }
            });
        }
    }
}
