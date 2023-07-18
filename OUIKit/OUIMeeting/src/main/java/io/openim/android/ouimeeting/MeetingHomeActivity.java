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
import androidx.viewpager.widget.PagerAdapter;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
import io.openim.android.ouimeeting.databinding.ViewSingleTextureBinding;
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


    private PageAdapter adapter;
    private BottomPopDialog bottomPopDialog, settingPopDialog, meetingInfoPopDialog, exitPopDialog;
    private RecyclerViewAdapter<Participant, MemberItemViewHolder> memberAdapter;
    //触发横屏 决定当前绑定的MeetingVM 是否释放
    private boolean triggerLandscape = false;
    private List<Participant> memberParticipants = new ArrayList<>();
    private Participant activeSpeaker;
    //每页显示多少Participant
    private final int pageShow = 4;

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

    private void listener() {


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

            List<Participant> participants = new ArrayList<>(memberParticipants);
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
            view.horn.setImageResource(aBoolean ? R.mipmap.ic_m_horn : R.mipmap.ic_m_receiver);
        });
        view.zoomOut.setOnClickListener(v -> {

        });
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
                vm.finishMeeting(vm.selectMeetingInfo.getMeetingID());
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
        view.pager.setAdapter(adapter = new PageAdapter(getLayoutInflater(), vm));

