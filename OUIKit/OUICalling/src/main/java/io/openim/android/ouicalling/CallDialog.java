package io.openim.android.ouicalling;


import android.content.Context;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;


import com.hjq.permissions.Permission;
import com.hjq.window.EasyWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.livekit.android.events.RoomEvent;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.participant.RemoteParticipant;
import io.livekit.android.room.track.RemoteVideoTrack;
import io.openim.android.ouicalling.databinding.DialogCallBinding;
import io.openim.android.ouicalling.databinding.LayoutFloatViewBinding;
import io.openim.android.ouicalling.vm.CallingVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.MediaPlayerUtil;
import io.openim.android.ouicore.utils.NotificationUtil;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.PublicUserInfo;
import io.openim.android.sdk.models.SignalingInfo;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;


public class CallDialog extends BaseDialog {

    protected final HasPermissions hasShoot, hasRecord, hasSystemAlert;
    protected Context context;
    private DialogCallBinding view;
    public CallingVM callingVM;
    protected SignalingInfo signalingInfo;

    protected EasyWindow easyWindow;
    protected LayoutFloatViewBinding floatViewBinding;
    private boolean isSubscribe;

    /**
     * 弹出通话界面
     *
     * @param context        上下文
     * @param callingService 通话服务
     * @param isCallOut      是否呼出
     */
    public CallDialog(@NonNull Context context, CallingService callingService, boolean isCallOut) {
        super(context);
        this.context = context;
        hasShoot = new HasPermissions(context, Permission.CAMERA, Permission.RECORD_AUDIO);
        hasRecord = new HasPermissions(context, Permission.RECORD_AUDIO);
        hasSystemAlert = new HasPermissions(context, Permission.SYSTEM_ALERT_WINDOW);

        callingVM = new CallingVM(callingService, isCallOut);
        callingVM.setDismissListener(v -> {
            dismiss();
        });
        callingVM.callViewModel.subscribe(callingVM.callViewModel.getRoom().getEvents().getEvents(), (v) -> {
            if (v instanceof RoomEvent.ParticipantDisconnected
                && v.getRoom().getRemoteParticipants().size() == 0) {
                //当只有1个人时关闭会议
                dismiss();
            }
            return null;
        }, callingVM.scope);

        initView();
        initRendererView();
    }

    public void initRendererView() {
        callingVM.initLocalSpeakerVideoView(view.localSpeakerVideoView);
        callingVM.initRemoteVideoRenderer(view.remoteSpeakerVideoView,
            view.remoteSpeakerVideoView2, floatViewBinding.shrinkRemoteSpeakerVideoView);
    }

    private void initView() {
        floatViewBinding = LayoutFloatViewBinding.inflate(getLayoutInflater());
        Window window = getWindow();
        view = DialogCallBinding.inflate(getLayoutInflater());
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(view.getRoot());
        //背景状态栏透明
        window.setDimAmount(1f);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        Common.addTypeSystemAlert(params);
        window.setAttributes(params);

        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        view.zoomOut.setVisibility(Common.isScreenLocked() ? View.GONE : View.VISIBLE);
    }


    //    收起/展开
    public void shrink(boolean isShrink) {
        if (isShrink) {
            showFloatView();
        } else if (null != easyWindow) {
            easyWindow.cancel();
        }
        view.home.setVisibility(isShrink ? View.GONE : View.VISIBLE);
        getWindow().setDimAmount(isShrink ? 0f : 1f);

        if (callingVM.isStartCall) {
            floatViewBinding.sTips.setText(io.openim.android.ouicore.R.string.calling);
        } else {
            floatViewBinding.sTips.setText(callingVM.isCallOut ?
                context.getString(io.openim.android.ouicore.R.string.waiting_tips2) :
                context.getString(io.openim.android.ouicore.R.string.waiting_tips3));
        }
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = isShrink ? ViewGroup.LayoutParams.WRAP_CONTENT :
            ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = isShrink ? ViewGroup.LayoutParams.WRAP_CONTENT :
            ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = isShrink ? (Gravity.TOP | Gravity.END) : Gravity.CENTER;
        getWindow().setAttributes(params);
    }

