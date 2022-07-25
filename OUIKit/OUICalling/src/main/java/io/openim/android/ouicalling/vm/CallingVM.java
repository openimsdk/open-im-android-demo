package io.openim.android.ouicalling.vm;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;

import org.webrtc.VideoSink;

import java.util.Arrays;
import java.util.List;

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
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.SignalingCertificate;
import io.openim.android.sdk.models.SignalingInfo;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class CallingVM {
    //获取音频服务
    public AudioManager audioManager;
    private DialogInterface.OnDismissListener dismissListener;
    private final CallViewModel callViewModel;
    private VideoTrack localVideoTrack;
    private CallingService callingService;
    //是否是视频通话
    public boolean isVideoCalls;
    //已连接
    public boolean isConnected;
    //呼出
    public boolean isCallOut;

    private TextureViewRenderer localSpeakerVideoView;
    private List<TextureViewRenderer> remoteSpeakerVideoViews;


    public CallingVM(CallingService callingService, boolean isVideoCalls) {
        this(callingService, isVideoCalls, false);
    }

    public CallingVM(CallingService callingService, boolean isVideoCalls,
                     boolean isCallOut) {
        this.callingService = callingService;
        this.isVideoCalls = isVideoCalls;
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


    public void setDismissListener(DialogInterface.OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public void setLocalSpeakerVideoView(TextureViewRenderer localSpeakerVideoView) {
        this.localSpeakerVideoView = localSpeakerVideoView;
        callViewModel.getRoom().initVideoRenderer(localSpeakerVideoView);
    }

    public void signalingInvite(SignalingInfo signalingInfo) {
        OpenIMClient.getInstance().signalingManager.signalingInvite(new OnBase<SignalingCertificate>() {
            @Override
            public void onError(int code, String error) {
                dismissUI();
            }

            @Override
            public void onSuccess(SignalingCertificate data) {
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
                localVideoTrack = callViewModel.getVideoTrack(callViewModel.getRoom().getLocalParticipant());
                if (null != localVideoTrack && null != localSpeakerVideoView)
                    localVideoTrack.addRenderer(localSpeakerVideoView);

                callViewModel.getParticipants().collect((participants, continuation) -> {
                    if (participants.isEmpty()) return null;
                    if (participants.size() == 1 && !isCallOut) {
                        callingService.onHangup(null);
                        return null;
                    }
                    for (int i = 0; i < participants.size(); i++) {
                        Participant participant = participants.get(i);
                        if (participant instanceof RemoteParticipant) {
                            callViewModel.bindRemoteViewRenderer(remoteSpeakerVideoViews.get(0), participant, new Continuation<Unit>() {
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

    public void signalingHungUp(SignalingInfo signalingInfo) {
        if (!isConnected) {
            signalingCancel(signalingInfo);
            return;
        }
        OpenIMClient.getInstance().signalingManager.signalingHungUp(new OnBase<SignalingCertificate>() {
            @Override
            public void onError(int code, String error) {
                dismissUI();
            }

            @Override
            public void onSuccess(SignalingCertificate data) {
                dismissUI();
            }
        }, signalingInfo);
    }

    private void dismissUI() {
        if (null != dismissListener)
            dismissListener.onDismiss(null);
    }

    private void signalingCancel(SignalingInfo signalingInfo) {
        if (isCallOut)
            OpenIMClient.getInstance().signalingManager.signalingCancel(new OnBase<SignalingCertificate>() {
                @Override
                public void onError(int code, String error) {
                    dismissUI();
                }

                @Override
                public void onSuccess(SignalingCertificate data) {
                    dismissUI();
                }
            }, signalingInfo);
        else
            OpenIMClient.getInstance().signalingManager.signalingReject(new OnBase<SignalingCertificate>() {
                @Override
                public void onError(int code, String error) {
                    dismissUI();
                }

                @Override
                public void onSuccess(SignalingCertificate data) {
                    dismissUI();
                }
            }, signalingInfo);
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


                onBase.onSuccess(null);
                connectToRoom(data);
            }
        }, signalingInfo);
    }

    public void unBindView() {
        try {
            if (null != localVideoTrack)
                localVideoTrack.removeRenderer(localSpeakerVideoView);

            for (TextureViewRenderer textureViewRenderer : remoteSpeakerVideoViews) {
                Object videoTask = textureViewRenderer.getTag();
                if (null != videoTask) {
                    ((VideoTrack) videoTask).removeRenderer(textureViewRenderer);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
