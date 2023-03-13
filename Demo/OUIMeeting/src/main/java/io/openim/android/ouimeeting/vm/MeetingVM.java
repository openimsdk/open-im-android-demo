package io.openim.android.ouimeeting.vm;

import android.annotation.SuppressLint;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.livekit.android.renderer.TextureViewRenderer;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.IView;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouimeeting.entity.RoomMetadata;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.SignalingCertificate;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.flow.AbstractFlow;
import kotlinx.coroutines.flow.FlowCollector;

public class MeetingVM extends BaseViewModel<MeetingVM.Interaction> {
    //上次摄像头开关、是否又开启权限
    boolean lastCameraEnabled, lastIsMuteAllVideo;
    //通话时间
    private Timer timer;
    private int second = 0;
    public MutableLiveData<String> timeStr = new MutableLiveData<>("");

    private List<TextureViewRenderer> textureViews;

    public SignalingCertificate signalingCertificate;
    public CallViewModel callViewModel;
    public MutableLiveData<RoomMetadata> roomMetadata = new MutableLiveData<>();
    public MutableLiveData<Boolean> isSelfHostUser = new MutableLiveData<>();


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
        callViewModel = new CallViewModel(BaseApp.inst());
        callViewModel.subscribe(callViewModel.getRoomMetadata(), (v) -> {
            fJsonRoomMetadata(callViewModel.getRoom().getMetadata());
            return null;
        });
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

    public void joinMeeting(String roomID) {
        OpenIMClient.getInstance().signalingManager.signalingJoinMeeting(new OnBase<SignalingCertificate>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error);
            }

            @Override
            public void onSuccess(SignalingCertificate data) {
                signalingCertificate = data;
                getIView().onSuccess(data);
            }
        }, roomID, null, null);
    }

    public void createMeeting(String meetingName, long startTime, int duration) {
        OpenIMClient.getInstance().signalingManager.signalingCreateMeeting(new OnBase<SignalingCertificate>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error);
            }

            @Override
            public void onSuccess(SignalingCertificate data) {
                signalingCertificate = data;
                getIView().onSuccess(data);
            }
        }, meetingName, BaseApp.inst().loginCertificate.userID, startTime, duration, null, null);
    }

    public void connectRoom() {
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
                    callViewModel.setCameraEnabled(false);
                    try {
                        isSelfHostUser.setValue(isHostUser(callViewModel.getRoom().getLocalParticipant()));
                    } catch (Exception ignored) {
                    }

                    buildTimer();
                    String meta = callViewModel.getRoom().getMetadata();
                    L.e("-------roomMetadata-------" + meta);
                    fJsonRoomMetadata(meta);
                    if (isSelfHostUser.getValue()) {
                        roomMetadata.getValue().isMuteAllVideo = false;
                        roomMetadata.getValue().isMuteAllMicrophone = false;
                        roomMetadata.getValue().onlyHostShareScreen = false;
                        roomMetadata.getValue().onlyHostInviteUser = false;
                        roomMetadata.setValue(roomMetadata.getValue());
                    }

                    VideoTrack localVideoTrack =
                        callViewModel.getVideoTrack(callViewModel.getRoom().getLocalParticipant());
                    getIView().connectRoomSuccess(localVideoTrack);
                });
            }
        });
    }

    private void fJsonRoomMetadata(String json) {
        try {
            roomMetadata.setValue(GsonHel.fromJson(json, RoomMetadata.class));
        } catch (Exception ignored) {
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
        lastIsMuteAllVideo = roomMetadata.getValue().isMuteAllVideo;

        callViewModel.setCameraEnabled(false);
        roomMetadata.getValue().isMuteAllVideo = true;
        roomMetadata.setValue(roomMetadata.getValue());
        callViewModel.startScreenCapture(data);
    }

    public void stopShareScreen() {
        callViewModel.stopScreenCapture();
        callViewModel.setCameraEnabled(lastCameraEnabled);
        roomMetadata.getValue().isMuteAllVideo = lastIsMuteAllVideo;
        roomMetadata.setValue(roomMetadata.getValue());
    }


    public interface Interaction extends IView {
        void connectRoomSuccess(VideoTrack localVideoTrack);
    }

    public void onCleared() {
        callViewModel.onCleared();
        cancelTimer();
    }

}