//        view.memberRecyclerView.setAdapter(adapter = new RecyclerViewAdapter<Participant,
//            UserStreamViewHolder>(UserStreamViewHolder.class) {
//
//            @Override
//            public void onBindView(@NonNull UserStreamViewHolder holder, Participant data,
//                                   int position) {
//                try {
//                    final Participant participant = data;
//                    ParticipantMeta participantMeta = GsonHel.fromJson(participant.getMetadata(),
//                        ParticipantMeta.class);
//                    vm.initVideoView(holder.view.textureView);
//                    bindRemoteViewRenderer(holder.view.textureView, participant);
//
//                    String name = getMetaUserName(participantMeta);
//                    String faceURL = participantMeta.userInfo.getFaceURL();
//                    bindUserStatus(holder.view, name, faceURL, participant);
//                    smallWindowSwitch(holder, participant.isCameraEnabled());
//                    AtomicReference<Boolean> isOpenCamera =
//                        new AtomicReference<>(participant.isCameraEnabled());
//                    subscribeUserStatus(holder.view, participant, isOpen -> {
//                        isOpenCamera.set(isOpen);
//                        smallWindowSwitch(holder, isOpen);
//                    });
//
//                    holder.view.getRoot().setOnClickListener(v -> {
//                        vm.selectCenterIndex = position;
//                        centerHandle(isOpenCamera.get(), participant, name, faceURL);
//                    });
//                    if (position == vm.selectCenterIndex) {
//                        centerHandle(isOpenCamera.get(), participant, name, faceURL);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            /**
//             * 屏幕中间直播画面数据处理
//             */
//            private void centerHandle(boolean isOpenCamera, Participant participant, String name,
//                                      String faceURL) {
//
//                centerCov(isOpenCamera);
//                bindRemoteViewRenderer(view.centerTextureView, participant);
//                bindUserStatus(view.centerUser, name, faceURL, participant);
//                subscribeUserStatus(view.centerUser, participant, isOpen -> {
//                    centerCov(isOpen);
//                });
//
//                bindUserStatus(view.centerUser2, name, faceURL, participant);
//                subscribeUserStatus(view.centerUser2, participant, null);
//            }
//
//            /**
//             * 下边小窗口状态切换
//             */
//            private void smallWindowSwitch(@NonNull UserStreamViewHolder holder, Boolean isOpen) {
//                if (isOpen) {
//                    holder.view.userStatus.setVisibility(View.GONE);
//                    holder.view.textureView.setVisibility(View.VISIBLE);
//                } else {
//                    holder.view.userStatus.setVisibility(View.VISIBLE);
//                    holder.view.textureView.setVisibility(View.GONE);
//                }
//            }
//        });
    }

    private String getMetaUserName(ParticipantMeta participantMeta) {
        String name = participantMeta.groupMemberInfo.getNickname();
        if (TextUtils.isEmpty(name)) name = participantMeta.userInfo.getNickname();
        return name;
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


//    private void subscribeUserStatus(LayoutUserStatusBinding holder, Participant participant,
//                                     CameraOpenListener cameraOpenListener) {
//        final CoroutineScope scope = bindCoroutineScope(holder.userStatus);
//
//        vm.callViewModel.subscribe(participant.getEvents().getEvents(), (v) -> {
//            L.e("-------subscribe--------" + (holder == view.centerUser));
//            bindUserStatus(holder, null, null, v.getParticipant());
//            if (null != cameraOpenListener) {
//                cameraOpenListener.onOpen(v.getParticipant().isCameraEnabled());
//            }
//            StateFlow<ConnectionQuality> flow =
//                vm.callViewModel.getConnectionFlow(v.getParticipant());
//            vm.callViewModel.subscribe(flow, (quality) -> {
//                L.e("-------quality subscribe--------" + (holder == view.centerUser));
//                bindConnectionQuality(holder, quality);
//                return null;
//            }, scope);
//
//            return null;
//        }, scope);
//    }


    void init() {
        vm.init();
        vm.connectRoom();
    }

    private void bindVM() {
        view.setMeetingVM(vm);
        view.setCallViewModel(vm.callViewModel);
//        vm.initVideoView(view.centerTextureView);
    }


    @Override
    protected void setLightStatus() {

    }


    @Override
    public void connectRoomSuccess(VideoTrack localVideoTrack) {
        view.landscape.setVisibility(View.VISIBLE);
//        removeRenderer(view.centerTextureView);
//        localVideoTrack.addRenderer(view.centerTextureView);
//        view.centerTextureView.setTag(localVideoTrack);


        vm.callViewModel.subscribe(vm.callViewModel.getAllParticipants(), (v) -> {
            if (v.isEmpty()) return null;
            memberParticipants = vm.handleParticipants(v);
            List<List<Participant>> data = new ArrayList<>();
            if (null != activeSpeaker) {
                data.add(new ArrayList<>(Collections.singleton(activeSpeaker)));
            }
            int pageNum = memberParticipants.size() / pageShow;
            if (pageNum == 0) {
                data.add(new ArrayList<>(memberParticipants));
            } else for (int i = 0; i < pageNum; i++) {
                List<Participant> participants = new ArrayList<>(memberParticipants.subList(i,
                    i * pageShow));
                data.add(participants);
            }

            adapter.setList(data);
            return null;
        });


        vm.callViewModel.subscribe(vm.callViewModel.getActiveSpeakers(), (v) -> {
            if (v.isEmpty()) return null;
            activeSpeaker = v.get(0);
            adapter.notifyDataSetChanged();
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
//            ViewGroup.LayoutParams params = view.avatar.getLayoutParams();
//            params.width = Common.dp2px(55);
//            params.height = Common.dp2px(55);
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

    private static class PageAdapter extends PagerAdapter {
        private LayoutInflater inflater;
        private List<List<Participant>> list = new ArrayList<>();
        private MeetingVM vm;

        public PageAdapter(LayoutInflater inflater, MeetingVM vm) {
            this.inflater = inflater;
            this.vm = vm;
        }

        public List<List<Participant>> getList() {
            return list;
        }

        public void setList(List<List<Participant>> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            List<Participant> participants = list.get(position);
            if (position == 0) {
                ViewSingleTextureBinding view = ViewSingleTextureBinding.inflate(inflater);
                if (participants.isEmpty()) return view.getRoot();
                Participant participant = participants.get(0);
                bindRemoteViewRenderer(view.textureView, participant);
                bindUserStatus(view.userStatus, participant);
            } else {

            }
            return super.instantiateItem(container, position);
        }

        private void removeRenderer(TextureViewRenderer textureView) {
            Object speakerVideoViewTag = textureView.getTag();
            if (speakerVideoViewTag instanceof VideoTrack) {
                ((VideoTrack) speakerVideoViewTag).removeRenderer(textureView);
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

        private void bindUserStatus(LayoutUserStatusBinding view, Participant participant) {
            view.mc.setVisibility(vm.isHostUser(participant) ? View.VISIBLE : View.GONE);
            view.mic.setImageResource(participant.isMicrophoneEnabled() ?
                io.openim.android.ouicore.R.mipmap.ic__mic_on :
                io.openim.android.ouicore.R.mipmap.ic__mic_off);
            ParticipantMeta meta = GsonHel.fromJson(participant.getMetadata(),
                ParticipantMeta.class);
            view.name.setText(meta.userInfo.getNickname());
            L.e("-------bindUserStatus--------" + participant.getConnectionQuality());
            bindConnectionQuality(view, participant.getConnectionQuality());
        }

        private void bindConnectionQuality(LayoutUserStatusBinding holder,
                                           ConnectionQuality quality) {
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

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }


    }
}
