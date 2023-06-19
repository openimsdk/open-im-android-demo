package io.openim.android.ouicalling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.livekit.android.events.EventListenable;
import io.livekit.android.events.ParticipantEvent;
import io.livekit.android.room.participant.LocalParticipant;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.participant.RemoteParticipant;
import io.livekit.android.room.track.LocalVideoTrack;
import io.livekit.android.room.track.Track;
import io.livekit.android.room.track.TrackPublication;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicalling.databinding.DialogGroupCallBinding;
import io.openim.android.ouicalling.databinding.ItemMemberRendererBinding;
import io.openim.android.ouicalling.vm.CallingVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.entity.ParticipantMeta;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.MediaPlayerUtil;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionImpl;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.flow.FlowCollector;

public class GroupCallDialog extends CallDialog {
    private DialogGroupCallBinding view;
    private RecyclerViewAdapter<UserInfo, ViewHol.ImageTxtViewHolder> memberAdapter;
    private RecyclerViewAdapter<Participant, RendererViewHole> viewRenderersAdapter;
    private boolean isJoin = false;
    private CoroutineScope scope = callingVM.callViewModel.buildScope();


    public GroupCallDialog(@NonNull Context context, CallingService callingService,
                           boolean isCallOut) {
        super(context, callingService, isCallOut);
    }

    @Override
    public void initRendererView() {
        view.memberRecyclerView.setLayoutManager(new GridLayoutManager(context,5));
        view.memberRecyclerView.setAdapter(memberAdapter = new RecyclerViewAdapter<UserInfo,
            ViewHol.ImageTxtViewHolder>(ViewHol.ImageTxtViewHolder.class) {

            @Override
            public void onBindView(@NonNull ViewHol.ImageTxtViewHolder holder, UserInfo data,
                                   int position) {
                holder.view.img.load(data.getFaceURL());
                holder.view.txt.setTextColor(Color.WHITE);
                holder.view.txt.setText(data.getNickname());
            }
        });

        view.viewRenderers.setLayoutManager(new GridLayoutManager(context, 2));
        view.viewRenderers.setAdapter(viewRenderersAdapter = new RecyclerViewAdapter<Participant,
            RendererViewHole>(RendererViewHole.class) {

            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void onBindView(@NonNull RendererViewHole holder, Participant data,
                                   int position) {
                Object speakerVideoViewTag = holder.view.remoteSpeakerVideoView.getTag();
                if (speakerVideoViewTag instanceof VideoTrack) {
                    ((VideoTrack) speakerVideoViewTag).removeRenderer(holder.view.remoteSpeakerVideoView);
                }
                try {
                    callingVM.initRemoteVideoRenderer(holder.view.remoteSpeakerVideoView);
                } catch (Exception ignored) {
                }
                ParticipantMeta participantMeta = GsonHel.fromJson(data.getMetadata(),
                    ParticipantMeta.class);
                String name = participantMeta.groupMemberInfo.getNickname();
                if (TextUtils.isEmpty(name)) name = participantMeta.userInfo.getNickname();
                holder.view.name.setText(name);
                holder.view.avatarRl.setVisibility(callingVM.isVideoCalls ? View.GONE :
                    View.VISIBLE);
                holder.view.avatar.load(participantMeta.userInfo.getFaceURL());

                callingVM.callViewModel.subscribe(data.getEvents().getEvents(), (v) -> {
                    ParticipantMeta participantMeta2 =
                        GsonHel.fromJson(v.getParticipant().getMetadata(), ParticipantMeta.class);
                    participantMeta.userInfo.getUserID().equals(participantMeta2);
                    holder.view.micOn.setImageResource(v.getParticipant().isMicrophoneEnabled() ?
                        R.mipmap.ic_mic_s_on : R.mipmap.ic_mic_s_off);
                    return null;
                });

                if (data instanceof LocalParticipant) {
                    if (callingVM.callViewModel.getCameraEnabled().getValue()) {
                        holder.view.remoteSpeakerVideoView.setVisibility(View.VISIBLE);
                        holder.view.avatarRl.setVisibility(View.GONE);
                        VideoTrack localVideoTrack = callingVM.callViewModel.getVideoTrack(data);
                        localVideoTrack.addRenderer(holder.view.remoteSpeakerVideoView);
                        holder.view.remoteSpeakerVideoView.setTag(localVideoTrack);
                    } else {
                        holder.view.avatarRl.setVisibility(View.VISIBLE);
                        holder.view.remoteSpeakerVideoView.setVisibility(View.GONE);
                    }
                } else {
                    callingVM.callViewModel.bindRemoteViewRenderer(holder.view.remoteSpeakerVideoView, data, new Continuation<Unit>() {
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(@NonNull Object o) {
                            L.e("");
                        }
                    });
                }
            }
        });
    }


