package io.openim.android.ouicalling;


import android.content.Context;

import android.content.res.AssetFileDescriptor;
import android.os.Build;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicalling.databinding.DialogCallBinding;
import io.openim.android.ouicalling.vm.CallingVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.utils.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.MediaPlayerListener;
import io.openim.android.ouicore.utils.MediaPlayerUtil;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;
import io.reactivex.Observable;


public class CallDialog extends BaseDialog {

    private Context context;
    private DialogCallBinding view;
    private CallingVM callingVM;


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
    public CallDialog(@NonNull Context context, CallingService callingService,
                      boolean isCallOut) {
        super(context);
        this.context = context;
        callingVM = new CallingVM(callingService, isCallOut);
        callingVM.setDismissListener(v -> {
            dismiss();
        });
        initView();
        callingVM.setLocalSpeakerVideoView(view.localSpeakerVideoView);
        callingVM.initRemoteVideoRenderer(view.remoteSpeakerVideoView);
    }

    private void initView() {
        Window window = getWindow();
        view = DialogCallBinding.inflate(getLayoutInflater());
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        callingVM.setVideoCalls("video".equals(signalingInfo.getInvitation()
            .getMediaType()));
        if (!callingVM.isVideoCalls) {
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

        try {
            ArrayList<String> ids = new ArrayList<>();
            ids.add(callingVM.isCallOut ? signalingInfo.getInvitation().getInviteeUserIDList().get(0)
                : signalingInfo.getInvitation().getInviterUserID());
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
                    //audio call
                    view.avatar2.load(userInfo.getFaceURL());
                    view.name2.setText(userInfo.getNickname());
                }
            }, ids);
        } catch (Exception e) {
            e.printStackTrace();
        }
        listener(signalingInfo);
    }

    private final Observer<String> bindTime = new Observer<String>() {
        @Override
        public void onChanged(String s) {
            if (TextUtils.isEmpty(s)) return;
            view.timeTv.setText(s);
            view.callingTips2.setText(s);
        }
    };

    private void listener(SignalingInfo signalingInfo) {
        callingVM.timeStr.observeForever(bindTime);
        view.micIsOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            view.micIsOn.setText(isChecked ? context.getString(io.openim.android.ouicore.R.string.microphone_on)
                : context.getString(io.openim.android.ouicore.R.string.microphone_off));
            //关闭麦克风
            callingVM.callViewModel.setMicEnabled(isChecked);
        });
        view.speakerIsOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            view.speakerIsOn.setText(isChecked ? context.getString(io.openim.android.ouicore.R.string.speaker_on)
                : context.getString(io.openim.android.ouicore.R.string.speaker_off));
            // 打开扬声器
            callingVM.audioManager.setSpeakerphoneOn(isChecked);
        });

        view.hangUp.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
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
                    }
                });
            }
        });
    }


    private void changeView() {
        view.headTips.setVisibility(View.GONE);
        view.ask.setVisibility(View.GONE);
        view.callingMenu.setVisibility(View.VISIBLE);
    }


    @Override
    public void show() {
        try {
            Common.wakeUp(context);
            AssetFileDescriptor assetFileDescriptor = BaseApp.inst().getAssets().openFd("incoming_call_ring.mp3");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                MediaPlayerUtil.INSTANCE.initMedia(BaseApp.inst(), assetFileDescriptor);
            }
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
                                                      }
            );
            MediaPlayerUtil.INSTANCE.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }

        super.show();
    }

    @Override
    public void dismiss() {
        try {
            MediaPlayerUtil.INSTANCE.pause();
            MediaPlayerUtil.INSTANCE.release();
            callingVM.audioManager.setSpeakerphoneOn(true);
            callingVM.timeStr.removeObserver(bindTime);
            callingVM.unBindView();
            ((CallingServiceImp) callingVM.callingService).callDialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.dismiss();
    }

    public void otherSideAccepted() {
        callingVM.isStartCall = true;
        callingVM.buildTimer();
        view.headTips.setVisibility(View.GONE);
        MediaPlayerUtil.INSTANCE.pause();
        MediaPlayerUtil.INSTANCE.release();
    }
}
