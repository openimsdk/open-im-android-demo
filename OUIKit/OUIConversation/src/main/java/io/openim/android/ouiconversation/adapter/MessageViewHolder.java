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
import io.openim.android.ouiconversation.databinding.LayoutMsgImgLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgImgRightBinding;
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
        if (viewType == MessageType.PICTURE) return new IMGView(parent);
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

        private void showTime(MsgExpand msgExpand) {
            TextView notice = itemView.findViewById(R.id.notice);
            if (msgExpand.isShowTime) {
                //显示时间
                String time = TimeUtil.getTimeString(message.getSendTime());
                notice.setVisibility(View.VISIBLE);
                notice.setText(time);
            } else notice.setVisibility(View.GONE);
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
            String content = message.getTextElem() == null ?
                BaseApp.inst().getString(io.openim.android.ouicore.R.string.unsupported_type) : message.getTextElem().getContent();
            v.content.setText(content);
        }

        @Override
        protected void bindRight(View itemView, Message message) {
            LayoutMsgTxtRightBinding v = LayoutMsgTxtRightBinding.bind(itemView);
            v.avatar2.load(message.getSenderFaceUrl(), message.getSenderNickname());
            v.sendState2.setSendState(message.getStatus());
            String content = message.getTextElem() == null ?
                BaseApp.inst().getString(io.openim.android.ouicore.R.string.unsupported_type) : message.getTextElem().getContent();
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
}