    @Override
    public void setContentView(@NonNull View v) {
        view = DialogGroupCallBinding.inflate(getLayoutInflater());
        super.setContentView(view.getRoot());
    }

    @Override
    public void bindData(SignalingInfo signalingInfo) {
        super.signalingInfo = signalingInfo;
        callingVM.isGroup =
            signalingInfo.getInvitation().getSessionType() != ConversationType.SINGLE_CHAT;
        callingVM.setVideoCalls(Constant.MediaType.VIDEO.equals(signalingInfo.getInvitation().getMediaType()));

        if (callingVM.isCallOut) {
            view.ask.setVisibility(View.GONE);
            view.callingMenu.setVisibility(View.VISIBLE);
            view.headTips.setVisibility(View.GONE);
            callingVM.signalingInvite(signalingInfo);
        } else {
            view.ask.setVisibility(View.VISIBLE);
            view.headTips.setVisibility(View.VISIBLE);
            view.callingMenu.setVisibility(View.GONE);
        }
        bindUserInfo(signalingInfo);
        listener(signalingInfo);
    }

    @Override
    public void otherSideAccepted() {
        callingVM.isStartCall = true;
        callingVM.buildTimer();
        view.headTips.setVisibility(View.GONE);
        MediaPlayerUtil.INSTANCE.pause();
        MediaPlayerUtil.INSTANCE.release();
    }

    public final Observer<String> bindTime = s -> {
        if (TextUtils.isEmpty(s)) return;
        view.timeTv.setText(s);
    };

    @Override
    public void dismiss() {
        callingVM.callViewModel.scopeCancel(scope);
        callingVM.timeStr.removeObserver(bindTime);
        super.dismiss();
    }

    @Override
    public void playRingtone() {
        if (isJoin) return;
        if (callingVM.isCallOut) Common.wakeUp(context);
        else super.playRingtone();
    }

    public void joinToShow() {
        isJoin = true;
        signalingAccept(signalingInfo);
        show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void listener(SignalingInfo signalingInfo) {
        callingVM.timeStr.observeForever(bindTime);

        view.closeCamera.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean isEnabled = !isChecked;
            callingVM.callViewModel.setCameraEnabled(isEnabled);
            Common.UIHandler.postDelayed(() -> viewRenderersAdapter.notifyItemChanged(0), 100);
        });
        view.switchCamera.setOnClickListener(v -> {
            callingVM.callViewModel.flipCamera();
        });

        view.micIsOn.setOnClickListener(new OnDedrepClickListener(1000) {
            @Override
            public void click(View v) {
                view.micIsOn.setText(view.micIsOn.isChecked() ?
                    context.getString(io.openim.android.ouicore.R.string.microphone_on) :
                    context.getString(io.openim.android.ouicore.R.string.microphone_off));
                //关闭麦克风
                callingVM.callViewModel.setMicEnabled(view.micIsOn.isChecked());
            }
        });

