package io.openim.android.demo;

import android.content.Intent;
import android.util.Log;

import androidx.multidex.MultiDex;


import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSONArray;
import com.cretin.www.cretinautoupdatelibrary.model.TypeConfig;
import com.cretin.www.cretinautoupdatelibrary.model.UpdateConfig;
import com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils;
import com.igexin.sdk.PushManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IM;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.net.RXRetrofit.HttpConfig;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.update.UpdateApp;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.UserLogic;
import io.openim.android.ouicore.voice.SPlayer;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.listener.OnAdvanceMsgListener;
import io.openim.android.sdk.listener.OnConnListener;
import io.openim.android.sdk.listener.OnSignalingListener;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.SignalingInvitationInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DemoApplication extends BaseApp {
    private static final String TAG = BaseApp.class.getSimpleName();


    @Override
    public void onCreate() {
        L.e(TAG, "-----onCreate------");
        super.onCreate();
        MultiDex.install(this);

        initFile();
        initARouter();
        initController();
        initNet();
        initBugly();
        initPush();
        initIM();



        EmojiManager.install(new GoogleEmojiProvider());
        //音频播放
        SPlayer.init(this);
        SPlayer.instance().setCacheDirPath(Constants.AUDIO_DIR);
    }

    private void initFile() {
        buildDirectory(Constants.AUDIO_DIR);
        buildDirectory(Constants.VIDEO_DIR);
        buildDirectory(Constants.PICTURE_DIR);
        buildDirectory(Constants.File_DIR);
    }

    private boolean buildDirectory(String path) {
        File file = new File(path);
        if (file.exists())
            return true;
        return file.mkdirs();
    }

    private void initARouter() {
        ARouter.init(this);
//        if (L.isDebug){
//            ARouter.openLog();
//            ARouter.openDebug();
//        }
    }

    private void initBugly() {
        CrashReport.setAppChannel(this,Common.isApkDebug() ? "debug" : "release");
        CrashReport.initCrashReport(getApplicationContext(), "4d365d80d1", L.isDebug);

//        new UpdateApp().init(R.mipmap.ic_launcher).checkUpdate(BaseApp.inst());
    }


    private void initController() {
        Easy.installVM(UserLogic.class);
    }


    private void initPush() {
        PushManager.getInstance().initialize(this);
        PushManager.getInstance().setDebugLogger(this, s -> L.i("getui", s));
    }

    private void initNet() {
        N.init(new HttpConfig().setBaseUrl(Constants.getAppAuthUrl()).addInterceptor(chain -> {
            String token = "";
            try {
                token = BaseApp.inst().loginCertificate.chatToken;
            } catch (Exception ignored) {
            }
            Request request = chain.request().newBuilder().addHeader("token", token).addHeader(
                "operationID", System.currentTimeMillis() + "").build();
            Response response = chain.proceed(request);
            return response;
        }));
    }

    private void initIM() {
        IM.initSdk(this);
        listenerIMOffline();
        CallingService callingService= (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
        if (null!=callingService){
            IMEvent.getInstance().addAdvanceMsgListener(new OnAdvanceMsgListener() {
                @Override
                public void onRecvNewMessage(Message msg) {
                    if (msg.getContentType() == MessageType.CUSTOM) {
                        Map map = JSONArray.parseObject(msg.getCustomElem().getData(), Map.class);
                        if (map.containsKey(Constants.K_CUSTOM_TYPE)) {
                            int customType = (int) map.get(Constants.K_CUSTOM_TYPE);
                            Object result = map.get(Constants.K_DATA);

                            if (customType >= Constants.MsgType.callingInvite
                                && customType<=Constants.MsgType.callingHungup ) {
                                SignalingInvitationInfo signalingInvitationInfo = GsonHel.fromJson((String) result, SignalingInvitationInfo.class);
                                SignalingInfo signalingInfo=new SignalingInfo();
                                signalingInfo.setInvitation(signalingInvitationInfo);

                                switch (customType) {
                                    case Constants.MsgType.callingInvite:
                                        callingService.onReceiveNewInvitation(signalingInfo);
                                        break;
                                    case Constants.MsgType.callingAccept:
                                        callingService.onInviteeAccepted(signalingInfo);
                                        break;
                                    case Constants.MsgType.callingReject:
                                        callingService.onInviteeRejected(signalingInfo);
                                        break;
                                    case Constants.MsgType.callingCancel:
                                        callingService.onInvitationCancelled(signalingInfo);
                                        break;
                                    case Constants.MsgType.callingHungup:
                                        callingService.onHangup(signalingInfo);
                                        break;
                                }
                            }
                        }
                    }
                }
            });
        }

    }

    private void listenerIMOffline() {
        IMEvent.getInstance().addConnListener(new OnConnListener() {
            @Override
            public void onConnectFailed(long code, String error) {

            }

            @Override
            public void onConnectSuccess() {

            }

            @Override
            public void onConnecting() {

            }

            @Override
            public void onKickedOffline() {
                offline();
            }

            @Override
            public void onUserTokenExpired() {
                offline();
            }


        });
    }

    public void offline() {
        LoginCertificate.clear();
        CallingService callingService =
            (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
        if (null != callingService) callingService.stopAudioVideoService(BaseApp.inst());

        ActivityManager.finishAllExceptActivity();
        startActivity(new Intent(BaseApp.inst(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
