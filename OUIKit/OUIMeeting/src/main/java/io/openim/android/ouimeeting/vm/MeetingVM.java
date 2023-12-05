package io.openim.android.ouimeeting.vm;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.livekit.android.renderer.TextureViewRenderer;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.IView;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.entity.MeetingInfoAttach;
import io.openim.android.ouicore.entity.ParticipantMeta;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouimeeting.MeetingHomeActivity;
import io.openim.android.ouimeeting.entity.RoomMetadata;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnMsgSendCallback;
import io.openim.android.sdk.models.MeetingInfo;
import io.openim.android.sdk.models.MeetingInfoList;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.SignalingCertificate;
import io.openim.android.sdk.models.UserInfo;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class MeetingVM extends BaseViewModel<MeetingVM.Interaction> {


    //预约上传的参数
    public static class TimingParameter {
        public State<String> meetingTheme = new State<>("");
        public State<Long> startTime = new State(0L);
        public State<String> startTimeStr = new State<>("");
        public State<Integer> duration = new State(0);
        public State<String> durationStr = new State("");
    }

    public TimingParameter timingParameter = new TimingParameter();

    //是否初始化、是否横屏，用于横竖屏切换
    public boolean isInit, isLandscape;
    public int selectCenterIndex = 0;
    //获取音频服务
    public AudioManager audioManager;
    //上次摄像头开关、上次麦克风开关、是否有开启权限
    boolean lastCameraEnabled, lastIsMuteAllVideo;
    //通话时间
    private Timer timer;
    public int second = 0;
    public State<String> timeStr = new State<>("");

    private List<TextureViewRenderer> textureViews;

    //是否听筒模式
    public State<Boolean> isReceiver = new State<>(false);

    public SignalingCertificate signalingCertificate;
    public CallViewModel callViewModel;
    public State<RoomMetadata> roomMetadata = new State<>();
    //在列表点击item选择的会议信息实体
    public MeetingInfo selectMeetingInfo;
    public State<List<MeetingInfo>> meetingInfoList = new State<>();
    //发起人信息
    public List<UserInfo> userInfos = new ArrayList<>();
    //都看他
    public State<Participant> allWatchedUser = new State<>();

    //下边菜单栏可点击权限
    public State<Boolean> micPermission = new State<>(false);
    public State<Boolean> cameraPermission = new State<>(false);
    public State<Boolean> sharePermission = new State<>(false);
    public State<Boolean> memberPermission = new State<>(false);
    public State<Boolean> isSelfHostUser = new State<>(false);

    public void buildTimer() {
        cancelTimer();
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                second++;
                String secondFormat = TimeUtil.secondFormat(second, TimeUtil.secondFormat);
                if (secondFormat.length() <= 2) secondFormat = "00:" + secondFormat;
                timeStr.postValue(secondFormat);
            }
        }, 0, 1000);
    }

    private void cancelTimer() {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
    }

    public void init() {
        audioManager = (AudioManager) BaseApp.inst().getSystemService(Context.AUDIO_SERVICE);
        callViewModel = new CallViewModel(BaseApp.inst());
        callViewModel.subscribe(callViewModel.getRoomMetadata(), (v) -> {
            fJsonRoomMetadata(callViewModel.getRoom().getMetadata());
            return null;
        });
    }

    public void buildMetaData(List<Participant> v) {
        try {

            for (int i = 0; i < v.size(); i++) {
                Participant data = v.get(i);
                ParticipantMeta participantMeta = GsonHel.fromJson(data.getMetadata(),
                    ParticipantMeta.class);
                participantMeta.setTop = null!= roomMetadata.val().pinedUserIDList&&
                    roomMetadata.val().pinedUserIDList.contains(data.getIdentity());
                data.setMetadata$livekit_android_sdk_release(GsonHel.toJson(participantMeta));
            }
        } catch (Exception ignored) {}
    }

    public String getMetaUserName(ParticipantMeta participantMeta) {
        try {
            String name = participantMeta.groupMemberInfo.getNickname();
            if (TextUtils.isEmpty(name)) name = participantMeta.userInfo.getNickname();
            return name;
        } catch (Exception ignore) {
        }
        return "";
    }

    public void initVideoView(TextureViewRenderer... viewRenderers) {
        textureViews = Arrays.asList(viewRenderers);
        for (TextureViewRenderer viewRenderer : viewRenderers) {
            try {
                callViewModel.getRoom().initVideoRenderer(viewRenderer);
            } catch (Exception ignored) {
            }
        }
    }

    public void fastMeeting() {
        String name =
            String.format(BaseApp.inst().getString(io.openim.android.ouicore.R.string.meeting_initiator), BaseApp.inst().loginCertificate.nickname);
        long startTime = System.currentTimeMillis() / 1000;
        createMeeting(name, startTime, 2 * 60 * 60);
    }

    private final OnBase<SignalingCertificate> signalingCertificateCallBack =
        new OnBase<SignalingCertificate>() {
        @Override
        public void onError(int code, String error) {
            getIView().onError(error);
        }

        @Override
        public void onSuccess(SignalingCertificate data) {
            if (isDestroy || null == data) return;
            signalingCertificate = data;
            getIView().onSuccess(data);
        }
    };

    private boolean isCalling() {
        if (null != ActivityManager.isExist(MeetingHomeActivity.class)) {
            Toast.makeText(BaseApp.inst(), io.openim.android.ouicore.R.string.now_calling,
                Toast.LENGTH_SHORT).show();
            BaseDialog.dismissAll();
            return true;
        }
        return false;
    }

    public boolean isAllSeeHe(Participant data) {
        if (null == allWatchedUser.val()) return false;
        return Objects.equals(allWatchedUser.val().getIdentity(), data.getIdentity());
    }

    public void joinMeeting(String roomID) {
        if (isCalling()) return;

        //3185791707
        OpenIMClient.getInstance().signalingManager.signalingJoinMeeting(signalingCertificateCallBack, roomID, null, null);
    }

    public void createMeeting(String meetingName, long startTime, int duration) {
        if (isCalling()) return;
        OpenIMClient.getInstance().signalingManager.signalingCreateMeeting(signalingCertificateCallBack, meetingName, BaseApp.inst().loginCertificate.userID, startTime, duration, null, null);
    }

    public void connectRoom() {
        if (null == signalingCertificate) return;
        callViewModel.connectToRoom(signalingCertificate.getLiveURL(),
            signalingCertificate.getToken(), new Continuation<Unit>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NonNull Object o) {
                Common.UIHandler.post(() -> {
                    buildTimer();
                    fJsonRoomMetadata(callViewModel.getRoom().getMetadata());
                    try {
                        VideoTrack localVideoTrack =
                            callViewModel.getVideoTrack(callViewModel.getRoom().getLocalParticipant());

                        //  callViewModel.setCameraEnabled(false);
                        callViewModel.setCameraEnabled(!roomMetadata.val().joinDisableVideo);
                        callViewModel.setMicEnabled(!roomMetadata.val().joinDisableMicrophone);
                        getIView().connectRoomSuccess(localVideoTrack);
                    }catch (Exception ignore){}
                });
            }
        });
    }


    private void handlePermission() {
        RoomMetadata meta = roomMetadata.getValue();
        isSelfHostUser.setValue(isHostUser(callViewModel.getRoom().getLocalParticipant()));
        if (Boolean.TRUE.equals(isSelfHostUser.getValue())) {
            micPermission.setValue(true);
            cameraPermission.setValue(true);
            sharePermission.setValue(true);
            memberPermission.setValue(true);
        } else {
            micPermission.setValue(meta.participantCanUnmuteSelf);
            cameraPermission.setValue(meta.participantCanEnableVideo);
            sharePermission.setValue(!meta.onlyHostShareScreen);
            memberPermission.setValue(!meta.onlyHostInviteUser);
        }
    }

    private void fJsonRoomMetadata(String json) {
        try {
            L.e("-------roomMetadata-------" + json);
            roomMetadata.setValue(GsonHel.fromJson(json, RoomMetadata.class));
            handlePermission();
            allSeeHe();
        } catch (Exception ignored) {
        }
    }

    public void allSeeHe() {
        RoomMetadata meta = roomMetadata.val();
        if (null == meta) return;
        if (null != meta.beWatchedUserIDList) {
            if (meta.beWatchedUserIDList.isEmpty()) {
                allWatchedUser.setValue(null);
            } else {
                String id = meta.beWatchedUserIDList.get(0);
                Participant localParticipant = callViewModel.getRoom().getLocalParticipant();
                if (Objects.equals(localParticipant.getIdentity(), id)) {
                    allWatchedUser.setValue(localParticipant);
                } else {
                    for (Participant value :
                        callViewModel.getRoom().getRemoteParticipants().values()) {
                        if (Objects.equals(value.getIdentity(), id)) {
                            allWatchedUser.setValue(value);
                        }
                    }
                }
            }
        }
    }


    public List<Participant> handleParticipants(List<Participant> v) {
        Participant hostUser = null;
        for (Participant participant : v) {
            if (isHostUser(participant)) {
                hostUser = participant;
            }
        }
        if (null != hostUser) {
            v.remove(hostUser);
            v.add(0, hostUser);
        }
        return v;
    }

    public boolean isHostUser(Participant participant) {
        if (null == roomMetadata.getValue()) return false;
        return null != participant.getIdentity() && participant.getIdentity().equals(roomMetadata.getValue().hostUserID);
    }


    public void startShareScreen(Intent data) {
        lastCameraEnabled = callViewModel.getRoom().getLocalParticipant().isCameraEnabled();
        lastIsMuteAllVideo = cameraPermission.getValue();

        callViewModel.setCameraEnabled(false);
        cameraPermission.setValue(false);
        roomMetadata.setValue(roomMetadata.getValue());
        callViewModel.startScreenCapture(data);
    }

    public void stopShareScreen() {
        callViewModel.stopScreenCapture();
        callViewModel.setCameraEnabled(lastCameraEnabled);
        cameraPermission.setValue(lastIsMuteAllVideo);
        roomMetadata.setValue(roomMetadata.getValue());
    }

    public void getMeetingInfoList() {
        OpenIMClient.getInstance().signalingManager.signalingGetMeetings(new OnBase<MeetingInfoList>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error);
            }

            @Override
            public void onSuccess(MeetingInfoList meetingInfo) {
                if (null == meetingInfo.getMeetingInfoList())
                    meetingInfo.setMeetingInfoList(new ArrayList<>());
                List<String> hostUserIDs = new ArrayList<>();
                for (MeetingInfo info : meetingInfo.getMeetingInfoList()) {
                    hostUserIDs.add(info.getHostUserID());
                }
                if (hostUserIDs.isEmpty()) {
                    meetingInfoList.setValue(meetingInfo.getMeetingInfoList());
                    return;
                }
                OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
                    @Override
                    public void onError(int code, String error) {
                        getIView().toast(error);
                    }

                    @Override
                    public void onSuccess(List<UserInfo> data) {
                        if (null != data) userInfos = data;
                        meetingInfoList.setValue(meetingInfo.getMeetingInfoList());
                    }
                }, hostUserIDs);
            }
        });
    }

    public void muteCamera(String identity, boolean isMute) {
        signalingOperateStream(identity, "video", isMute);
    }

    public void muteMic(String identity, boolean isMute) {
        signalingOperateStream(identity, "audio", isMute);
    }

    void signalingOperateStream(String identity, String stType, boolean isMute) {
        OpenIMClient.getInstance().signalingManager.signalingOperateStream(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error);
            }

            @Override
            public void onSuccess(String data) {
            }
        }, signalingCertificate.getRoomID(), stType, identity, isMute, false);
    }

    public void inviterUser(String id, String groupId) {
        io.openim.android.ouicore.entity.MeetingInfo meetingInfo =
            new io.openim.android.ouicore.entity.MeetingInfo();
        try {
            meetingInfo.inviterNickname = BaseApp.inst().loginCertificate.nickname;
            meetingInfo.inviterUserID = BaseApp.inst().loginCertificate.userID;
            meetingInfo.inviterFaceURL = BaseApp.inst().loginCertificate.faceURL;
            if (null == selectMeetingInfo) {
                selectMeetingInfo = new MeetingInfo();
                RoomMetadata metadata = roomMetadata.getValue();
                selectMeetingInfo.setMeetingID(metadata.roomID);
                selectMeetingInfo.setMeetingName(metadata.meetingName);
                selectMeetingInfo.setStartTime(metadata.startTime);
                selectMeetingInfo.setEndTime(metadata.endTime);
            }
            meetingInfo.id = selectMeetingInfo.getMeetingID();
            meetingInfo.subject = selectMeetingInfo.getMeetingName();
            meetingInfo.start = selectMeetingInfo.getStartTime();
            meetingInfo.duration =
                (int) (selectMeetingInfo.getEndTime() - selectMeetingInfo.getStartTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put(Constant.K_CUSTOM_TYPE, Constant.MsgType.CUSTOMIZE_MEETING);
        map.put(Constant.K_DATA, meetingInfo);

        Message msg =
            OpenIMClient.getInstance().messageManager.createCustomMessage(GsonHel.toJson(map),
                null, null);
        OfflinePushInfo offlinePushInfo = new OfflinePushInfo(); // 离线推送的消息备注；不为null
        OpenIMClient.getInstance().messageManager.sendMessage(new OnMsgSendCallback() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + code);
            }

            @Override
            public void onProgress(long progress) {
            }

            @Override
            public void onSuccess(Message message) {
                getIView().toast(getContext().getString(io.openim.android.ouicore.R.string.send_succ));
            }
        }, msg, id, groupId, offlinePushInfo);
    }


    public void updateMeetingInfo(Map<String, Object> configure,
                                  IMUtil.OnSuccessListener<String> OnSuccessListener) {
        OpenIMClient.getInstance().signalingManager.signalingUpdateMeetingInfo(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error);
            }

            @Override
            public void onSuccess(String data) {
                if (null != OnSuccessListener) OnSuccessListener.onSuccess(data);
            }
        }, configure);
    }

    public void finishMeeting(String roomID) {
        OpenIMClient.getInstance().signalingManager.signalingCloseRoom(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error);
            }

            @Override
            public void onSuccess(String data) {
                getMeetingInfoList();
                getIView().toast(BaseApp.inst().getString(io.openim.android.ouicore.R.string.meeting_finish));
            }
        }, roomID);
    }


    public interface Interaction extends IView {
        void connectRoomSuccess(VideoTrack localVideoTrack);
    }

    public void onCleared() {
        if (null != callViewModel) callViewModel.onCleared();
        cancelTimer();
    }
}
