package io.openim.android.ouimeeting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;
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
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.ParticipantMeta;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouimeeting.databinding.ActivityMeetingHomeBinding;
import io.openim.android.ouimeeting.databinding.LayoutMeetingInfoDialogBinding;
import io.openim.android.ouimeeting.databinding.LayoutMemberDialogBinding;
import io.openim.android.ouimeeting.databinding.LayoutSettingDialogBinding;
import io.openim.android.ouimeeting.databinding.LayoutUserStatusBinding;
import io.openim.android.ouimeeting.databinding.MeetingIietmMemberBinding;
import io.openim.android.ouimeeting.entity.RoomMetadata;
import io.openim.android.ouimeeting.vm.MeetingVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.UserInfo;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.flow.StateFlow;
import open_im_sdk_callback.Base;

public class MeetingHomeActivity extends BaseActivity<MeetingVM, ActivityMeetingHomeBinding> implements MeetingVM.Interaction {


    private RecyclerViewAdapter<Participant, UserStreamViewHolder> adapter;
    private BottomPopDialog bottomPopDialog, settingPopDialog, meetingInfoPopDialog, exitPopDialog;
    private RecyclerViewAdapter<Participant, MemberItemViewHolder> memberAdapter;
    //触发横屏 决定当前绑定的MeetingVM 是否释放
    private boolean triggerLandscape = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(MeetingVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMeetingHomeBinding.inflate(getLayoutInflater()));
        initView();

        if (vm.isInit) {
            if (vm.isLandscape)
                toast(getString(io.openim.android.ouicore.R.string.double_tap_tips));
            connectRoomSuccess(vm.callViewModel.getVideoTrack(vm.callViewModel.getRoom().getLocalParticipant()));
        } else init();

        bindVM();
        listener();

