package io.openim.android.ouiconversation.adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import io.openim.android.ouiconversation.databinding.LayoutMsgTxtLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgTxtRightBinding;
import io.openim.android.ouiconversation.ui.PreviewActivity;
import io.openim.android.ouicore.utils.EmojiUtil;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouiconversation.widget.InputExpandFragment;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.AtUsersInfo;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.ByteUtil;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.MediaFileUtil;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.ouicore.voice.listener.PlayerListener;
import io.openim.android.ouicore.voice.player.SMediaPlayer;
import io.openim.android.ouicore.widget.AvatarImage;
import io.openim.android.ouicore.widget.WebViewActivity;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.MergeElem;
import io.openim.android.sdk.models.Message;

public class MessageViewHolder {
    public static RecyclerView.ViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Constant.LOADING)
            return new LoadingView(parent);

        if (viewType == Constant.MsgType.TXT)
            return new TXTView(parent);

        if (viewType == Constant.MsgType.PICTURE)
            return new IMGView(parent);

        if (viewType == Constant.MsgType.VOICE)
            return new AudioView(parent);

        if (viewType == Constant.MsgType.VIDEO)
            return new VideoView(parent);

        if (viewType == Constant.MsgType.FILE)
            return new FileView(parent);

        if (viewType == Constant.MsgType.LOCATION)
            return new LocationView(parent);

        if (viewType >= Constant.MsgType.NOTICE || viewType == Constant.MsgType.REVOKE)
            return new NoticeView(parent);

        if (viewType == Constant.MsgType.MERGE)
            return new MergeView(parent);

        if (viewType == Constant.MsgType.TRANSIT)
            return new BusinessCardView(parent);

        return new TXTView(parent);
    }

    public abstract static class MsgViewHolder extends RecyclerView.ViewHolder {
        protected RecyclerView recyclerView;
        protected MessageAdapter messageAdapter;

        private PopupWindow popupWindow;
        private Message message;
        private RecyclerViewAdapter adapter;
        private ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);

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
            return LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_msg, parent, false);
        }

        abstract int getLeftInflatedId();

        abstract int getRightInflatedId();

        abstract void bindLeft(View itemView, Message message);

        abstract void bindRight(View itemView, Message message);

        /**
         * 是否是自己发的消息
         */
        protected boolean isOwn = false;

        //绑定数据
        public void bindData(Message message, int position) {
            this.message = message;
            try {
                if (isOwn = message.getSendID().equals(BaseApp.inst().loginCertificate.userID)) {
                    if (leftIsInflated)
                        left.setVisibility(View.GONE);
                    if (rightIsInflated)
                        right.setVisibility(View.VISIBLE);
                    if (!rightIsInflated) {
                        right.setLayoutResource(getRightInflatedId());
                        right.inflate();
                    }
                    bindRight(itemView, message);
                } else {
                    if (leftIsInflated)
                        left.setVisibility(View.VISIBLE);
                    if (rightIsInflated)
                        right.setVisibility(View.GONE);
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

        /**
         * 统一处理
         */
        private void unite() {
            MsgExpand msgExpand = (MsgExpand) message.getExt();
            TextView notice = itemView.findViewById(R.id.notice);
            if (msgExpand.isShowTime) {
                //显示时间
                String time = TimeUtil.getTimeString(message.getSendTime());
                notice.setVisibility(View.VISIBLE);
                notice.setText(time);
            } else
                notice.setVisibility(View.GONE);


            AvatarImage avatarImage = itemView.findViewById(R.id.avatar);
            if (null != avatarImage)
                avatarImage.setOnLongClickListener(v -> {
                    if (chatVM.isSingleChat) return false;
                    List<Message> atMessages = chatVM.atMessages.getValue();
                    for (Message atMessage : atMessages) {
                        if (atMessage.getSendID().equals(message.getSendID())) return false;
                    }
                    atMessages.add(message);
                    chatVM.atMessages.setValue(atMessages);
                    return false;
                });

            CheckBox checkBox = itemView.findViewById(R.id.choose);
            if (chatVM.enableMultipleSelect.getValue()
                && message.getContentType() != Constant.MsgType.NOTICE) {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setChecked(msgExpand.isChoice);
                checkBox.setOnClickListener((buttonView) -> {
                    msgExpand.isChoice = checkBox.isChecked();
                });
            } else {
                checkBox.setVisibility(View.GONE);
            }
            ((LinearLayout.LayoutParams) checkBox.getLayoutParams()).topMargin = msgExpand.isShowTime ? Common.dp2px(15) : 0;

        }

        /***
         * 长按显示扩展菜单
         * @param view
         */
        protected void showMsgExMenu(View view) {
            view.setOnLongClickListener(v -> {
                List<Integer> menuIcons = new ArrayList<>();
                List<String> menuTitles = new ArrayList<>();

                if (null == popupWindow) {
                    popupWindow = new PopupWindow(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    LayoutMsgExMenuBinding view1 = LayoutMsgExMenuBinding.inflate(LayoutInflater.from(itemView.getContext()));
                    popupWindow.setContentView(view1.getRoot());
                    popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
                    popupWindow.setOutsideTouchable(true);
                    adapter = new RecyclerViewAdapter<Object, InputExpandFragment.ExpandHolder>(InputExpandFragment.ExpandHolder.class) {

                        @Override
                        public void onBindView(@NonNull InputExpandFragment.ExpandHolder holder, Object data, int position) {
                            int iconRes = menuIcons.get(position);
                            holder.v.menu.setCompoundDrawablesRelativeWithIntrinsicBounds(null, v.getContext().getDrawable(iconRes), null, null);
                            holder.v.menu.setText(menuTitles.get(position));
                            holder.v.menu.setTextColor(Color.WHITE);
                            holder.v.menu.setOnClickListener(v1 -> {
                                popupWindow.dismiss();
                                if (iconRes == R.mipmap.ic_withdraw) {
                                    chatVM.revokeMessage(message);
                                }
                                if (iconRes == R.mipmap.ic_delete) {
                                    chatVM.deleteMessageFromLocalStorage(message);
                                }
                                if (iconRes == R.mipmap.ic_forward) {
                                    Message forwardMessage = OpenIMClient.getInstance().messageManager.createForwardMessage(message);
                                    chatVM.forwardMsg = forwardMessage;
                                    ARouter.getInstance().build(Routes.Contact.FORWARD)
                                        .navigation((Activity) view.getContext(), Constant.Event.FORWARD);
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
                menuIcons.add(R.mipmap.ic_delete);
                menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.delete));
                if (message.getSendID().equals(BaseApp.inst().loginCertificate.userID)) {
                    //5分钟内可以撤回
                    if (System.currentTimeMillis() - message.getSendTime() < (1000 * 60 * 5)) {
                        menuIcons.add(R.mipmap.ic_withdraw);
                        menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.withdraw));
                    }
                }
                menuIcons.add(R.mipmap.ic_forward);
                menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.forward));
                menuIcons.add(R.mipmap.ic_multiple_choice);
                menuTitles.add(v.getContext().getString(io.openim.android.ouicore.R.string.multiple_choice));

                LayoutMsgExMenuBinding.bind(popupWindow.getContentView())
                    .recyclerview.setLayoutManager(new GridLayoutManager(view.getContext(),
                    menuIcons.size() < 4 ? menuIcons.size() : 4));
                adapter.setItems(menuIcons);

                int yDelay = Common.dp2px(5);
                popupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                popupWindow.showAsDropDown(v, -(popupWindow.getContentView().getMeasuredWidth() - v.getMeasuredWidth()) / 2, -(popupWindow.getContentView().getMeasuredHeight() + v.getMeasuredHeight() + yDelay));
                return true;
            });
        }

        /**
         * 处理at、emoji
         *
         * @param showView
         * @return
         */
        protected boolean handleSequence(TextView showView) {
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

        @SuppressLint("SetTextI18n")
        @Override
        public void bindData(Message message, int position) {
            TextView textView = itemView.findViewById(R.id.notice);
            textView.setVisibility(View.VISIBLE);
            String tips = message.getNotificationElem().getDefaultTips();
            if (message.getContentType() == Constant.MsgType.REVOKE)
                textView.setText(message.getSenderNickname() + textView.getContext().getString(io.openim.android.ouicore.R.string.revoke_tips));
            else
                textView.setText(tips);
        }

        @Override
        int getLeftInflatedId() {
            return 0;
        }

        @Override
        int getRightInflatedId() {
            return 0;
        }

        @Override
        void bindLeft(View itemView, Message message) {

        }

        @Override
        void bindRight(View itemView, Message message) {

        }
    }

    //文本消息
    public static class TXTView extends MessageViewHolder.MsgViewHolder {

        public TXTView(ViewGroup parent) {
            super(parent);
        }

        @Override
        int getLeftInflatedId() {
            return R.layout.layout_msg_txt_left;
        }

        @Override
        int getRightInflatedId() {
            return R.layout.layout_msg_txt_right;
        }

        @Override
        void bindLeft(View itemView, Message message) {
            LayoutMsgTxtLeftBinding v = LayoutMsgTxtLeftBinding.bind(itemView);
            v.avatar.load(message.getSenderFaceUrl());
            v.content.setText(message.getContent());

            if (message.getSessionType() == io.openim.android.ouicore.utils.Constant.SessionType.GROUP_CHAT) {
                v.nickName.setVisibility(View.VISIBLE);
                v.nickName.setText(message.getSenderNickname());
            } else
                v.nickName.setVisibility(View.GONE);

            showMsgExMenu(v.content);

            if (!handleSequence(v.content))
                v.content.setText(message.getContent());

        }

        @Override
        void bindRight(View itemView, Message message) {
            LayoutMsgTxtRightBinding v = LayoutMsgTxtRightBinding.bind(itemView);
            v.avatar2.load(message.getSenderFaceUrl());
            showMsgExMenu(v.content2);

            if (!handleSequence(v.content2))
                v.content2.setText(message.getContent());

        }

    }

    public static class IMGView extends MessageViewHolder.MsgViewHolder {

        public IMGView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        int getLeftInflatedId() {
            return R.layout.layout_msg_img_left;
        }

        @Override
        int getRightInflatedId() {
            return R.layout.layout_msg_img_right;
        }

        @Override
        void bindLeft(View itemView, Message message) {
            LayoutMsgImgLeftBinding v = LayoutMsgImgLeftBinding.bind(itemView);
            String url = message.getPictureElem().getSourcePicture().getUrl();

            v.nickName.setVisibility(View.VISIBLE);
            v.nickName.setText(message.getSenderNickname());
            v.avatar.load(message.getSenderFaceUrl());
            Glide.with(itemView.getContext())
                .load(url)
                .into(v.content);

            v.sendState.setSendState(message.getStatus());
            toPreview(v.content, url);
            showMsgExMenu(v.content);
        }

        @Override
        void bindRight(View itemView, Message message) {
            LayoutMsgImgRightBinding v = LayoutMsgImgRightBinding.bind(itemView);
            v.avatar2.load(message.getSenderFaceUrl());
            String url = message.getPictureElem().getSourcePicture().getUrl();
            if (messageAdapter.hasStorage) {
                String filePath = message.getPictureElem().getSourcePath();
                if (new File(filePath).exists())
                    url = filePath;
            }
            Glide.with(itemView.getContext())
                .load(url)
                .into(v.content2);

            v.sendState2.setSendState(message.getStatus());
            toPreview(v.content2, url);
            showMsgExMenu(v.content2);
        }

        void toPreview(View view, String url) {
            view.setOnClickListener(v -> view.getContext().startActivity(
                new Intent(view.getContext(), PreviewActivity.class).putExtra(PreviewActivity.MEDIA_URL, url)));
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
                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int firstVisiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                        int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
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
        int getLeftInflatedId() {
            return R.layout.layout_msg_audio_left;
        }

        @Override
        int getRightInflatedId() {
            return R.layout.layout_msg_audio_right;
        }

        @Override
        void bindLeft(View itemView, Message message) {
            final LayoutMsgAudioLeftBinding view = LayoutMsgAudioLeftBinding.bind(itemView);
            view.avatar.load(message.getSenderFaceUrl());
            view.sendState.setSendState(message.getStatus());
            view.duration.setText(message.getSoundElem().getDuration() + "``");
            view.content.setOnClickListener(v -> extracted(message, view.lottieView));
            showMsgExMenu(view.content);
        }

        @Override
        void bindRight(View itemView, Message message) {
            final LayoutMsgAudioRightBinding view = LayoutMsgAudioRightBinding.bind(itemView);
            view.avatar2.load(message.getSenderFaceUrl());
            view.sendState2.setSendState(message.getStatus());
            view.duration2.setText(message.getSoundElem().getDuration() + "``");

            view.content2.setOnClickListener(v -> extracted(message, view.lottieView2));
            showMsgExMenu(view.content2);
        }

        private void extracted(Message message, LottieAnimationView lottieView) {
            SPlayer.instance().getMediaPlayer();
            SPlayer.instance().stop();
            SPlayer.instance().playByUrl(message.getSoundElem().getSourceUrl(), new PlayerListener() {
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
        void bindRight(View itemView, Message message) {
            LayoutMsgImgRightBinding view = LayoutMsgImgRightBinding.bind(itemView);
            view.avatar2.load(message.getSenderFaceUrl());
            view.sendState2.setSendState(message.getStatus());
            view.videoPlay2.setVisibility(View.VISIBLE);

            String snapshotUrl = message.getVideoElem().getSnapshotUrl();
            if (messageAdapter.hasStorage || null == snapshotUrl) {
                String filePath = message.getVideoElem().getSnapshotPath();
                if (new File(filePath).exists())
                    snapshotUrl = filePath;
            }

            Glide.with(itemView.getContext())
                .load(snapshotUrl)
                .into(view.content2);
            toPreview(view.videoPlay2, message.getVideoElem().getVideoUrl(), snapshotUrl);
            showMsgExMenu(view.content2);
        }

        @Override
        void bindLeft(View itemView, Message message) {
            LayoutMsgImgLeftBinding view = LayoutMsgImgLeftBinding.bind(itemView);
            view.avatar.load(message.getSenderFaceUrl());
            view.sendState.setSendState(message.getStatus());
            view.videoPlay.setVisibility(View.VISIBLE);
            Glide.with(itemView.getContext())
                .load(message.getVideoElem().getSnapshotUrl())
                .into(view.content);
            toPreview(view.videoPlay, message.getVideoElem().getVideoUrl(), message.getVideoElem().getSnapshotUrl());
            showMsgExMenu(view.content);
        }

        void toPreview(View view, String url, String firstFrameUrl) {
            view.setOnClickListener(v -> view.getContext().startActivity(
                new Intent(view.getContext(), PreviewActivity.class)
                    .putExtra(PreviewActivity.MEDIA_URL, url)
                    .putExtra(PreviewActivity.FIRST_FRAME, firstFrameUrl)));
        }
    }


    public static class FileView extends MessageViewHolder.MsgViewHolder {

        public FileView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        int getLeftInflatedId() {
            return R.layout.layout_msg_file_left;
        }

        @Override
        int getRightInflatedId() {
            return R.layout.layout_msg_file_right;
        }

        @Override
        void bindLeft(View itemView, Message message) {
            LayoutMsgFileLeftBinding view = LayoutMsgFileLeftBinding.bind(itemView);
            view.avatar.load(message.getSenderFaceUrl());

            view.title.setText(message.getFileElem().getFileName());
            Long size = message.getFileElem().getFileSize();
            view.size.setText(ByteUtil.bytes2kb(size) + "");

            view.sendState.setSendState(message.getStatus());
            showMsgExMenu(view.content);

            view.content.setOnClickListener(v -> {
                GetFilePathFromUri.openFile(v.getContext(), message);
            });
        }

        @Override
        void bindRight(View itemView, Message message) {
            LayoutMsgFileRightBinding view = LayoutMsgFileRightBinding.bind(itemView);
            view.avatar2.load(message.getSenderFaceUrl());

            view.title2.setText(message.getFileElem().getFileName());
            Long size = message.getFileElem().getFileSize();
            view.size2.setText(ByteUtil.bytes2kb(size) + "");

            view.sendState2.setSendState(message.getStatus());
            showMsgExMenu(view.content2);

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
        int getLeftInflatedId() {
            return R.layout.layout_msg_location1;
        }

        @Override
        int getRightInflatedId() {
            return R.layout.layout_msg_location2;
        }

        @Override
        void bindLeft(View itemView, Message message) {
            LayoutMsgLocation1Binding view = LayoutMsgLocation1Binding.bind(itemView);
            view.avatar.load(message.getSenderFaceUrl());
            try {
                MsgExpand msgExpand = (MsgExpand) message.getExt();
                view.title.setText(msgExpand.locationInfo.name);
                view.address.setText(msgExpand.locationInfo.addr);
                Glide.with(itemView.getContext())
                    .load(msgExpand.locationInfo.url)
                    .into(view.map);
            } catch (Exception e) {
            }
            view.sendState.setSendState(message.getStatus());
            view.content.setOnClickListener(v -> {

                v.getContext().startActivity(new Intent(v.getContext(), WebViewActivity.class)
                    .putExtra(WebViewActivity.LOAD_URL, "https://apis.map.qq.com/uri/v1/geocoder?coord=" +
                        message.getLocationElem().getLatitude() + "," + message.getLocationElem().getLongitude() + "&referer=" + WebViewActivity.mapAppKey));
            });
            showMsgExMenu(view.content);
        }

        @Override
        void bindRight(View itemView, Message message) {
            LayoutMsgLocation2Binding view = LayoutMsgLocation2Binding.bind(itemView);
            view.avatar2.load(message.getSenderFaceUrl());
            try {
                MsgExpand msgExpand = (MsgExpand) message.getExt();
                view.title2.setText(msgExpand.locationInfo.name);
                view.address2.setText(msgExpand.locationInfo.addr);
                Glide.with(itemView.getContext())
                    .load(msgExpand.locationInfo.url)
                    .into(view.map2);
            } catch (Exception e) {
            }
            view.sendState2.setSendState(message.getStatus());
            view.content2.setOnClickListener(v -> {
                v.getContext().startActivity(new Intent(v.getContext(), WebViewActivity.class)
                    .putExtra(WebViewActivity.LOAD_URL, "https://apis.map.qq.com/uri/v1/geocoder?coord=" +
                        message.getLocationElem().getLatitude() + "," + message.getLocationElem().getLongitude() + "&referer=" + WebViewActivity.mapAppKey));
            });
            showMsgExMenu(view.content2);
        }
    }

    public static class MergeView extends MessageViewHolder.MsgViewHolder {

        public MergeView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        int getLeftInflatedId() {
            return R.layout.layout_msg_merge_left;
        }

        @Override
        int getRightInflatedId() {
            return R.layout.layout_msg_merge_right;
        }


        @Override
        void bindLeft(View itemView, Message message) {
            LayoutMsgMergeLeftBinding view = LayoutMsgMergeLeftBinding.bind(itemView);
            view.sendState.setSendState(message.getStatus());
            MergeElem mergeElem = message.getMergeElem();
            view.content.setText(mergeElem.getTitle());
            try {
                view.history11.setText(mergeElem.getAbstractList().get(0));
                view.history12.setText(mergeElem.getAbstractList().get(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
            showMsgExMenu(view.contentLy);
            view.contentLy.setOnClickListener(clickJumpDetail);
        }

        @Override
        void bindRight(View itemView, Message message) {
            LayoutMsgMergeRightBinding view = LayoutMsgMergeRightBinding.bind(itemView);
            view.sendState2.setSendState(message.getStatus());
            MergeElem mergeElem = message.getMergeElem();
            view.content2.setText(mergeElem.getTitle());
            try {
                view.history21.setText(mergeElem.getAbstractList().get(0));
                view.history22.setText(mergeElem.getAbstractList().get(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
            showMsgExMenu(view.contentLy2);
            view.contentLy2.setOnClickListener(clickJumpDetail);
        }

        private View.OnClickListener clickJumpDetail = v -> {
//            v.getContext().startActivity(new Intent(v.getContext(), ChatHistoryDetailsActivity.class));
        };
    }


    public static class BusinessCardView extends MessageViewHolder.MsgViewHolder {

        public BusinessCardView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        int getLeftInflatedId() {
            return R.layout.layout_msg_card_left;
        }

        @Override
        int getRightInflatedId() {
            return R.layout.layout_msg_card_right;
        }

        @Override
        void bindLeft(View itemView, Message message) {
            LayoutMsgCardLeftBinding view = LayoutMsgCardLeftBinding.bind(itemView);
            view.sendState.setSendState(message.getStatus());
            view.avatar.load(message.getSenderFaceUrl());
            String friendInfo = message.getContent();
            FriendInfo friendInfoBean = GsonHel.fromJson(friendInfo, FriendInfo.class);
            view.nickName.setText(friendInfoBean.getNickname());
            view.otherAvatar.load(friendInfoBean.getFaceURL());
            jump(view.content, friendInfoBean.getUserID());
        }

        void jump(View view, String uid) {
            view.setOnClickListener(v -> ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                .withString(Constant.K_ID, uid)
                .withBoolean(Constant.K_RESULT, true).navigation(view.getContext()));
        }

        @Override
        void bindRight(View itemView, Message message) {
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

}
