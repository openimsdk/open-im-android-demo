package io.openim.android.ouimeeting;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import io.livekit.android.events.ParticipantEvent;
import io.livekit.android.renderer.TextureViewRenderer;
import io.livekit.android.room.participant.ConnectionQuality;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.participant.RemoteParticipant;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.entity.ParticipantMeta;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouimeeting.databinding.ActivityMeetingHomeBinding;
import io.openim.android.ouimeeting.databinding.LayoutUserStatusBinding;
import io.openim.android.ouimeeting.vm.MeetingVM;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.flow.SharedFlow;
import kotlinx.coroutines.flow.StateFlow;

public class MeetingHomeActivity extends BaseActivity<MeetingVM, ActivityMeetingHomeBinding> implements MeetingVM.Interaction {


    private RecyclerViewAdapter<RemoteParticipant, UserStreamViewHolder> adapter;
    private String centerSID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(MeetingVM.class);

        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMeetingHomeBinding.inflate(getLayoutInflater()));

        init();
        initView();
        listener();
    }

    private void listener() {
        view.expand.setOnClickListener(v -> {
            Object tag = v.getTag();
            if (null == tag) tag = false;
            boolean isExpand = (boolean) tag;
            v.setTag(!isExpand);
            if (isExpand) {
                v.setRotationX(0);
                view.memberRecyclerView.setVisibility(View.VISIBLE);
            } else {
                v.setRotationX(180);
                view.memberRecyclerView.setVisibility(View.GONE);
            }
        });
        vm.callViewModel.subscribe(vm.callViewModel.getRemoteParticipants(), (v) -> {
            Collections.reverse(v);
            adapter.setItems(v);
            return null;
        });

    }

    private void initView() {
        view.setCallViewModel(vm.callViewModel);
        vm.initVideoView(view.centerTextureView);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(RecyclerView.HORIZONTAL);
        view.memberRecyclerView.setLayoutManager(lm);

        view.memberRecyclerView.setAdapter(adapter = new RecyclerViewAdapter<RemoteParticipant,
            UserStreamViewHolder>(UserStreamViewHolder.class) {

            @Override
            public void onBindView(@NonNull UserStreamViewHolder holder, RemoteParticipant data,
                                   int position) {
                ParticipantMeta participantMeta = GsonHel.fromJson(data.getMetadata(),
                    ParticipantMeta.class);
                String name = participantMeta.groupMemberInfo.getNickname();
                if (TextUtils.isEmpty(name)) name = participantMeta.userInfo.getNickname();
                String faceURL = participantMeta.userInfo.getFaceURL();
                holder.view.name.setText(name);
                holder.view.avatar.load(faceURL);

                vm.initVideoView(holder.view.textureView);
                bindRemoteViewRenderer(holder.view.textureView, data);
                AtomicBoolean isOpenCamera = new AtomicBoolean(false);
                //这里更新item 的画面需要一直更新 我们传centerSID，这样相等就会更新
                userStatusViewBind(holder.view, data.getEvents().getEvents(), false, isOpen -> {
                    isOpenCamera.set(isOpen);
                    if (isOpen) {
                        holder.view.userStatus.setVisibility(View.GONE);
                        holder.view.textureView.setVisibility(View.VISIBLE);
                    } else {
                        holder.view.userStatus.setVisibility(View.VISIBLE);
                        holder.view.textureView.setVisibility(View.GONE);
                    }
                });
                final String finalName = name;
                holder.view.getRoot().setOnClickListener(v -> {
                    centerCov(isOpenCamera.get());
                    centerSID = data.getSid();
                    bindCenterUser(finalName, faceURL,data.getEvents().getEvents());

                    bindRemoteViewRenderer(view.centerTextureView, data);
                });
            }
        });
        initRecyclerViewHeaderView();
    }


    private void bindCenterUser(String nickname, String faceUrl,
                                SharedFlow<ParticipantEvent> flow) {
        view.centerUser.name.setText(nickname);
        view.centerUser.avatar.load(faceUrl);
        userStatusViewBind(view.centerUser, flow,true, this::centerCov);
    }

    /**
     * 屏幕中间直播画面切换
     *
     * @param isOpen
     */
    private void centerCov(Boolean isOpen) {
        if (isOpen) {
            view.centerTextureView.setVisibility(View.VISIBLE);
            view.centerUser.getRoot().setVisibility(View.GONE);
        } else {
            view.centerTextureView.setVisibility(View.GONE);
            view.centerUser.getRoot().setVisibility(View.VISIBLE);
        }
    }

    private void bindRemoteViewRenderer(TextureViewRenderer textureView, Participant data) {
        Object speakerVideoViewTag = textureView.getTag();
        if (speakerVideoViewTag instanceof VideoTrack) {
            ((VideoTrack) speakerVideoViewTag).removeRenderer(textureView);
        }
        vm.callViewModel.bindRemoteViewRenderer(textureView, data, new Continuation<Unit>() {
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

    private void initRecyclerViewHeaderView() {
        LayoutUserStatusBinding holder = LayoutUserStatusBinding.inflate(getLayoutInflater(),
            view.memberRecyclerView, false);
        LoginCertificate l = BaseApp.inst().loginCertificate;
        holder.name.setText(l.nickname);
        holder.avatar.load(l.faceURL);

        SharedFlow<ParticipantEvent> sharedFlow =
            vm.callViewModel.getRoom().getLocalParticipant().getEvents().getEvents();
        centerSID = vm.callViewModel.getRoom().getLocalParticipant().getSid();
        bindCenterUser(l.nickname, l.faceURL, sharedFlow);
        bindRemoteViewRenderer(view.centerTextureView,
            vm.callViewModel.getRoom().getLocalParticipant());

        userStatusViewBind(holder, sharedFlow, true, null);
        view.memberRecyclerView.addHeaderView(holder.getRoot());

        holder.getRoot().setOnClickListener(v -> {
            centerSID = vm.callViewModel.getRoom().getLocalParticipant().getSid();
            centerCov(vm.callViewModel.getRoom().getLocalParticipant().isCameraEnabled());
            bindCenterUser(l.nickname, l.faceURL, sharedFlow);
            bindRemoteViewRenderer(view.centerTextureView,
                vm.callViewModel.getRoom().getLocalParticipant());
        });
    }


    private void userStatusViewBind(LayoutUserStatusBinding holder,
                                    SharedFlow<ParticipantEvent> sharedFlow,
                                    boolean isCare,
                                    CameraOpenListener cameraOpenListener) {

        vm.callViewModel.subscribe(sharedFlow, (v) -> {
            L.e("--------"+v.getParticipant().getSid());
            L.e("----2----"+centerSID);
            if (isCare&&!v.getParticipant().getSid().equals(centerSID)) return null;

            holder.mic.setImageResource(v.getParticipant().isMicrophoneEnabled() ?
                io.openim.android.ouicore.R.mipmap.ic__mic_on :
                io.openim.android.ouicore.R.mipmap.ic__mic_off);

            if (null != cameraOpenListener)
                cameraOpenListener.onOpen(v.getParticipant().isCameraEnabled());

            StateFlow<ConnectionQuality> flow =
                vm.callViewModel.getConnectionFlow(v.getParticipant());
            vm.callViewModel.subscribe(flow, (quality) -> {
                switch (quality) {
                    case EXCELLENT:
                        holder.net.setImageResource(io.openim.android.ouicore.R.mipmap.ic_net_excellent);
                        break;
                    case GOOD:
                        holder.net.setImageResource(io.openim.android.ouicore.R.mipmap.ic_net_good);
                        break;
                    default:
                        holder.net.setImageResource(io.openim.android.ouicore.R.mipmap.ic_net_poor);
                }
                return null;
            });

            return null;
        });
    }

    void init() {
        vm.init();
        vm.connectRoom();
    }

    @Override
    protected void setLightStatus() {

    }


    @Override
    public void connectRoomSuccess(VideoTrack localVideoTrack) {
        localVideoTrack.addRenderer(view.centerTextureView);
        view.centerTextureView.setTag(localVideoTrack);
    }

    public static class UserStreamViewHolder extends RecyclerView.ViewHolder {
        public final LayoutUserStatusBinding view;

        public UserStreamViewHolder(@NonNull View itemView) {
            super(LayoutUserStatusBinding.inflate(LayoutInflater.from(itemView.getContext()),
                (ViewGroup) itemView, false).getRoot());
            view = LayoutUserStatusBinding.bind(this.itemView);
            ViewGroup.LayoutParams params = view.avatar.getLayoutParams();
            params.width = Common.dp2px(55);
            params.height = Common.dp2px(55);
            view.mc.setVisibility(View.GONE);
        }
    }

    interface CameraOpenListener {
        void onOpen(Boolean isOpen);
    }
}