        if (vm.isLandscape) Common.UIHandler.post(() -> setMemberExpand(view.expand));
    }


    @Override
    protected void requestedOrientation() {
    }

    @Override
    public void onBackPressed() {
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
    private final GestureDetector gestureDetector = new GestureDetector(BaseApp.inst(),
        new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            boolean isEx = getViewBooleanTag(view.topTitle);
            view.topTitle.setTag(!isEx);
            if (isEx) {
                view.topTitle.setVisibility(View.VISIBLE);
                view.bottomMenu.setVisibility(View.VISIBLE);
            } else {
                view.topTitle.setVisibility(View.GONE);
                view.bottomMenu.setVisibility(View.GONE);
            }
            return super.onDoubleTap(e);
        }
    });

    private void listener() {
        if (vm.isInit && vm.isLandscape) {
            view.center.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        }
        view.landscape.setOnClickListener(v -> {
            triggerLandscape = true;
            vm.isInit = true;
            setRequestedOrientation(vm.isLandscape ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            vm.isLandscape = !vm.isLandscape;
        });

        View.OnClickListener clickListener = v -> {
            if (null == meetingInfoPopDialog)
                meetingInfoPopDialog = new BottomPopDialog(this, buildMeetingInfoPopDialogView());
            meetingInfoPopDialog.show();
        };
        view.topCenter.setOnClickListener(clickListener);
        view.down.setOnClickListener(clickListener);
        view.end.setOnClickListener(v -> {
            if (!vm.isSelfHostUser.getValue()) {
                exit(false);
                return;
            }
            if (null == exitPopDialog) exitPopDialog = new BottomPopDialog(this);
            exitPopDialog.show();
            exitPopDialog.getMainView().menu3.setOnClickListener(v1 -> exitPopDialog.dismiss());
            exitPopDialog.getMainView().menu1.setText(io.openim.android.ouicore.R.string.exit_meeting);
            exitPopDialog.getMainView().menu2.setText(io.openim.android.ouicore.R.string.finish_meeting);
            exitPopDialog.getMainView().menu2.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            exitPopDialog.getMainView().menu1.setOnClickListener(v1 -> {
                exit(false);
                exitPopDialog.dismiss();
            });
            exitPopDialog.getMainView().menu2.setOnClickListener(v1 -> {
                exit(true);
                exitPopDialog.dismiss();
            });
        });
        view.expand.setOnClickListener(v -> {
            setMemberExpand(v);
        });
        view.mic.setOnClickListener(v -> vm.callViewModel.setMicEnabled(view.mic.isChecked()));
        view.camera.setOnClickListener(v -> vm.callViewModel.setCameraEnabled(view.camera.isChecked()));
        view.shareScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestMediaProjection();
            } else {
                vm.stopShareScreen();
            }
        });
        view.member.setOnClickListener(v -> {
            bottomPopDialog = new BottomPopDialog(this, buildPopView());

            List<Participant> participants = new ArrayList<>();
            participants.addAll(adapter.getItems());
            memberAdapter.setItems(participants);
            bottomPopDialog.show();
        });
        view.setting.setOnClickListener(v -> {
            if (null == settingPopDialog)
                settingPopDialog = new BottomPopDialog(this, buildSettingPopView());
            settingPopDialog.show();
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

        view.horn.setOnClickListener(v -> {
            boolean isHorn = getViewBooleanTag(v);
            v.setTag(!isHorn);
            vm.isReceiver.setValue(!isHorn);
        });
        vm.isReceiver.observe(this, aBoolean -> {
            vm.audioManager.setSpeakerphoneOn(!aBoolean);
            view.horn.setImageResource(aBoolean ?  R.mipmap.ic_m_horn
                :R.mipmap.ic_m_receiver);
        });

    }

    private void setMemberExpand(View v) {
        boolean isExpand = getViewBooleanTag(v);
        v.setTag(!isExpand);
        if (isExpand) {
            v.setRotationX(0);
            view.memberRecyclerView.setVisibility(View.VISIBLE);
        } else {
            v.setRotationX(180);
            view.memberRecyclerView.setVisibility(View.GONE);
        }
    }

    private boolean getViewBooleanTag(View v) {
        Object tag = v.getTag();
        if (null == tag) tag = false;
        return (boolean) tag;
    }

    private void exit(Boolean isFinishMeeting) {
        CommonDialog commonDialog = new CommonDialog(this).atShow();
        commonDialog.getMainView().tips.setText(isFinishMeeting ?
            io.openim.android.ouicore.R.string.exit_meeting_tips2 :
            io.openim.android.ouicore.R.string.exit_meeting_tips);
        commonDialog.getMainView().cancel.setOnClickListener(v1 -> commonDialog.dismiss());
        commonDialog.getMainView().confirm.setOnClickListener(v1 -> {
            commonDialog.dismiss();
            if (isFinishMeeting) {
                setResult(RESULT_OK);
                vm.finishMeeting();
            }
            finish();
        });
    }


    private View buildMeetingInfoPopDialogView() {
        LayoutMeetingInfoDialogBinding v =
            LayoutMeetingInfoDialogBinding.inflate(getLayoutInflater());
        RoomMetadata roomMetadata = vm.roomMetadata.getValue();
        v.title.setText(roomMetadata.meetingName);
        List<String> ids = new ArrayList<>();
        ids.add(roomMetadata.hostUserID);
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                toast(error);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;
                BigDecimal bigDecimal =
                    (BigDecimal.valueOf(roomMetadata.endTime - roomMetadata.startTime).divide(BigDecimal.valueOf(3600), 1, BigDecimal.ROUND_HALF_DOWN));
                String durationStr =
                    bigDecimal.toString() + BaseApp.inst().getString(io.openim.android.ouicore.R.string.hour);
                v.description.setText(getString(io.openim.android.ouicore.R.string.meeting_num) + "：" + roomMetadata.meetingID + "\n" + getString(io.openim.android.ouicore.R.string.emcee) + "：" + data.get(0).getNickname() + "\n" + getString(io.openim.android.ouicore.R.string.start_time) + "：" + TimeUtil.getTime(roomMetadata.createTime * 1000, TimeUtil.yearMonthDayFormat) + "\t\t" + TimeUtil.getTime(roomMetadata.startTime * 1000, TimeUtil.hourTimeFormat) + "\n" + getString(io.openim.android.ouicore.R.string.meeting_duration) + "：" + durationStr);
            }
        }, ids);
        return v.getRoot();
    }

    private View buildSettingPopView() {
        LayoutSettingDialogBinding v = LayoutSettingDialogBinding.inflate(getLayoutInflater());
        vm.roomMetadata.observe(this, roomMetadata -> {
            v.allowCancelMute.setCheckedWithAnimation(roomMetadata.participantCanUnmuteSelf);
            v.allowOpenCamera.setCheckedWithAnimation(roomMetadata.participantCanEnableVideo);
            v.onlyHostShare.setCheckedWithAnimation(roomMetadata.onlyHostShareScreen);
            v.onlyHostInvite.setCheckedWithAnimation(roomMetadata.onlyHostInviteUser);
            v.joinMute.setCheckedWithAnimation(roomMetadata.joinDisableMicrophone);
        });
        RoomMetadata roomMetadata = vm.roomMetadata.getValue();
        v.allowCancelMute.setOnSlideButtonClickListener(isChecked -> roomMetadata.participantCanUnmuteSelf = isChecked);
        v.allowOpenCamera.setOnSlideButtonClickListener(isChecked -> roomMetadata.participantCanEnableVideo = isChecked);
        v.onlyHostShare.setOnSlideButtonClickListener(isChecked -> roomMetadata.onlyHostShareScreen = isChecked);
        v.onlyHostInvite.setOnSlideButtonClickListener(isChecked -> roomMetadata.onlyHostInviteUser = isChecked);
        v.joinMute.setOnSlideButtonClickListener(isChecked -> roomMetadata.joinDisableMicrophone
            = isChecked);
        v.sure.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                Map map = JSONObject.parseObject(GsonHel.toJson(vm.roomMetadata.getValue()),
                    Map.class);
                map.put("roomID", vm.signalingCertificate.getRoomID());
                vm.updateMeetingInfo(map, data -> settingPopDialog.dismiss());
            }
        });
        return v.getRoot();
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

        v.invite.setVisibility(vm.memberPermission.getValue() ? View.VISIBLE : View.GONE);
        v.invite.setOnClickListener(v1 -> {
            ARouter.getInstance().build(Routes.Contact.FORWARD).navigation(this,
                Constant.Event.FORWARD);
        });
        boolean isMuteAllMicrophone = vm.roomMetadata.getValue().isMuteAllMicrophone;

        v.allMute.setVisibility(vm.isSelfHostUser.getValue() ? View.VISIBLE : View.GONE);
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
        try {
            RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) view.centerUser2.getRoot().getLayoutParams();
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            LinearLayout.LayoutParams params2 =
                (LinearLayout.LayoutParams) view.centerUser2.userStatus.getLayoutParams();
            params2.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            params2.gravity = Gravity.END;
            view.centerUser2.getRoot().setBackgroundColor(Color.TRANSPARENT);
            view.centerUser2.avatar.setVisibility(View.GONE);
        } catch (Exception ignored) {
        }

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
                        vm.selectCenterIndex = position;
                        centerHandle(isOpenCamera.get(), participant, name, faceURL);
                    });
                    if (position == vm.selectCenterIndex) {
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

                bindUserStatus(view.centerUser2, name, faceURL, participant);
                subscribeUserStatus(view.centerUser2, participant, null);
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
        L.e("-------bindUserStatus--------" + participant.getConnectionQuality());
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
            view.centerUser2.getRoot().setVisibility(View.VISIBLE);
        } else {
            view.centerTextureView.setVisibility(View.GONE);
            view.centerUser2.getRoot().setVisibility(View.GONE);
            view.centerUser.getRoot().setVisibility(View.VISIBLE);
        }
    }

    private void bindRemoteViewRenderer(TextureViewRenderer textureView, Participant data) {
        removeRenderer(textureView);
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

    private void removeRenderer(TextureViewRenderer textureView) {
        Object speakerVideoViewTag = textureView.getTag();
        if (speakerVideoViewTag instanceof VideoTrack) {
            ((VideoTrack) speakerVideoViewTag).removeRenderer(textureView);
        }
    }

    private void subscribeUserStatus(LayoutUserStatusBinding holder, Participant participant,
                                     CameraOpenListener cameraOpenListener) {
        final CoroutineScope scope = bindCoroutineScope(holder.userStatus);

        vm.callViewModel.subscribe(participant.getEvents().getEvents(), (v) -> {
            L.e("-------subscribe--------" + (holder == view.centerUser));
            bindUserStatus(holder, null, null, v.getParticipant());
            if (null != cameraOpenListener) {
                cameraOpenListener.onOpen(v.getParticipant().isCameraEnabled());
            }
            StateFlow<ConnectionQuality> flow =
                vm.callViewModel.getConnectionFlow(v.getParticipant());
            vm.callViewModel.subscribe(flow, (quality) -> {
                L.e("-------quality subscribe--------" + (holder == view.centerUser));
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

    private void bindVM() {
        view.setMeetingVM(vm);
        view.setCallViewModel(vm.callViewModel);
        vm.initVideoView(view.centerTextureView);
    }


    @Override
    protected void setLightStatus() {

    }


    @Override
    public void connectRoomSuccess(VideoTrack localVideoTrack) {
        view.landscape.setVisibility(View.VISIBLE);
        removeRenderer(view.centerTextureView);
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
        release();
    }

    private void release() {
        if (isFinishing() && !triggerLandscape) {
            vm.audioManager.setSpeakerphoneOn(true);
            vm.onCleared();
            removeCacheVM();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }
}