    protected void showFloatView() {
        // 传入 Activity 对象表示设置成局部的，不需要有悬浮窗权限
        // 传入 Application 对象表示设置成全局的，但需要有悬浮窗权限
        if (null == easyWindow) {
            easyWindow =
                new EasyWindow<>(BaseApp.inst()).setContentView(floatViewBinding.getRoot()).setGravity(Gravity.END | Gravity.TOP)
                    // 设置成可拖拽的
                    .setDraggable();
            floatViewBinding.shrink.setOnClickListener(v -> shrink(false));
        }
        if (!easyWindow.isShowing()) easyWindow.show();
    }

    public void bindData(SignalingInfo signalingInfo) {
        this.signalingInfo = signalingInfo;
        callingVM.isGroup =
            signalingInfo.getInvitation().getSessionType() != ConversationType.SINGLE_CHAT;
        callingVM.setVideoCalls(Constants.MediaType.VIDEO.equals(signalingInfo.getInvitation().getMediaType()));
        view.cameraControl.setVisibility(callingVM.isVideoCalls ? View.VISIBLE : View.GONE);
        if (!callingVM.isVideoCalls) {
            callingVM.callViewModel.setCameraEnabled(false);
            view.localSpeakerVideoView.setVisibility(View.GONE);
            view.timeTv.setVisibility(View.GONE);
            view.headTips.setVisibility(View.GONE);
            view.audioCall.setVisibility(View.VISIBLE);
        }
        view.micIsOn.setChecked(true);
        view.speakerIsOn.setChecked(true);
        if (callingVM.isCallOut) {
            view.callingMenu.setVisibility(View.VISIBLE);
            view.ask.setVisibility(View.GONE);

            view.callingTips.setText(context.getString(io.openim.android.ouicore.R.string.waiting_tips) + "...");
            view.callingTips2.setText(context.getString(io.openim.android.ouicore.R.string.waiting_tips) + "...");
            callingVM.signalingInvite(signalingInfo);
        } else {
            view.callingMenu.setVisibility(View.GONE);
            view.ask.setVisibility(View.VISIBLE);
        }
        bindUserInfo(signalingInfo);
        listener(signalingInfo);
    }

    /**
     * 绑定用户信息
     */
    public void bindUserInfo(SignalingInfo signalingInfo) {
        try {
            ArrayList<String> ids = new ArrayList<>();
            ids.add(callingVM.isCallOut ?
                signalingInfo.getInvitation().getInviteeUserIDList().get(0) :
                signalingInfo.getInvitation().getInviterUserID());

            OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<PublicUserInfo>>() {
                @Override
                public void onError(int code, String error) {
                    Toast.makeText(context, error + code, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(List<PublicUserInfo> data) {
                    if (data.isEmpty()) return;
                    PublicUserInfo userInfo = data.get(0);
                    view.avatar.load(userInfo.getFaceURL());
                    floatViewBinding.sAvatar.load(userInfo.getFaceURL(), userInfo.getNickname());
                    view.name.setText(userInfo.getNickname());

                    //audio call
                    view.avatar2.load(userInfo.getFaceURL());
                    view.name2.setText(userInfo.getNickname());
                }
            }, ids);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final Observer<String> bindTime = new Observer<String>() {
        @Override
        public void onChanged(String s) {
            if (TextUtils.isEmpty(s)) return;
            view.timeTv.setText(s);
            view.callingTips2.setText(s);
        }
    };

    public void listener(SignalingInfo signalingInfo) {
        callingVM.callViewModel.subscribe(callingVM.callViewModel.getRemoteParticipants(), (v) -> {
            if (isSubscribe) return null;
            Object[] toArray = v.values().toArray();
            if (toArray.length == 0) return null;
            callingVM.callViewModel.subscribe(((RemoteParticipant) toArray[0]).getEvents().getEvents(), (event) -> {
                isSubscribe = true;
                view.remoteSpeakerVideoView.setVisibility(event.getParticipant().isCameraEnabled() ? View.VISIBLE : View.GONE);
                return null;
            }, callingVM.scope);
            return null;
        }, callingVM.scope);

        callingVM.timeStr.observeForever(bindTime);
        view.closeCamera.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hasShoot.safeGo(() -> {
                boolean isEnabled = !isChecked;
                callingVM.callViewModel.setCameraEnabled(isEnabled);
                view.localSpeakerVideoView.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
            });
        });
        view.switchCamera.setOnClickListener(new OnDedrepClickListener(1000) {
            @Override
            public void click(View v) {
                callingVM.callViewModel.flipCamera();
            }
        });
        view.micIsOn.setOnClickListener(new OnDedrepClickListener(1000) {
            @Override
            public void click(View v) {
                view.micIsOn.setText(view.micIsOn.isChecked() ?
                    context.getString(io.openim.android.ouicore.R.string.microphone_on) :
                    context.getString(io.openim.android.ouicore.R.string.microphone_off));
                //关闭麦克风
                callingVM.callViewModel.setMicEnabled(view.micIsOn.isChecked());
            }
        });

        view.speakerIsOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            view.speakerIsOn.setText(isChecked ?
                context.getString(io.openim.android.ouicore.R.string.speaker_on) :
                context.getString(io.openim.android.ouicore.R.string.speaker_off));
            // 打开扬声器
            callingVM.setSpeakerphoneOn(isChecked);
        });

