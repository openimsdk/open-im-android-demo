package io.openim.android.ouimeeting.vm;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.Arrays;
import java.util.List;

import io.livekit.android.renderer.TextureViewRenderer;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.IView;
import io.openim.android.ouicore.utils.Common;
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
    //是否是加入会议
    public boolean isJoinMeeting=false;
    private List<TextureViewRenderer> textureViews;

    public SignalingCertificate signalingCertificate;
    public CallViewModel callViewModel;

    public void init() {
        callViewModel = new CallViewModel(BaseApp.inst());
        callViewModel.getMutableMicEnabled().observe((LifecycleOwner) getIView(),
            aBoolean -> callViewModel.setMicEnabled(aBoolean));
        callViewModel.getMutableCameraEnabled().observe((LifecycleOwner) getIView(),
            aBoolean -> callViewModel.setCameraEnabled(aBoolean));
    }

    public void initVideoView(TextureViewRenderer... viewRenderers) {
        textureViews = Arrays.asList(viewRenderers);
        for (TextureViewRenderer viewRenderer : viewRenderers) {
          try {
              callViewModel.getRoom().initVideoRenderer(viewRenderer);
          }catch (Exception ignored){}
        }
    }


    public void fastMeeting() {
        String name =
            String.format(BaseApp.inst().getString(io.openim.android.ouicore.R.string.meeting_initiator), BaseApp.inst().loginCertificate.nickname);
        long startTime = System.currentTimeMillis() / 1000;
        createMeeting(name, startTime, 60 * 60);
    }

    public void joinMeeting(String roomID) {
        OpenIMClient.getInstance().signalingManager
            .signalingJoinMeeting(new OnBase<SignalingCertificate>() {
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

    public void createMeeting(String name, long startTime, int duration) {
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
        }, name, BaseApp.inst().loginCertificate.userID, startTime, duration, null, null);
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
                    callViewModel.getMutableCameraEnabled().setValue(false);
                    VideoTrack localVideoTrack =
                        callViewModel.getVideoTrack(callViewModel.getRoom().getLocalParticipant());
                    getIView().connectRoomSuccess(localVideoTrack);
                });
            }
        });
    }


    public interface Interaction extends IView {
        void connectRoomSuccess(VideoTrack localVideoTrack);
    }
}
