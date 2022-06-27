package io.openim.android.ouiconversation.adapter;

import static io.openim.android.ouiconversation.adapter.MessageAdapter.OWN_ID;

import android.content.Intent;
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
import android.widget.TextView;

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


import io.openim.android.ouiconversation.databinding.LayoutMsgFileLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgFileRightBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgImgLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgImgRightBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgLocation1Binding;
import io.openim.android.ouiconversation.databinding.LayoutMsgLocation2Binding;
import io.openim.android.ouiconversation.databinding.LayoutMsgNoticeBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgTxtLeftBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgTxtRightBinding;
import io.openim.android.ouiconversation.ui.PreviewActivity;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.entity.LocationInfo;
import io.openim.android.ouicore.utils.ByteUtil;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.ouicore.voice.listener.PlayerListener;
import io.openim.android.ouicore.voice.player.SMediaPlayer;
import io.openim.android.ouicore.widget.WebViewActivity;
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

        if (viewType >= Constant.MsgType.NOTICE)
            return new NoticeView(parent);

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

    //通知消息
    public static class NoticeView extends MessageViewHolder.MsgViewHolder {

        public NoticeView(ViewGroup itemView) {
            super(itemView);
        }

        @Override
        public void bindData(Message message, int position) {
            TextView textView = itemView.findViewById(R.id.notice);
            textView.setVisibility(View.VISIBLE);
            String tips = message.getNotificationElem().getDefaultTips();
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
            toPreview(v.content2, url);
        }

        @Override
        void bindRight(View itemView, Message message) {
            LayoutMsgImgRightBinding v = LayoutMsgImgRightBinding.bind(itemView);
            String url = message.getPictureElem().getSourcePicture().getUrl();
            if (messageAdapter.hasStorage) {
                String filePath = message.getPictureElem().getSourcePath();
                if (new File(filePath).exists())
                    url = filePath;
            }
            Glide.with(itemView.getContext())
                .load(url)
                .into(v.content);

            v.sendState.setSendState(message.getStatus());
            toPreview(v.content, url);
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

            String snapshotUrl = message.getVideoElem().getSnapshotUrl();
            if (messageAdapter.hasStorage || null == snapshotUrl) {
                String filePath = message.getVideoElem().getSnapshotPath();
                if (new File(filePath).exists())
                    snapshotUrl = filePath;
            }

            Glide.with(itemView.getContext())
                .load(snapshotUrl)
                .into(view.content);
            toPreview(view.contentGroup, message.getVideoElem().getVideoUrl(), snapshotUrl);
        }

        @Override
        void bindLeft(View itemView, Message message) {
            LayoutMsgImgLeftBinding view = LayoutMsgImgLeftBinding.bind(itemView);
            view.sendState2.setSendState(message.getStatus());
            view.videoPlay2.setVisibility(View.VISIBLE);
            Glide.with(itemView.getContext())
                .load(message.getVideoElem().getSnapshotUrl())
                .into(view.content2);
            toPreview(view.contentGroup2, message.getVideoElem().getVideoUrl(), message.getVideoElem().getSnapshotUrl());
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
        }

        @Override
        void bindRight(View itemView, Message message) {
            LayoutMsgFileRightBinding view = LayoutMsgFileRightBinding.bind(itemView);

            view.avatar2.load(message.getSenderFaceUrl());
            view.title2.setText(message.getFileElem().getFileName());
            Long size = message.getFileElem().getFileSize();
            view.size2.setText(ByteUtil.bytes2kb(size) + "");

            view.sendState2.setSendState(message.getStatus());
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
            try {
                LocationInfo locationInfo = (LocationInfo) message.getExt();
                view.title.setText(locationInfo.name);
                view.address.setText(locationInfo.addr);
                Glide.with(itemView.getContext())
                    .load(locationInfo.url)
                    .into(view.map);
            } catch (Exception e) {
            }
            view.sendState.setSendState(message.getStatus());
            view.content.setOnClickListener(v -> {

                v.getContext().startActivity(new Intent(v.getContext(), WebViewActivity.class)
                    .putExtra(WebViewActivity.LOAD_URL, "https://apis.map.qq.com/uri/v1/geocoder?coord=" +
                        message.getLocationElem().getLatitude() + "," + message.getLocationElem().getLongitude() + "&referer=" + WebViewActivity.mapAppKey));
            });
        }

        @Override
        void bindRight(View itemView, Message message) {
            LayoutMsgLocation2Binding view = LayoutMsgLocation2Binding.bind(itemView);
            try {
                LocationInfo locationInfo = (LocationInfo) message.getExt();
                view.title2.setText(locationInfo.name);
                view.address2.setText(locationInfo.addr);
                Glide.with(itemView.getContext())
                    .load(locationInfo.url)
                    .into(view.map2);
            } catch (Exception e) {
            }
            view.sendState2.setSendState(message.getStatus());
            view.content2.setOnClickListener(v -> {
                v.getContext().startActivity(new Intent(v.getContext(), WebViewActivity.class)
                    .putExtra(WebViewActivity.LOAD_URL, "https://apis.map.qq.com/uri/v1/geocoder?coord=" +
                        message.getLocationElem().getLatitude() + "," + message.getLocationElem().getLongitude() + "&referer=" + WebViewActivity.mapAppKey));
            });
        }
    }

}
