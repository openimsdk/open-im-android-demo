package io.openim.android.ouicalling;


import android.content.Context;

import android.content.res.AssetFileDescriptor;
import android.os.Build;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.openim.android.ouicalling.databinding.DialogCallBinding;
import io.openim.android.ouicalling.vm.CallingVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.MediaPlayerListener;
import io.openim.android.ouicore.utils.MediaPlayerUtil;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;


public class CallDialog extends BaseDialog {

    protected Context context;
    private DialogCallBinding view;
    public CallingVM callingVM;
    protected SignalingInfo signalingInfo;


    public CallDialog(@NonNull Context context, CallingService callingService) {
        this(context, callingService, false);
    }

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
        callingVM = new CallingVM(callingService, isCallOut);
        callingVM.setDismissListener(v -> {
            dismiss();
        });
        initView();
        initRendererView();
    }

    public void initRendererView() {
        callingVM.initLocalSpeakerVideoView(view.localSpeakerVideoView);
        callingVM.initRemoteVideoRenderer(view.remoteSpeakerVideoView,
            view.remoteSpeakerVideoView2);
    }

    private void initView() {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        window.setAttributes(params);

        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        view.cameraControl.setVisibility(callingVM.isVideoCalls ? View.GONE : View.VISIBLE);
    }

    //收起/展开
    public void shrink(boolean isShrink) {
        view.home.setVisibility(isShrink ? View.GONE : View.VISIBLE);
        getWindow().setDimAmount(isShrink ? 0f : 1f);
        view.shrink.setVisibility(isShrink ? View.VISIBLE : View.GONE);

        view.waiting.setVisibility(callingVM.isVideoCalls
            ? View.GONE : View.VISIBLE);
        if (callingVM.isStartCall &&!callingVM.isVideoCalls){
            view.sTips.setText(io.openim.android.ouicore.R.string.calling);
        }else {
            view.sTips.setText(callingVM.isCallOut ?
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

    public void bindData(SignalingInfo signalingInfo) {
        this.signalingInfo = signalingInfo;
        callingVM.isGroup =
            signalingInfo.getInvitation().getSessionType() != ConversationType.SINGLE_CHAT;
        callingVM.setVideoCalls(Constant.MediaType.VIDEO.equals(signalingInfo.getInvitation().getMediaType()));
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
                    view.sAvatar.load(userInfo.getFaceURL());
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
        callingVM.timeStr.observeForever(bindTime);
        view.closeCamera.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean isEnabled = !isChecked;
            callingVM.callViewModel.setCameraEnabled(isEnabled);
            view.localSpeakerVideoView.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        });
        view.switchCamera.setOnClickListener(v -> {
            callingVM.callViewModel.flipCamera();
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
            callingVM.audioManager.setSpeakerphoneOn(isChecked);
        });

        view.hangUp.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                callingVM.renewalDB(signalingInfo.getInvitation().getCustomData(),
                    (realm, callHistory) -> callHistory.setDuration((int) (System.currentTimeMillis() - callHistory.getDate())));

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
                callingVM.signalingAccept(signalingInfo, new OnBase() {
                    @Override
                    public void onError(int code, String error) {

                    }

                    @Override
                    public void onSuccess(Object data) {
                        changeView();

                        callingVM.renewalDB(signalingInfo.getInvitation().getCustomData(),(realm, v)-> v.setSuccess(true));
                    }
                });
            }
        });
        view.zoomOut.setOnClickListener(v -> {
            shrink(true);
        });
        view.shrink.setOnClickListener(v -> {
            shrink(false);
        });
    }


    public void changeView() {
        view.headTips.setVisibility(View.GONE);
        view.ask.setVisibility(View.GONE);
        view.callingMenu.setVisibility(View.VISIBLE);
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
            AssetFileDescriptor assetFileDescriptor = BaseApp.inst().getAssets().openFd(
                "incoming_call_ring.mp3");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                MediaPlayerUtil.INSTANCE.initMedia(BaseApp.inst(), assetFileDescriptor);
            }
            MediaPlayerUtil.INSTANCE.prepare();
            MediaPlayerUtil.INSTANCE.setMediaListener(new MediaPlayerListener() {
                @Override
                public void finish() {
                    MediaPlayerUtil.INSTANCE.playMedia();
                }

                @Override
                public void onErr(int what) {

                }

                @Override
                public void prepare() {
                    MediaPlayerUtil.INSTANCE.playMedia();
                }
            });
            MediaPlayerUtil.INSTANCE.playMedia();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        try {
            MediaPlayerUtil.INSTANCE.pause();
            MediaPlayerUtil.INSTANCE.release();
            callingVM.audioManager.setSpeakerphoneOn(true);
            callingVM.timeStr.removeObserver(bindTime);
            callingVM.unBindView();
            insertChatHistory();
            super.dismiss();
            ((CallingServiceImp) callingVM.callingService).callDialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void insertChatHistory() {
        boolean isGroup = callingVM.isGroup;
        if (!isShowing()
            || isGroup
            || (null != signalingInfo
            && TextUtils.isEmpty(signalingInfo.getInvitation().getCustomData())))
            return;
        String id = signalingInfo.getInvitation().getCustomData();
        String senderID = isGroup ? BaseApp.inst().loginCertificate.userID :
            signalingInfo.getInvitation().getInviterUserID();
        String receiver = signalingInfo.getInvitation().getInviteeUserIDList().get(0);

        callingVM.renewalDB(id, (realm, callHistory) -> {
            callHistory = realm.copyFromRealm(callHistory);

            HashMap<String, Object> map = new HashMap<>();
            map.put(Constant.K_CUSTOM_TYPE, Constant.MsgType.LOCAL_CALL_HISTORY);
            map.put(Constant.K_RESULT, callHistory);

            String data = GsonHel.toJson(map);
            Message message = OpenIMClient.getInstance().messageManager.createCustomMessage(data,
                "", "");
            message.setRead(true);
            OpenIMClient.getInstance().messageManager.insertSingleMessageToLocalStorage(new IMUtil.IMCallBack<String>() {
                @Override
                public void onSuccess(String data) {
                    Obs.newMessage(Constant.Event.INSERT_MSG);
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
    }

}
