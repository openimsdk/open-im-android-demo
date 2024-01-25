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
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.livekit.android.events.EventListenable;
import io.livekit.android.events.ParticipantEvent;
import io.livekit.android.events.RoomEvent;
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
    private final CoroutineScope scope = callingVM.callViewModel.buildScope();


    public GroupCallDialog(@NonNull Context context, CallingService callingService,
                           boolean isCallOut) {
        super(context, callingService, isCallOut);

        callingVM.callViewModel.subscribe(callingVM.callViewModel.getRoom().getEvents().getEvents(), (v) -> {
            if (v instanceof RoomEvent.ParticipantDisconnected && v.getRoom().getRemoteParticipants().size() == 0) {
                //当只有1个人时关闭会议
                dismiss();
            }
            return null;
        }, scope);
    }

    @Override
    public void initRendererView() {
        view.memberRecyclerView.setLayoutManager(new GridLayoutManager(context, 5));
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
                } catch (Exception ignored) {}
                try {
                    ParticipantMeta participantMeta = GsonHel.fromJson(data.getMetadata(),
                        ParticipantMeta.class);
                    String name = participantMeta.groupMemberInfo.getNickname();
                    if (TextUtils.isEmpty(name)) name = participantMeta.userInfo.getNickname();
                    holder.view.name.setText(name);
                    holder.view.avatar.load(participantMeta.userInfo.getFaceURL(), name);


                    CoroutineScope coroutineScope = (CoroutineScope) holder.view.getRoot().getTag();
                    if (null != coroutineScope) {
                        callingVM.callViewModel.scopeCancel(coroutineScope);
                    }
                    coroutineScope = callingVM.callViewModel.buildScope();
                    holder.view.getRoot().setTag(coroutineScope);
                    callingVM.callViewModel.subscribe(data.getEvents().getEvents(), (v) -> {
                        holder.view.micOn.setImageResource(v.getParticipant().isMicrophoneEnabled() ? R.mipmap.ic_mic_s_on : R.mipmap.ic_mic_s_off);

                        boolean isCameraEnabled = v.getParticipant().isCameraEnabled();
                        L.e(v.getParticipant().getIdentity()+"---isCameraEnabled--"+isCameraEnabled);
                        showRemoteSpeakerVideoView(holder, isCameraEnabled);
                        return null;
                    }, coroutineScope);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!callingVM.isVideoCalls) return;
                boolean isCameraEnabled =
                    Boolean.TRUE.equals(callingVM.callViewModel
                        .getCameraEnabled().getValue());
                showRemoteSpeakerVideoView(holder, isCameraEnabled);
                if (data instanceof LocalParticipant) {
                    VideoTrack localVideoTrack = callingVM.callViewModel.getVideoTrack(data);
                    if (null!=localVideoTrack){
                        callingVM.callViewModel.bindVideoTrack(holder.
                            view.remoteSpeakerVideoView,localVideoTrack);
                    }
                    return;
                }
                callingVM.callViewModel.bindRemoteViewRenderer(holder.view.remoteSpeakerVideoView
                    , data, scope, new Continuation<Unit>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }
                    @Override
                    public void resumeWith(@NonNull Object o) {
                    }
                });

            }

            @Override
            public void onViewDetachedFromWindow(@NonNull RendererViewHole holder) {
                holder.view.remoteSpeakerVideoView.release();
                super.onViewDetachedFromWindow(holder);
            }
        });
    }

    private static void showRemoteSpeakerVideoView(@NonNull RendererViewHole holder,
                                                   boolean isShow) {
        holder.view.remoteSpeakerVideoView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        holder.view.avatarRl.setVisibility(isShow ? View.GONE : View.VISIBLE);
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
        view.cameraControl.setVisibility(callingVM.isVideoCalls ? View.VISIBLE : View.GONE);
        if (callingVM.isCallOut) {
            view.ask.setVisibility(View.GONE);
            view.callingMenu.setVisibility(View.VISIBLE);
            view.headTips.setVisibility(View.GONE);
            callingVM.signalingInvite(signalingInfo);
            timing();
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
    public final Observer<Boolean> cameraEnabled = isChecked -> {
        view.closeCamera.setChecked(!isChecked);
        view.closeCamera.setOnClickListener(v -> {
            boolean isEnabled = !((CheckBox)v).isChecked();
            callingVM.callViewModel.setCameraEnabled(isEnabled);
            Common.UIHandler.postDelayed(() ->
                viewRenderersAdapter.notifyItemChanged(0), 100);
        });
    };

    @Override
    public void dismiss() {
        callingVM.callViewModel.scopeCancel(scope);
        callingVM.timeStr.removeObserver(bindTime);
        callingVM.callViewModel.getCameraEnabled().removeObserver(cameraEnabled);
        super.dismiss();
    }

    @Override
    public void playRingtone() {
        if (callingVM.isCallOut || isJoin) Common.wakeUp(context);
        else super.playRingtone();
    }

    public void joinToShow() {
        isJoin = true;
        answerClick(signalingInfo);
        show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void listener(SignalingInfo signalingInfo) {
        callingVM.timeStr.observeForever(bindTime);
        callingVM.callViewModel.getCameraEnabled().observeForever(cameraEnabled);
        view.switchCamera.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                callingVM.callViewModel.flipCamera();
            }
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
                answerClick(signalingInfo);
            }
        });
        callingVM.setOnParticipantsChangeListener(participants -> {
            removeHost(participants);
            viewRenderersAdapter.setItems(participants);
        });
        view.zoomOut.setOnClickListener(v -> {
            zoomOutClick();
        });

        callingVM.callViewModel.subscribe(callingVM.callViewModel.getActiveSpeakersFlow(), (v) -> {
            if (!v.isEmpty()) {
                ParticipantMeta participantMeta = GsonHel.fromJson(v.get(0).getMetadata(),
                    ParticipantMeta.class);
//                String name = participantMeta.groupMemberInfo.getNickname();
//                if (TextUtils.isEmpty(name)) name = participantMeta.userInfo.getNickname();
//                view.sTips.setText(String.format(context.getString(io.openim.android.ouicore.R
//                .string.who_talk), name));
//                view.sAvatar.load(participantMeta.userInfo.getFaceURL());
                floatViewBinding.sTips.setText(context.getString(io.openim.android.ouicore.R.string.meeting));
                floatViewBinding.sAvatar.load(participantMeta.userInfo.getFaceURL(), true);
            }
            return null;
        }, scope);
    }

    private void removeHost(List<Participant> participants) {
        try {
            Iterator<Participant> iterator = participants.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getIdentity().equals(signalingInfo.getInvitation().getGroupID()))
                    iterator.remove();
            }
        } catch (Exception ignored) {
        }
    }

    public void shrink(boolean isShrink) {
        showFloatView();
        view.home.setVisibility(isShrink ? View.GONE : View.VISIBLE);
        getWindow().setDimAmount(isShrink ? 0f : 1f);
        if (isShrink) {
            showFloatView();
        } else if (null != easyWindow) {
            easyWindow.cancel();
        }

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = isShrink ? ViewGroup.LayoutParams.WRAP_CONTENT :
            ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = isShrink ? ViewGroup.LayoutParams.WRAP_CONTENT :
            ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = isShrink ? (Gravity.TOP | Gravity.END) : Gravity.CENTER;
        getWindow().setAttributes(params);
    }


    public void signalingAccept(SignalingInfo signalingInfo) {

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
        timing();

        if (callingVM.isVideoCalls) view.cameraControl.setVisibility(View.VISIBLE);
    }

    private void timing() {
        view.timeTv.setVisibility(View.VISIBLE);
        callingVM.buildTimer();
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
                floatViewBinding.sTips.setText(context.getString(io.openim.android.ouicore.R.string.meeting));
                floatViewBinding.sAvatar.load(userInfo.getFaceURL(), true);
                view.avatar.load(userInfo.getFaceURL());

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
