package io.openim.android.ouicalling;


import android.app.Application;
import android.content.Context;

import android.os.Build;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.participant.RemoteParticipant;
import io.livekit.android.room.track.VideoTrack;
import io.livekit.android.room.track.video.ViewVisibility;
import io.openim.android.ouicalling.databinding.DialogCallBinding;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.SignalingCertificate;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.flow.FlowCollector;


public class CallDialog extends BaseDialog {


    private final CallViewModel callingVM;
    private Context context;
    private DialogCallBinding view;
    private VideoTrack localVideoTrack, remoteVideoTrack;

    public CallDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
        callingVM = new CallViewModel((Application) context);
        callingVM.getRoom().initVideoRenderer(view.localSpeakerVideoView);
        callingVM.getRoom().initVideoRenderer(view.remoteSpeakerVideoView);
    }

    private void initView() {
        Window window = getWindow();
        view = DialogCallBinding.inflate(getLayoutInflater());
        window.requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(view.getRoot());
        //状态栏透明
        window.setDimAmount(1f);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        window.setBackgroundDrawableResource(android.R.color.transparent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        window.setAttributes(params);
    }

    public void bindData(SignalingInfo signalingInfo) {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(signalingInfo.getInvitation().getInviterUserID());
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                Toast.makeText(context, error + code, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;
                UserInfo userInfo = data.get(0);
                view.avatar.load(userInfo.getFaceURL());
                view.name.setText(userInfo.getNickname());
            }
        }, ids);
        listener(signalingInfo);
    }

    private void listener(SignalingInfo signalingInfo) {
        view.hangUp.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                OpenIMClient.getInstance().signalingManager.signalingReject(new OnBase<SignalingCertificate>() {
                    @Override
                    public void onError(int code, String error) {
                        dismiss();
                    }

                    @Override
                    public void onSuccess(SignalingCertificate data) {
                        dismiss();
                    }
                }, signalingInfo);
            }
        });
        view.answer.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                accept(signalingInfo);
            }
        });
    }

    private void accept(SignalingInfo signalingInfo) {
        OpenIMClient.getInstance().signalingManager.signalingAccept(new OnBase<SignalingCertificate>() {
            @Override
            public void onError(int code, String error) {
                L.e(CallingServiceImp.TAG, error + code);
            }

            @Override
            public void onSuccess(SignalingCertificate data) {
                L.e(CallingServiceImp.TAG, data.getToken());
                callingVM.connectToRoom(data.getLiveURL(), data.getToken(), new Continuation<Unit>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(@NonNull Object o) {
                        callingVM.getParticipants().collect((participants, continuation) -> {

                            for (int i = 0; i < participants.size(); i++) {
                                Participant participant = participants.get(i);
                                VideoTrack videoTrack = callingVM.getVideoTrack(participant);
                                if (null != videoTrack) {
                                    videoTrack.addRenderer(i == 0 ? view.localSpeakerVideoView : view.remoteSpeakerVideoView);
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
//                        localVideoTrack = callingVM.getVideoTrack(callingVM.getRoom().getLocalParticipant());
//                        Map<String, RemoteParticipant> remoteParticipants = callingVM.getRoom().getRemoteParticipants();
//                        if (!remoteParticipants.isEmpty()){
//                            Collection<RemoteParticipant> values=remoteParticipants.values();
//                            remoteVideoTrack = callingVM.getVideoTrack(((RemoteParticipant)values.toArray()[0]));
//                        }
//                        if (null != localVideoTrack)
//                            localVideoTrack.addRenderer(view.localSpeakerVideoView);
//                        if (null != remoteVideoTrack)
//                            remoteVideoTrack.addRenderer(view.remoteSpeakerVideoView);
                    }
                });
            }
        }, signalingInfo);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        if (null != localVideoTrack)
            localVideoTrack.removeRenderer(view.localSpeakerVideoView);

        super.dismiss();
    }
}
