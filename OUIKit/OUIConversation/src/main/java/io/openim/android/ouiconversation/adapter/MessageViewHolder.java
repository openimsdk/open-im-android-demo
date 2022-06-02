package io.openim.android.ouiconversation.adapter;

import static io.openim.android.ouiconversation.adapter.MessageAdapter.OWN_ID;

import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.lqr.audio.AudioPlayManager;
import com.lqr.audio.IAudioPlayListener;

import java.io.File;

import io.openim.android.ouiconversation.R;

import io.openim.android.ouiconversation.databinding.LayoutLoadingSmallBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgAudioLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgAudioRightBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgBinding;


import io.openim.android.ouiconversation.databinding.LayoutMsgImgLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgImgRightBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgTxtLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgTxtRightBinding;
import io.openim.android.ouiconversation.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.ouicore.voice.listener.PlayerListener;
import io.openim.android.ouicore.voice.player.SMediaPlayer;
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

        return new TXTView(parent);
    }

    public abstract static class MsgViewHolder extends RecyclerView.ViewHolder {
        protected RecyclerView recyclerView;
        protected MessageAdapter messageAdapter;
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
            if (isOwn = message.getSendID().equals(OWN_ID)) {
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
        }

        @Override
        void bindRight(View itemView, Message message) {
            LayoutMsgTxtRightBinding v = LayoutMsgTxtRightBinding.bind(itemView);
            v.avatar2.load(message.getSenderFaceUrl());
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

            Glide.with(itemView.getContext())
                .load(url)
                .into(v.content2);

            v.sendState2.setSendState(message.getStatus());
        }

        @Override
        void bindRight(View itemView, Message message) {
            LayoutMsgImgRightBinding v = LayoutMsgImgRightBinding.bind(itemView);
            String url = message.getPictureElem().getSourcePath();

            Glide.with(itemView.getContext())
                .load(url)
                .into(v.content);

            v.sendState.setSendState(message.getStatus());
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
            view.sendState.setSendState(message.getStatus());
            view.duration.setText(message.getSoundElem().getDuration() + "``");
            view.content.setOnClickListener(v -> extracted(message, view.lottieView));
        }

        @Override
        void bindRight(View itemView, Message message) {
            final LayoutMsgAudioRightBinding view = LayoutMsgAudioRightBinding.bind(itemView);
            view.sendState2.setSendState(message.getStatus());
            view.duration2.setText(message.getSoundElem().getDuration() + "``");

            view.content2.setOnClickListener(v -> extracted(message, view.lottieView2));
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
            view.sendState.setSendState(message.getStatus());
            view.videoPlay.setVisibility(View.VISIBLE);
            Glide.with(itemView.getContext())
                .load(message.getVideoElem().getSnapshotPath())
                .into(view.content);
            view.contentGroup.setOnClickListener(v -> {

            });
        }

        @Override
        void bindLeft(View itemView, Message message) {
            LayoutMsgImgLeftBinding view = LayoutMsgImgLeftBinding.bind(itemView);
            view.sendState2.setSendState(message.getStatus());
            view.videoPlay2.setVisibility(View.VISIBLE);
            Glide.with(itemView.getContext())
                .load(message.getVideoElem().getSnapshotUrl())
                .into(view.content2);
            view.contentGroup2.setOnClickListener(v -> {

            });
        }
    }
}