        view.hangUp.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                callingVM.renewalDB(callingVM.buildPrimaryKey(signalingInfo), (realm,
                                                                               callHistory) -> callHistory.setDuration((int) (System.currentTimeMillis() - callHistory.getDate())));

                callingVM.signalingHungUp(signalingInfo);
            }
        });
        view.reject.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                callingVM.signalingHungUp(signalingInfo);
            }
        });
        view.answer.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                answerClick(signalingInfo);
            }
        });
        view.zoomOut.setOnClickListener(v -> {
            zoomOutClick();
        });
        view.shrink.setOnClickListener(v -> {
            shrink(false);
        });
        view.localSpeakerVideoView.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                Object object = view.remoteSpeakerVideoView.getTag();
                if ( null != object) {
                    Participant participant = object instanceof RemoteVideoTrack
                        ? (Participant) callingVM.callViewModel.getRoom().getLocalParticipant()
                        : (Participant) callingVM.callViewModel.getSingleRemotePar();
                    Participant participant2 = object instanceof RemoteVideoTrack
                        ? (Participant)  callingVM.callViewModel.getSingleRemotePar()
                        : (Participant) callingVM.callViewModel.getRoom().getLocalParticipant();
                       if (null==participant2||null==participant)return;

                    callingVM.callViewModel.bindRemoteViewRenderer(view.localSpeakerVideoView,
                        participant2, callingVM.scope, new Continuation<Unit>() {
                            @NonNull
                            @Override
                            public CoroutineContext getContext() {
                                return null;
                            }

                            @Override
                            public void resumeWith(@NonNull Object o) {

                            }
                        });
                    callingVM.callViewModel.bindRemoteViewRenderer(view.remoteSpeakerVideoView,
                        participant, callingVM.scope, new Continuation<Unit>() {
                            @NonNull
                            @Override
                            public CoroutineContext getContext() {
                                return null;
                            }

                            @Override
                            public void resumeWith(@NonNull Object o) {

                            }
                        });

                }

            }
        });
    }

    public void zoomOutClick() {
        hasSystemAlert.safeGo(() -> shrink(true));
    }

    public void answerClick(SignalingInfo signalingInfo) {
        if (callingVM.isVideoCalls) {
            hasShoot.safeGo(() -> signalingAccept(signalingInfo));
        } else {
            hasRecord.safeGo(() -> signalingAccept(signalingInfo));
        }
    }

    public void signalingAccept(SignalingInfo signalingInfo) {
        callingVM.signalingAccept(signalingInfo, new OnBase() {
            @Override
            public void onError(int code, String error) {
            }

            @Override
            public void onSuccess(Object data) {
                changeView();

                callingVM.renewalDB(CallingVM.buildPrimaryKey(signalingInfo),
                    (realm, v1) -> v1.setSuccess(true));
            }
        });
    }


    public void changeView() {
        view.headTips.setVisibility(View.GONE);
        view.ask.setVisibility(View.GONE);
        view.callingMenu.setVisibility(View.VISIBLE);
        view.cameraControl.setVisibility(callingVM.isVideoCalls ? View.VISIBLE : View.GONE);

        waitingHandle();
    }


    @Override
    public void show() {
        playRingtone();
        super.show();
    }

    public void playRingtone() {
        try {
            Common.wakeUp(context);
//           Ringtone铃声
            if (!MediaPlayerUtil.INSTANCE.isPlaying()) {
                MediaPlayerUtil.INSTANCE.initMedia(BaseApp.inst(), R.raw.incoming_call_ring);
                MediaPlayerUtil.INSTANCE.loopPlay();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        try {
            if (null != easyWindow) {
                easyWindow.cancel();
            }
            insertChatHistory();
            MediaPlayerUtil.INSTANCE.pause();
            MediaPlayerUtil.INSTANCE.release();
            callingVM.setSpeakerphoneOn(true);
            callingVM.timeStr.removeObserver(bindTime);
            videoViewRelease();
            callingVM.unBindView();
            super.dismiss();
            ((CallingServiceImp) callingVM.callingService).callDialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void videoViewRelease() {
        view.localSpeakerVideoView.release();
        view.remoteSpeakerVideoView.release();
        view.remoteSpeakerVideoView2.release();
        floatViewBinding.shrinkRemoteSpeakerVideoView.release();
    }

    private void insertChatHistory() {
        boolean isGroup = callingVM.isGroup;
        if (!isShowing() || isGroup || (null != signalingInfo && TextUtils.isEmpty(callingVM.buildPrimaryKey(signalingInfo))))
            return;
        String id = callingVM.buildPrimaryKey(signalingInfo);
        String senderID = isGroup ? BaseApp.inst().loginCertificate.userID :
            signalingInfo.getInvitation().getInviterUserID();
        String receiver = signalingInfo.getInvitation().getInviteeUserIDList().get(0);

        callingVM.renewalDB(id, (realm, callHistory) -> {
            callHistory = realm.copyFromRealm(callHistory);
            try {
                callHistory.setDuration((int)(System.currentTimeMillis() - callHistory.getDate()));
            } catch (Exception e){}

            HashMap<String, Object> map = new HashMap<>();
            map.put(Constants.K_CUSTOM_TYPE, Constants.MsgType.LOCAL_CALL_HISTORY);
            map.put(Constants.K_DATA, callHistory);

            String data = GsonHel.toJson(map);
            Message message = OpenIMClient.getInstance().messageManager.createCustomMessage(data,
                "", "");
            message.setRead(true);
            OpenIMClient.getInstance().messageManager.insertSingleMessageToLocalStorage(new IMUtil.IMCallBack<String>() {
                @Override
                public void onSuccess(String data) {
                    Obs.newMessage(Constants.Event.INSERT_MSG);
                }
            }, message, receiver, senderID);
        });
    }


    public void otherSideAccepted() {
        callingVM.isStartCall = true;
        callingVM.buildTimer();
        view.headTips.setVisibility(View.GONE);
        MediaPlayerUtil.INSTANCE.pause();
        MediaPlayerUtil.INSTANCE.release();

        waitingHandle();
    }

    public String buildPrimaryKey() {
        return CallingVM.buildPrimaryKey(signalingInfo);
    }

    private void waitingHandle() {
        if (callingVM.isVideoCalls) floatViewBinding.waiting.setVisibility(View.GONE);

        if (callingVM.isStartCall) {
            floatViewBinding.sTips.setText(io.openim.android.ouicore.R.string.calling);
        } else {
            floatViewBinding.sTips.setText(callingVM.isCallOut ?
                context.getString(io.openim.android.ouicore.R.string.waiting_tips2) :
                context.getString(io.openim.android.ouicore.R.string.waiting_tips3));
        }
    }
}
