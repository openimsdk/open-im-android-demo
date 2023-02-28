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
import io.openim.android.sdk.listener._SignalingListener;
import io.openim.keepalive.KeepAliveService;
import open_im_sdk.Open_im_sdk;
import open_im_sdk_callback.OnListenerForService;

public class AudioVideoService extends KeepAliveService {

    private static final String TAG = "AudioVideoService-----";
    private static final int NOTIFY_ID = 10000;
    private CallingService callingService;

    private void showNotification() {
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
            .build();

        //Android 8.0 以上需包添加渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(notificationChannel);
        }
        manager.notify(NOTIFY_ID, notification);
        startForeground(NOTIFY_ID, notification);
    }

    private void msgNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent hangIntent = new Intent(this, JumpReceiver.class);
        PendingIntent hangPendingIntent = PendingIntent.getBroadcast(this, 1002,
            hangIntent, PendingIntent.FLAG_MUTABLE);

        String CHANNEL_ID = "MsgNotification";
        String CHANNEL_NAME = getString(io.openim.android.ouicore.R.string.msg_notification);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(io.openim.android.ouicore.R.string.app_name))
            .setContentText(getString(io.openim.android.ouicore.R.string.a_message_is_received))
            .setSmallIcon(io.openim.android.ouicore.R.mipmap.ic_logo)
            .setContentIntent(hangPendingIntent)
            .setAutoCancel(true)
            .build();

        //Android 8.0 以上需包添加渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(notificationChannel);
        }
        manager.notify(NOTIFY_ID, notification);
    }
    @Override
    public void onCreate() {
        L.e(TAG, "onCreate");
        showNotification();
        super.onCreate();

         callingService = (CallingService) ARouter.getInstance()
            .build(Routes.Service.CALLING).navigation();
        addListener();

        loginOpenIM(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                L.e(TAG, "login -----" + error + code);
            }

            @Override
            public void onSuccess(String data) {
                L.e(TAG, "login -----onSuccess");
                CallingService.OnServicePriorLoginCallBack callBack =
                    callingService.getOnServicePriorLoginCallBack();
                if (null != callBack) callBack.onLogin();
            }
        });
    }

    private void addListener() {
        OpenIMClient.getInstance().signalingManager.setSignalingListener(callingService);
        Open_im_sdk.setListenerForService(new OnListenerForService() {
            @Override
            public void onFriendApplicationAccepted(String s) {
                msgNotification();
            }

            @Override
            public void onFriendApplicationAdded(String s) {
                msgNotification();
            }

            @Override
            public void onGroupApplicationAccepted(String s) {
                msgNotification();
            }

            @Override
            public void onGroupApplicationAdded(String s) {
                msgNotification();
            }

            @Override
            public void onRecvNewMessage(String s) {
                msgNotification();
            }
        });
    }

    public void loginOpenIM(OnBase<String> stringOnBase) {
        if (IMUtil.isLogged()) return;
        L.e(TAG, "logging...");
        BaseApp.inst().loginCertificate = LoginCertificate.getCache(BaseApp.inst());
        if (null != BaseApp.inst().loginCertificate) {
            OpenIMClient.getInstance().login(stringOnBase, BaseApp.inst().loginCertificate.userID,
                BaseApp.inst().loginCertificate.imToken);
        }
    }
}