        view.speakerIsOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            view.speakerIsOn.setText(isChecked ?
                context.getString(io.openim.android.ouicore.R.string.speaker_on) :
                context.getString(io.openim.android.ouicore.R.string.speaker_off));
            // 打开扬声器
            callingVM.audioManager.setSpeakerphoneOn(isChecked);
        });

        view.hangUp.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                callingVM.signalingHungUp(signalingInfo);
            }
        });
        view.reject.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                callingVM.signalingHungUp(signalingInfo);

            }
        });
        view.answer.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                signalingAccept(signalingInfo);
            }
        });
        callingVM.setOnParticipantsChangeListener(participants -> {
            viewRenderersAdapter.setItems(participants);
        });
        view.zoomOut.setOnClickListener(v -> {
            shrink(true);
        });
        view.shrink.setOnClickListener(v -> {
            shrink(false);
        });

        callingVM.callViewModel.subscribe(callingVM.callViewModel.getActiveSpeakersFlow(), (v) -> {
            if (!v.isEmpty()) {
                ParticipantMeta participantMeta = GsonHel.fromJson(v.get(0).getMetadata(),
                    ParticipantMeta.class);
                String name = participantMeta.groupMemberInfo.getNickname();
                if (TextUtils.isEmpty(name)) name = participantMeta.userInfo.getNickname();
                view.sTips.setText(String.format(context.getString(io.openim.android.ouicore.R.string.who_talk), name));
                view.sAvatar.load(participantMeta.userInfo.getFaceURL());
            }
            return null;
        }, scope);
    }

    @Override
    public void shrink(boolean isShrink) {
        view.home.setVisibility(isShrink ? View.GONE : View.VISIBLE);
        getWindow().setDimAmount(isShrink ? 0f : 1f);
        view.shrink.setVisibility(isShrink ? View.VISIBLE : View.GONE);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = isShrink ? ViewGroup.LayoutParams.WRAP_CONTENT :
            ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = isShrink ? ViewGroup.LayoutParams.WRAP_CONTENT :
            ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = isShrink ? (Gravity.TOP | Gravity.END) : Gravity.CENTER;
        getWindow().setAttributes(params);
    }

    private void signalingAccept(SignalingInfo signalingInfo) {
        callingVM.signalingAccept(signalingInfo, new OnBase() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(Object data) {
                changeView();
            }
        });
    }


    @Override
    public void changeView() {
        view.headTips.setVisibility(View.GONE);
        view.ask.setVisibility(View.GONE);
        view.callingMenu.setVisibility(View.VISIBLE);
        view.timeTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void bindUserInfo(SignalingInfo signalingInfo) {
        List<String> ids = new ArrayList<>();
        ids.add(signalingInfo.getInvitation().getInviterUserID());
        ids.addAll(signalingInfo.getInvitation().getInviteeUserIDList());
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                L.e(error + "-" + code);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;
                UserInfo userInfo = data.get(0);
                view.sTips.setText(String.format(context.getString(io.openim.android.ouicore.R.string.who_talk), userInfo.getNickname()));
                view.sAvatar.load(userInfo.getFaceURL());

                memberAdapter.setItems(data);
                UserInfo inviterUser = data.get(0);
                view.tips1.setText(inviterUser.getNickname() + (callingVM.isVideoCalls ?
                    context.getString(io.openim.android.ouicore.R.string.invite_video_call) :
                    context.getString(io.openim.android.ouicore.R.string.invite_audio_call)));
                view.tips2.setText(data.size() + "人" + context.getString(io.openim.android.ouicore.R.string.calling));
            }
        }, ids);
    }

    public static class RendererViewHole extends RecyclerView.ViewHolder {

        public ItemMemberRendererBinding view;

        public RendererViewHole(@NonNull View itemView) {
            super(ItemMemberRendererBinding.inflate(LayoutInflater.from(itemView.getContext())).getRoot());
            view = ItemMemberRendererBinding.bind(this.itemView);
        }
    }
}
