package io.openim.android.ouicalling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicalling.databinding.DialogGroupCallBinding;
import io.openim.android.ouicalling.databinding.ItemMemberRendererBinding;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.databinding.ItemImgTxtBinding;
import io.openim.android.ouicore.utils.CallingService;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class GroupCallDialog extends CallDialog {
    private DialogGroupCallBinding view;
    private RecyclerViewAdapter<UserInfo, ViewHol.ImageTxtViewHolder> memberAdapter;
    private RecyclerViewAdapter<Participant, RendererViewHole> viewRenderersAdapter;

    public GroupCallDialog(@NonNull Context context, CallingService callingService) {
        super(context, callingService);
    }

    public GroupCallDialog(@NonNull Context context, CallingService callingService, boolean isCallOut) {
        super(context, callingService, isCallOut);
    }

    @Override
    public void initRendererView() {
        view.memberRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        view.memberRecyclerView.setAdapter(memberAdapter = new RecyclerViewAdapter<UserInfo,
            ViewHol.ImageTxtViewHolder>(ViewHol.ImageTxtViewHolder.class) {

            @Override
            public void onBindView(@NonNull ViewHol.ImageTxtViewHolder holder, UserInfo data, int position) {
                holder.view.img.load(data.getFaceURL());
                holder.view.txt.setText(data.getNickname());
            }
        });

        view.viewRenderers.setLayoutManager(new LinearLayoutManager(context));
        view.viewRenderers.setAdapter(viewRenderersAdapter = new RecyclerViewAdapter<Participant,
            RendererViewHole>(RendererViewHole.class) {

            @Override
            public void onBindView(@NonNull RendererViewHole holder, Participant data, int position) {
                Object speakerVideoViewTag = holder.view.remoteSpeakerVideoView.getTag();
                if (speakerVideoViewTag instanceof VideoTrack) {
                    ((VideoTrack) speakerVideoViewTag).removeRenderer(holder.view.remoteSpeakerVideoView);
                }
                callingVM.callViewModel.bindRemoteViewRenderer(holder.view.remoteSpeakerVideoView, data,
                    new Continuation<Unit>() {
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
        });
    }

    @Override
    public void setContentView(@NonNull View v) {
        view = DialogGroupCallBinding.inflate(getLayoutInflater());
        super.setContentView(view.getRoot());
    }

    @Override
    public void bindData(SignalingInfo signalingInfo) {
        callingVM.isGroup = signalingInfo.getInvitation().getInviteeUserIDList().size() > 1;
        callingVM.setVideoCalls("video".equals(signalingInfo.getInvitation()
            .getMediaType()));

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
    public void listener(SignalingInfo signalingInfo) {
        callingVM.timeStr.observeForever(bindTime);
        view.micIsOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            view.micIsOn.setText(isChecked ? context.getString(io.openim.android.ouicore.R.string.microphone_on)
                : context.getString(io.openim.android.ouicore.R.string.microphone_off));
            //关闭麦克风
            callingVM.callViewModel.setMicEnabled(isChecked);
        });
        view.speakerIsOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            view.speakerIsOn.setText(isChecked ? context.getString(io.openim.android.ouicore.R.string.speaker_on)
                : context.getString(io.openim.android.ouicore.R.string.speaker_off));
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
        });
    }

    @Override
    public void changeView() {

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
                if (!callingVM.isCallOut) {
                    UserInfo inviterUser = data.get(0);
                    view.tips1.setText(inviterUser.getNickname() + (callingVM.isVideoCalls ?
                        context.getString(io.openim.android.ouicore.R.string.invite_video_call) :
                        context.getString(io.openim.android.ouicore.R.string.invite_audio_call)));
                    view.tips2.setText(data.size() + "人" + context.getString(io.openim.android.ouicore.R.string.calling));
                }

            }
        }, ids);
    }

    private static class RendererViewHole extends RecyclerView.ViewHolder {

        public ItemMemberRendererBinding view;

        public RendererViewHole(@NonNull View itemView) {
            super(ItemMemberRendererBinding.inflate(LayoutInflater.from(itemView.getContext())).getRoot());
            view = ItemMemberRendererBinding.bind(this.itemView);
        }
    }
}
