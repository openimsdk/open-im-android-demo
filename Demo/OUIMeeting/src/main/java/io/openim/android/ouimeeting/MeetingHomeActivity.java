package io.openim.android.ouimeeting;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.livekit.android.renderer.TextureViewRenderer;
import io.livekit.android.room.participant.ConnectionQuality;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.ParticipantMeta;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouimeeting.databinding.ActivityMeetingHomeBinding;
import io.openim.android.ouimeeting.databinding.LayoutMemberDialogBinding;
import io.openim.android.ouimeeting.databinding.LayoutUserStatusBinding;
import io.openim.android.ouimeeting.databinding.MeetingIietmMemberBinding;
import io.openim.android.ouimeeting.vm.MeetingVM;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.flow.StateFlow;

public class MeetingHomeActivity extends BaseActivity<MeetingVM, ActivityMeetingHomeBinding> implements MeetingVM.Interaction {


    private RecyclerViewAdapter<Participant, UserStreamViewHolder> adapter;
    private BottomPopDialog bottomPopDialog;
    private RecyclerViewAdapter<Participant, MemberItemViewHolder> memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(MeetingVM.class);

        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMeetingHomeBinding.inflate(getLayoutInflater()));

        init();
        initView();
        listener();
    }

    //分享屏幕
    private ActivityResultLauncher<Intent> screenCaptureIntentLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            int resultCode = result.getResultCode();
            Intent data = result.getData();
            if (resultCode != Activity.RESULT_OK || data == null) {
                return;
            }
            vm.startShareScreen(data);
            toast(getString(io.openim.android.ouicore.R.string.share_screen));
        });


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
        view.mic.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                vm.callViewModel.setMicEnabled(view.mic.isChecked());
            }
        });
        view.camera.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                vm.callViewModel.setCameraEnabled(view.camera.isChecked());
            }
        });
        view.shareScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestMediaProjection();
            } else {
                vm.stopShareScreen();
            }
        });
        view.member.setOnClickListener(v -> {
            if (null == bottomPopDialog)
                bottomPopDialog = new BottomPopDialog(this, buildPopView());

            List<Participant> participants = new ArrayList<>();
            participants.addAll(adapter.getItems());
            memberAdapter.setItems(participants);
            bottomPopDialog.show();
        });

        vm.callViewModel.subscribe(vm.callViewModel.getRoom().getLocalParticipant().getEvents().getEvents(), (v) -> {
            boolean isMicrophoneEnabled = v.getParticipant().isMicrophoneEnabled();
            boolean isCameraEnabled = v.getParticipant().isCameraEnabled();
            view.mic.setChecked(isMicrophoneEnabled);
            view.mic.setText(isMicrophoneEnabled ?
                getString(io.openim.android.ouicore.R.string.mute) :
                getString(io.openim.android.ouicore.R.string.cancel_mute));
            view.camera.setChecked(isCameraEnabled);
            view.camera.setText(isCameraEnabled ?
                getString(io.openim.android.ouicore.R.string.close_camera) :
                getString(io.openim.android.ouicore.R.string.start_camera));
            return null;
        }, vm.callViewModel.buildScope());
    }

    //成员弹窗
    private View buildPopView() {
        LayoutMemberDialogBinding v = LayoutMemberDialogBinding.inflate(getLayoutInflater());
        v.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        v.recyclerView.setAdapter(memberAdapter = new RecyclerViewAdapter<Participant,
            MemberItemViewHolder>(MemberItemViewHolder.class) {

            @Override
            public void onBindView(@NonNull MemberItemViewHolder holder, Participant data,
                                   int position) {
                try {
                    ParticipantMeta participantMeta = GsonHel.fromJson(data.getMetadata(),
                        ParticipantMeta.class);
                    holder.view.avatar.load(participantMeta.userInfo.getFaceURL());
                    holder.view.name.setText(getMetaUserName(participantMeta));

                    if (vm.isSelfHostUser.getValue()) {
                        holder.view.mic.setVisibility(View.VISIBLE);
                        holder.view.camera.setVisibility(View.VISIBLE);
                        holder.view.mic.setChecked(data.isMicrophoneEnabled());
                        holder.view.camera.setChecked(data.isCameraEnabled());
                        holder.view.mic.setOnClickListener(new OnDedrepClickListener() {
                            @Override
                            public void click(View v) {
                                vm.muteMic(data.getIdentity(), !holder.view.mic.isChecked());
                            }
                        });
                        holder.view.camera.setOnClickListener(new OnDedrepClickListener() {
                            @Override
                            public void click(View v) {
                                vm.muteCamera(data.getIdentity(), !holder.view.camera.isChecked());
                            }
                        });
                    } else {
                        holder.view.mic.setVisibility(View.GONE);
                        holder.view.camera.setVisibility(View.GONE);
                    }

                } catch (Exception ignored) {
                }
            }
        });

        v.invite.setOnClickListener(v1 -> {
            ARouter.getInstance().build(Routes.Contact.FORWARD).navigation(this,
                Constant.Event.FORWARD);
        });
        boolean isMuteAllMicrophone = vm.roomMetadata.getValue().isMuteAllMicrophone;
        v.allMute.setText(isMuteAllMicrophone ?
            io.openim.android.ouicore.R.string.cancle_all_mute :
            io.openim.android.ouicore.R.string.all_mute);
        v.allMute.setTag(isMuteAllMicrophone);
        v.allMute.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v1) {
                Object isAllMute = v1.getTag();
                final boolean isAll = !(boolean) isAllMute;
                Map<String, Object> map = new HashMap<>();
                map.put("roomID", vm.signalingCertificate.getRoomID());
                map.put("isMuteAllMicrophone", isAll);

                vm.updateMeetingInfo(map, data -> {
                    ((TextView) v1).setText(isAll ?
                        io.openim.android.ouicore.R.string.cancle_all_mute :
                        io.openim.android.ouicore.R.string.all_mute);
                    v1.setTag(isAll);

                    bottomPopDialog.dismiss();
                });
            }
        });
        return v.getRoot();
    }

    private void requestMediaProjection() {
        MediaProjectionManager mediaProjectionManager =
            (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        screenCaptureIntentLauncher.launch(mediaProjectionManager.createScreenCaptureIntent());
    }


    private void initView() {
        view.setMeetingVM(vm);
        view.setCallViewModel(vm.callViewModel);
        vm.initVideoView(view.centerTextureView);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(RecyclerView.HORIZONTAL);
        view.memberRecyclerView.setLayoutManager(lm);

        view.memberRecyclerView.setAdapter(adapter = new RecyclerViewAdapter<Participant,
            UserStreamViewHolder>(UserStreamViewHolder.class) {


            @Override
            public void onBindView(@NonNull UserStreamViewHolder holder, Participant data,
                                   int position) {
                try {
                    final Participant participant = data;
                    ParticipantMeta participantMeta = GsonHel.fromJson(participant.getMetadata(),
                        ParticipantMeta.class);
                    vm.initVideoView(holder.view.textureView);
                    bindRemoteViewRenderer(holder.view.textureView, participant);

                    String name = getMetaUserName(participantMeta);
                    String faceURL = participantMeta.userInfo.getFaceURL();
                    bindUserStatus(holder.view, name, faceURL, participant);
                    smallWindowSwitch(holder, participant.isCameraEnabled());
                    AtomicReference<Boolean> isOpenCamera =
                        new AtomicReference<>(participant.isCameraEnabled());
                    subscribeUserStatus(holder.view, participant, isOpen -> {
                        isOpenCamera.set(isOpen);
                        smallWindowSwitch(holder, isOpen);
                    });

                    holder.view.getRoot().setOnClickListener(v -> {
                        centerHandle(isOpenCamera.get(), participant, name, faceURL);
                    });
                    if (position == 0) {
                        centerHandle(isOpenCamera.get(), participant, name, faceURL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            /**
             * 屏幕中间直播画面数据处理
             */
            private void centerHandle(boolean isOpenCamera, Participant participant, String name,
                                      String faceURL) {
                centerCov(isOpenCamera);
                bindRemoteViewRenderer(view.centerTextureView, participant);
                bindUserStatus(view.centerUser, name, faceURL, participant);
                subscribeUserStatus(view.centerUser, participant, isOpen -> {
                    centerCov(isOpen);
                });
            }

            /**
             * 下边小窗口状态切换
             */
            private void smallWindowSwitch(@NonNull UserStreamViewHolder holder, Boolean isOpen) {
                if (isOpen) {
                    holder.view.userStatus.setVisibility(View.GONE);
                    holder.view.textureView.setVisibility(View.VISIBLE);
                } else {
                    holder.view.userStatus.setVisibility(View.VISIBLE);
                    holder.view.textureView.setVisibility(View.GONE);
                }
            }
        });
    }

    private String getMetaUserName(ParticipantMeta participantMeta) {
        String name = participantMeta.groupMemberInfo.getNickname();
        if (TextUtils.isEmpty(name)) name = participantMeta.userInfo.getNickname();
        return name;
    }

    private void bindUserStatus(LayoutUserStatusBinding view, String nickname, String faceUrl,
                                Participant participant) {
        if (null != nickname) view.name.setText(nickname);
        if (null != faceUrl) view.avatar.load(faceUrl);
        view.mc.setVisibility(vm.isHostUser(participant) ? View.VISIBLE : View.GONE);
        view.mic.setImageResource(participant.isMicrophoneEnabled() ?
            io.openim.android.ouicore.R.mipmap.ic__mic_on :
            io.openim.android.ouicore.R.mipmap.ic__mic_off);
        L.e("-------bindUserStatus--------"+participant.getConnectionQuality());
        bindConnectionQuality(view, participant.getConnectionQuality());
    }

    @NonNull
    private CoroutineScope bindCoroutineScope(View view) {
        if (view.getTag() instanceof CoroutineScope) {
            vm.callViewModel.scopeCancel((CoroutineScope) view.getTag());
        }
        CoroutineScope scope = vm.callViewModel.buildScope();
        view.setTag(scope);
        return scope;
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

    private void subscribeUserStatus(LayoutUserStatusBinding holder, Participant participant,
                                     CameraOpenListener cameraOpenListener) {
        final CoroutineScope scope = bindCoroutineScope(holder.userStatus);

        vm.callViewModel.subscribe(participant.getEvents().getEvents(), (v) -> {
            L.e("-------subscribe--------");
            bindUserStatus(holder, null, null, v.getParticipant());
            if (null != cameraOpenListener) {
                cameraOpenListener.onOpen(v.getParticipant().isCameraEnabled());
            }
            StateFlow<ConnectionQuality> flow =
                vm.callViewModel.getConnectionFlow(v.getParticipant());
            vm.callViewModel.subscribe(flow, (quality) -> {
                L.e("-------quality subscribe--------");
                bindConnectionQuality(holder, quality);
                return null;
            }, scope);

            return null;
        }, scope);
    }

    private void bindConnectionQuality(LayoutUserStatusBinding holder, ConnectionQuality quality) {
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

        vm.callViewModel.subscribe(vm.callViewModel.getAllParticipants(), (v) -> {
            if (v.isEmpty()) return null;
            adapter.setItems(vm.handleParticipants(v));
            return null;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == Constant.Event.FORWARD && null != data) {
            //在这里转发
            String id = data.getStringExtra(Constant.K_ID);
            String otherSideNickName = data.getStringExtra(Constant.K_NAME);
            String groupId = data.getStringExtra(Constant.K_GROUP_ID);
            vm.inviterUser(id, groupId);
        }
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
        }
    }

    public static class MemberItemViewHolder extends RecyclerView.ViewHolder {
        public final MeetingIietmMemberBinding view;

        public MemberItemViewHolder(@NonNull View itemView) {
            super(MeetingIietmMemberBinding.inflate(LayoutInflater.from(itemView.getContext()),
                (ViewGroup) itemView, false).getRoot());
            view = MeetingIietmMemberBinding.bind(this.itemView);
        }
    }

    interface CameraOpenListener {
        void onOpen(Boolean isOpen);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            vm.onCleared();
            removeCacheVM();
        }
    }

    @Override
    protected void onDestroy() {
        vm.onCleared();
        removeCacheVM();
        super.onDestroy();
    }
}
