package io.openim.android.ouicalling.vm;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

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
import io.openim.android.ouicore.entity.CallHistory;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
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
import kotlinx.coroutines.flow.FlowCollector;

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

    private List<TextureViewRenderer> remoteSpeakerVideoViews, localSpeakerVideoViews;


    public CallingVM(CallingService callingService, boolean isCallOut) {
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

    public void initLocalSpeakerVideoView(TextureViewRenderer... viewRenderers) {
        localSpeakerVideoViews = Arrays.asList(viewRenderers);
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
        OnBase<SignalingCertificate> certificateOnBase = new OnBase<SignalingCertificate>() {
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
        };
        if (isGroup)
            OpenIMClient.getInstance().signalingManager.signalingInviteInGroup(certificateOnBase,
                signalingInfo);
        else
            OpenIMClient.getInstance().signalingManager.signalingInvite(certificateOnBase,
                signalingInfo);
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
                if (!isVideoCalls) callViewModel.setCameraEnabled(false);

                localVideoTrack =
                    callViewModel.getVideoTrack(callViewModel.getRoom().getLocalParticipant());
                if (null != localVideoTrack && null != localSpeakerVideoViews && !localSpeakerVideoViews.isEmpty()) {
                    for (TextureViewRenderer localSpeakerVideoView : localSpeakerVideoViews) {
                        localVideoTrack.addRenderer(localSpeakerVideoView);
                    }
                }

                callViewModel.getParticipants().collect((participants, continuation) -> {
                    if (participants.isEmpty()) return null;

                    if (null != onParticipantsChangeListener) {
                        Common.UIHandler.post(() -> onParticipantsChangeListener.onChange(participants));
                    } else {
                        for (int i = 0; i < participants.size(); i++) {
                            Participant participant = participants.get(i);
                            if (participant instanceof RemoteParticipant) {
                                for (TextureViewRenderer remoteSpeakerVideoView :
                                    remoteSpeakerVideoViews) {
                                    callViewModel.bindRemoteViewRenderer(remoteSpeakerVideoView,
                                        participant, new Continuation<Unit>() {
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
                String secondFormat = TimeUtil.secondFormat(second, TimeUtil.secondFormat);
                if (secondFormat.length() <= 2) secondFormat = "00:" + secondFormat;
                timeStr.postValue(secondFormat);
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
        OpenIMClient.getInstance().signalingManager.signalingHungUp(callBackDismissUI,
            signalingInfo);
    }

    private void dismissUI() {
        if (null != dismissListener) dismissListener.onDismiss(null);
    }

    private void signalingCancel(SignalingInfo signalingInfo) {
        if (isCallOut)
            OpenIMClient.getInstance().signalingManager.signalingCancel(new OnBase<SignalingCertificate>() {
                @Override
                public void onError(int code, String error) {
                    L.e(CallingServiceImp.TAG, error + "-" + code);
                    dismissUI();
                }

                @Override
                public void onSuccess(SignalingCertificate data) {
                    renewalDB(signalingInfo.getInvitation().getRoomID(), v -> v.setFailedState(1));
                    dismissUI();
                }
            }, signalingInfo);
        else
            OpenIMClient.getInstance().signalingManager.signalingReject(callBackDismissUI,
                signalingInfo);
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
            if (null != localVideoTrack) {
                localVideoTrack.stop();
                if (null != localSpeakerVideoViews) {
                    for (TextureViewRenderer localSpeakerVideoView : localSpeakerVideoViews) {
                        localVideoTrack.removeRenderer(localSpeakerVideoView);
                    }
                }
            }
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


    public interface OnParticipantsChangeListener {
        void onChange(List<Participant> participants);
    }


    public void renewalDB(String roomID, OnRenewalDBListener onRenewalDBListener) {
        BaseApp.inst().realm.executeTransactionAsync(realm -> {
            CallHistory callHistory =
                realm.where(CallHistory.class).equalTo("roomID", roomID).findFirst();
            if (null == callHistory) return;
            onRenewalDBListener.onRenewal(callHistory);
        });
    }

    public interface OnRenewalDBListener {
        void onRenewal(CallHistory callHistory);
    }
}
