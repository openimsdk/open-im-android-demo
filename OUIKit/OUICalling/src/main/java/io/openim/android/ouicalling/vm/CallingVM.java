package io.openim.android.ouicalling.vm;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import org.webrtc.VideoSink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.livekit.android.renderer.TextureViewRenderer;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.participant.RemoteParticipant;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicalling.CallingServiceImp;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.CallingService;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.MediaPlayerUtil;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.SignalingCertificate;
import io.openim.android.sdk.models.SignalingInfo;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class CallingVM {
    //通话时间
    private Timer timer;
    private int second = 0;
    public MutableLiveData<String> timeStr = new MutableLiveData<>("");

    //获取音频服务
    public AudioManager audioManager;
    private DialogInterface.OnDismissListener dismissListener;
    private OnParticipantsChangeListener onParticipantsChangeListener;

    public final CallViewModel callViewModel;
    public final CallingService callingService;
    private VideoTrack localVideoTrack;
    //是否是视频通话
    public boolean isVideoCalls = true;
    //已经开始通话
    public boolean isStartCall;
    //呼出
    public boolean isCallOut;
    //是否是群
    public boolean isGroup;

    private TextureViewRenderer localSpeakerVideoView;
    private List<TextureViewRenderer> remoteSpeakerVideoViews;


    public CallingVM(CallingService callingService,
                     boolean isCallOut) {
        this.callingService = callingService;
        this.isCallOut = isCallOut;

        callViewModel = new CallViewModel(BaseApp.inst());
        audioManager = (AudioManager) BaseApp.inst().getSystemService(Context.AUDIO_SERVICE);
    }

    public void initRemoteVideoRenderer(TextureViewRenderer... viewRenderers) {
        remoteSpeakerVideoViews = Arrays.asList(viewRenderers);
        for (TextureViewRenderer viewRenderer : viewRenderers) {
            callViewModel.getRoom().initVideoRenderer(viewRenderer);
        }
    }

    public void setOnParticipantsChangeListener(OnParticipantsChangeListener onParticipantsChangeListener) {
        this.onParticipantsChangeListener = onParticipantsChangeListener;
    }

    public void setDismissListener(DialogInterface.OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public void setLocalSpeakerVideoView(TextureViewRenderer localSpeakerVideoView) {
        this.localSpeakerVideoView = localSpeakerVideoView;
        callViewModel.getRoom().initVideoRenderer(localSpeakerVideoView);
    }

    public void setVideoCalls(boolean videoCalls) {
        isVideoCalls = videoCalls;
    }

    private OnBase<SignalingCertificate> callBackDismissUI = new OnBase<SignalingCertificate>() {
        @Override
        public void onError(int code, String error) {
            L.e(CallingServiceImp.TAG, error + "-" + code);
            dismissUI();
        }

        @Override
        public void onSuccess(SignalingCertificate data) {
            dismissUI();
        }
    };

    public void signalingInvite(SignalingInfo signalingInfo) {
        OpenIMClient.getInstance().signalingManager.signalingInvite(new OnBase<SignalingCertificate>() {
            @Override
            public void onError(int code, String error) {
                L.e(CallingServiceImp.TAG, error + "-" + code);
                dismissUI();
            }

            @Override
            public void onSuccess(SignalingCertificate data) {
                L.e(CallingServiceImp.TAG, data.getToken());
                connectToRoom(data);
            }
        }, signalingInfo);
    }

    /**
     * 连接房间
     *
     * @param data
     */
    private void connectToRoom(SignalingCertificate data) {
        callViewModel.connectToRoom(data.getLiveURL(), data.getToken(), new Continuation<Unit>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NonNull Object o) {
                audioManager.setSpeakerphoneOn(true);
                if (!isVideoCalls)
                    callViewModel.setCameraEnabled(false);

                localVideoTrack = callViewModel.getVideoTrack(callViewModel.getRoom().getLocalParticipant());
                if (null != localVideoTrack && null != localSpeakerVideoView)
                    localVideoTrack.addRenderer(localSpeakerVideoView);

                callViewModel.getParticipants().collect((participants, continuation) -> {
                    if (participants.isEmpty()) return null;
                    if (!isGroup && !isCallOut &&participants.size() == 1) {
                        callingService.onHangup(null);
                        return null;
                    }
                    if (null != onParticipantsChangeListener) {
                        List<Participant> participantList = new ArrayList<>();
                        for (Participant participant : participants) {
                            if (participant instanceof RemoteParticipant)
                                participantList.add(participant);
                        }
                        onParticipantsChangeListener.onChange(participantList);
                    } else {
                        for (int i = 0; i < participants.size(); i++) {
                            Participant participant = participants.get(i);
                            if (participant instanceof RemoteParticipant) {
                                for (TextureViewRenderer remoteSpeakerVideoView
                                    : remoteSpeakerVideoViews) {
                                    callViewModel.bindRemoteViewRenderer(remoteSpeakerVideoView, participant, new Continuation<Unit>() {
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
                            }
                        }
                    }

                    return null;
                }, new Continuation<Unit>() {
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

    public void buildTimer() {
        cancelTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                second++;
                int minute = (second / 60);
                int hour = (minute / 60);
                if (hour != 0)
                    timeStr.postValue(repair0(hour) + ":" + repair0(minute) + ":" + repair0(second));
                else
                    timeStr.postValue(repair0(minute) + ":" + repair0(second));
            }
        }, 0, 1000);
    }

    private String repair0(int v) {
        return v < 10 ? ("0" + v) : (v + "");
    }

    private void cancelTimer() {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
    }

    public void signalingHungUp(SignalingInfo signalingInfo) {
        if (!isStartCall) {
            signalingCancel(signalingInfo);
            return;
        }
        OpenIMClient.getInstance().signalingManager.signalingHungUp(callBackDismissUI, signalingInfo);
    }

    private void dismissUI() {
        if (null != dismissListener)
            dismissListener.onDismiss(null);
    }

    private void signalingCancel(SignalingInfo signalingInfo) {
        if (isCallOut)
            OpenIMClient.getInstance().signalingManager.signalingCancel(callBackDismissUI, signalingInfo);
        else
            OpenIMClient.getInstance().signalingManager.signalingReject(callBackDismissUI, signalingInfo);
    }

    public void signalingAccept(SignalingInfo signalingInfo, OnBase onBase) {
        OpenIMClient.getInstance().signalingManager.signalingAccept(new OnBase<SignalingCertificate>() {
            @Override
            public void onError(int code, String error) {
                L.e(CallingServiceImp.TAG, error + code);
            }

            @Override
            public void onSuccess(SignalingCertificate data) {
                L.e(CallingServiceImp.TAG, data.getToken());
                MediaPlayerUtil.INSTANCE.pause();
                MediaPlayerUtil.INSTANCE.release();

                isStartCall = true;
                onBase.onSuccess(null);
                connectToRoom(data);
                buildTimer();
            }
        }, signalingInfo);
    }

    public void unBindView() {
        try {
            callViewModel.onCleared();
            cancelTimer();
            if (null != localVideoTrack)
                localVideoTrack.removeRenderer(localSpeakerVideoView);

            for (TextureViewRenderer textureViewRenderer : remoteSpeakerVideoViews) {
                Object videoTask = textureViewRenderer.getTag();
                if (null != videoTask) {
                    ((VideoTrack) videoTask).removeRenderer(textureViewRenderer);
                }
            }
            L.e("unBindView");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    interface OnParticipantsChangeListener {
        void onChange(List<Participant> participants);
    }
}
