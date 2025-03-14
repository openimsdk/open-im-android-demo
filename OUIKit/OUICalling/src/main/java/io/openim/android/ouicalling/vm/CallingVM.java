package io.openim.android.ouicalling.vm;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.twilio.audioswitch.AudioDevice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.livekit.android.renderer.TextureViewRenderer;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.participant.RemoteParticipant;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicalling.CallingServiceImp;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.CallHistory;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.MediaPlayerUtil;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnMsgSendCallback;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.SignalingCertificate;
import io.openim.android.sdk.models.SignalingInfo;
import io.reactivex.functions.Function;
import io.realm.Realm;
import kotlin.Result;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.CoroutineScope;

public class CallingVM {
    public final CoroutineScope scope;
    //通话时间
    private Timer timer;
    private int second = 0;
    public MutableLiveData<String> timeStr = new MutableLiveData<>("");

    //获取音频服务
//    public AudioManager audioManager;
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
        scope = callViewModel.buildScope();
//        audioManager = (AudioManager) BaseApp.inst().getSystemService(Context.AUDIO_SERVICE);
        listenerBluetoothConnectionReceiver();
    }

    private void listenerBluetoothConnectionReceiver() {
        BluetoothConnectionReceiver audioNoisyReceiver = new BluetoothConnectionReceiver(this);
        //蓝牙状态广播监听
        IntentFilter audioFilter = new IntentFilter();
        audioFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        audioFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        BaseApp.inst().registerReceiver(audioNoisyReceiver, audioFilter);
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

    private OnMsgSendCallback callBackDismissUI = new OnMsgSendCallback() {
        @Override
        public void onError(int code, String error) {
            L.e(CallingServiceImp.TAG, error + "-" + code);
            dismissUI();
        }

        @Override
        public void onSuccess(Message data) {
            dismissUI();
        }
    };

    public void signalingInvite(SignalingInfo signalingInfo) {
        sendSignaling(Constants.MsgType.callingInvite, signalingInfo, new OnMsgSendCallback() {
            @Override
            public void onSuccess(Message s) {
                getTokenAndConnectRoom(signalingInfo, new OnBase<SignalingCertificate>() {
                    @Override
                    public void onSuccess(SignalingCertificate data) {
                        connectToRoom(data);
                    }
                });
            }
        });
    }

    private void sendSignaling(int code, SignalingInfo signalingInfo, OnMsgSendCallback onMsgSendCallback) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constants.K_CUSTOM_TYPE, code);
        hashMap.put(Constants.K_DATA, signalingInfo.getInvitation());
        Message message = OpenIMClient.getInstance().messageManager.createCustomMessage(GsonHel.toJson(hashMap), "", "");

        List<String> uidList = signalingInfo.getInvitation().getInviteeUserIDList();
        if (null != message && !uidList.isEmpty()) {
            String recvUid = signalingInfo.getInvitation().getInviterUserID().equals(BaseApp.inst().loginCertificate.userID) ? signalingInfo.getInvitation().getInviteeUserIDList().get(0) : signalingInfo.getInvitation().getInviterUserID();
            OpenIMClient.getInstance().messageManager.sendMessage(onMsgSendCallback, message, recvUid, null, new OfflinePushInfo(), true);
        }
    }

    private void getTokenAndConnectRoom(SignalingInfo signalingInfo, OnBase<SignalingCertificate> callBack) {
        Parameter parameter = new Parameter();
        parameter.add("room", signalingInfo.getInvitation().getRoomID());
        parameter.add("identity", BaseApp.inst().loginCertificate.userID);
        N.API(OneselfService.class).getTokenForRTC(parameter.buildJsonBody()).map(OneselfService.turn(HashMap.class)).map((Function<HashMap, SignalingCertificate>) responseBody -> {
            String serverUrl = (String) responseBody.get("serverUrl");
            String token = (String) responseBody.get("token");
            SignalingCertificate signalingCertificate = new SignalingCertificate();
            signalingCertificate.setLiveURL(serverUrl);
            signalingCertificate.setToken(token);
            return signalingCertificate;
        }).compose(N.IOMain()).subscribe(new NetObserver<SignalingCertificate>("") {
            @Override
            public void onSuccess(SignalingCertificate data) {
                if (null == data) return;
                L.e(CallingServiceImp.TAG, data.getToken());
                callBack.onSuccess(data);
            }

            @Override
            protected void onFailure(Throwable e) {
                callBack.onError(-1, e.getMessage());
            }
        });
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
                setSpeakerphoneOn(true);
                if (!isVideoCalls) callViewModel.setCameraEnabled(false);

                localVideoTrack = callViewModel.getVideoTrack(callViewModel.getRoom().getLocalParticipant());
                if (null != localVideoTrack && null != localSpeakerVideoViews && !localSpeakerVideoViews.isEmpty()) {
                    for (TextureViewRenderer localSpeakerVideoView : localSpeakerVideoViews) {
                        localVideoTrack.addRenderer(localSpeakerVideoView);
                        localSpeakerVideoView.setTag(localVideoTrack);
                    }
                }
                callViewModel.subscribe(callViewModel.getAllParticipants(), (v) -> {
                    if (v.isEmpty()) return null;
                    if (null != onParticipantsChangeListener) {
                        onParticipantsChangeListener.onChange(v);
                    } else {
                        for (int i = 0; i < v.size(); i++) {
                            Participant participant = v.get(i);
                            if (participant instanceof RemoteParticipant) {
                                for (TextureViewRenderer remoteSpeakerVideoView : remoteSpeakerVideoViews) {
                                    callViewModel.bindRemoteViewRenderer(remoteSpeakerVideoView, participant, scope, new Continuation<Unit>() {
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
                }, scope);
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
        Common.UIHandler.postDelayed(this::dismissUI, 18 * 1000);
        if (!isStartCall) {
            signalingCancel(signalingInfo);
            return;
        }
        sendSignaling(Constants.MsgType.callingHungup, signalingInfo, callBackDismissUI);
    }

    private void dismissUI() {
        if (null != dismissListener) dismissListener.onDismiss(null);
    }

    private void signalingCancel(SignalingInfo signalingInfo) {
        if (isCallOut) {
            renewalDB(buildPrimaryKey(signalingInfo), (realm, v) -> v.setFailedState(1));
            sendSignaling(Constants.MsgType.callingCancel, signalingInfo, callBackDismissUI);
        } else {
            renewalDB(buildPrimaryKey(signalingInfo), (realm, v) -> v.setFailedState(3));
            sendSignaling(Constants.MsgType.callingReject, signalingInfo, callBackDismissUI);
        }
    }

    public static String buildPrimaryKey(SignalingInfo signalingInfo) {
        return signalingInfo.getInvitation().getRoomID() + signalingInfo.getInvitation().getInitiateTime();
    }

    public void signalingAccept(SignalingInfo signalingInfo, OnBase onBase) {
        sendSignaling(Constants.MsgType.callingAccept, signalingInfo, new OnMsgSendCallback() {
            @Override
            public void onError(int code, String error) {
                OnMsgSendCallback.super.onError(code, error);
            }

            @Override
            public void onSuccess(Message s) {
                getTokenAndConnectRoom(signalingInfo, new OnBase<SignalingCertificate>() {
                    @Override
                    public void onError(int code, String error) {
                        Toast.makeText(BaseApp.inst(), "加入会议失败,服务器错误(" + error + ")", Toast.LENGTH_LONG).show();
                        L.e(CallingServiceImp.TAG, error + code);
                        dismissUI();
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
                });


            }
        });
    }

    public void unBindView() {
        try {
            cancelTimer();
            if (null != localVideoTrack) {
                if (null != localSpeakerVideoViews) {
                    for (TextureViewRenderer localSpeakerVideoView : localSpeakerVideoViews) {
                        localSpeakerVideoView.release();
                        localVideoTrack.removeRenderer(localSpeakerVideoView);
                    }
                }
            }
            for (TextureViewRenderer textureViewRenderer : remoteSpeakerVideoViews) {
                textureViewRenderer.release();
                Object videoTask = textureViewRenderer.getTag();
                if (null != videoTask) {
                    ((VideoTrack) videoTask).removeRenderer(textureViewRenderer);
                }
            }
            callViewModel.onCleared();
            L.e("unBindView");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSpeakerphoneOn(boolean isChecked) {
        if (callViewModel.getAudioHandler().getSelectedAudioDevice() instanceof AudioDevice.BluetoothHeadset) {
            return;
        }
        callViewModel.getAudioHandler().selectDevice(isChecked ? new AudioDevice.Speakerphone()
            : new AudioDevice.Earpiece());
    }


    public interface OnParticipantsChangeListener {
        void onChange(List<Participant> participants);
    }


    public void renewalDB(String id, OnRenewalDBListener onRenewalDBListener) {
        BaseApp.inst().realm.executeTransactionAsync(realm -> {
            CallHistory callHistory = realm.where(CallHistory.class).equalTo("id", id).findFirst();
            if (null == callHistory) return;
            onRenewalDBListener.onRenewal(realm, callHistory);
        });
    }


    public interface OnRenewalDBListener {
        void onRenewal(Realm realm, CallHistory callHistory);
    }


    public static class BluetoothConnectionReceiver extends BroadcastReceiver {
        CallingVM callingVM;

        public BluetoothConnectionReceiver(CallingVM callingVM) {
            this.callingVM = callingVM;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) { //蓝牙连接状态
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                if (state == BluetoothAdapter.STATE_CONNECTED) {
                    //连接或失联，切换音频输出（到蓝牙、或者强制仍然扬声器外放）
                    callingVM.changeToHeadset();
                } else if (state == BluetoothAdapter.STATE_DISCONNECTED) {
                    callingVM.changeToSpeaker();
                }
            }
        }
    }

    /**
     * 切换到外放
     */
    public void changeToSpeaker() {
        setSpeakerphoneOn(true);
    }

    /**
     * 切换到蓝牙音箱
     */
    public void changeToHeadset() {
        callViewModel.getAudioHandler().selectDevice(new AudioDevice.BluetoothHeadset());
    }

}
