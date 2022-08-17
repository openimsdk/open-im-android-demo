package io.openim.android.ouicalling.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouicalling.CallingServiceImp;
import io.openim.android.ouicalling.JumpReceiver;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;

public class AudioVideoService extends Service {

    private static final int NOTIFY_ID = 1000;
    private static final String TAG = "AudioVideoService-----";

    protected void showNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent hangIntent = new Intent(this, JumpReceiver.class);
        PendingIntent hangPendingIntent = PendingIntent.getBroadcast(this, 1002,
            hangIntent, PendingIntent.FLAG_MUTABLE);

        String CHANNEL_ID = "AudioVideoService";
        String CHANNEL_NAME = getString(io.openim.android.ouicore.R.string.audio_video_service);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(io.openim.android.ouicore.R.string.audio_video_service_tips1))
            .setContentText(getString(io.openim.android.ouicore.R.string.audio_video_service_tips2))
            .setSmallIcon(io.openim.android.ouicore.R.mipmap.ic_logo)
            .setContentIntent(hangPendingIntent)
            .setAutoCancel(true)
            .build();

        //Android 8.0 以上需包添加渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(notificationChannel);
        }

        manager.notify(NOTIFY_ID, notification);
        startForeground(NOTIFY_ID, notification);
        CallingServiceImp.launchAlarm(this);
    }

    public class sIBinder extends Binder {
        public AudioVideoService getService() {
            return AudioVideoService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        L.e(TAG, "onBind");
        showNotification();
        CallingService callingService = (CallingService) ARouter.getInstance()
            .build(Routes.Service.CALLING).navigation();
        if (null != callingService)
            OpenIMClient.getInstance().signalingManager.setSignalingListener(callingService);
        return new sIBinder();
    }

    @Override
    public void onCreate() {
        L.e(TAG, "onCreate");
        super.onCreate();
        CallingService callingService = (CallingService) ARouter.getInstance()
            .build(Routes.Service.CALLING).navigation();
        OpenIMClient.getInstance().signalingManager.setSignalingListener(callingService);

        loginOpenIM(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                L.e(TAG, "login -----" + error + code);
            }

            @Override
            public void onSuccess(String data) {
                L.e(TAG, "login -----onSuccess");
                CallingService.OnServicePriorLoginCallBack callBack = callingService.getOnServicePriorLoginCallBack();
                if (null != callBack) callBack.onLogin();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.e(TAG, "onStartCommand");
        if (IMUtil.isLogged()) {
            showNotification();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void loginOpenIM(OnBase<String> stringOnBase) {
        if (IMUtil.isLogged()) return;

        BaseApp.inst().loginCertificate = LoginCertificate.getCache(BaseApp.inst());
        if (null != BaseApp.inst().loginCertificate) {
            OpenIMClient.getInstance().login(stringOnBase, BaseApp.inst().loginCertificate.userID,
                BaseApp.inst().loginCertificate.token);
        }
    }

    @Override
    public void onDestroy() {
        L.e(TAG, "onDestroy");
        super.onDestroy();
    }
}
