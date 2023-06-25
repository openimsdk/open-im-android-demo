package io.openim.android.ouicalling.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.keepalive.KeepAliveService;
import open_im_sdk.Open_im_sdk;
import open_im_sdk_callback.OnListenerForService;

public class AudioVideoService extends KeepAliveService {

    public static final String TAG = "AudioVideoService-----";
    public static final int NOTIFY_ID = 10000;
    private CallingService callingService;
    private Class<?> postcardDestination;

    private void showNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent hangIntent = new Intent(this, postcardDestination);
        PendingIntent hangPendingIntent = PendingIntent.getActivity(this, 1002, hangIntent,
            PendingIntent.FLAG_MUTABLE);

        String CHANNEL_ID = "AudioVideoService";
        String CHANNEL_NAME = getString(io.openim.android.ouicore.R.string.audio_video_service);
        Notification notification =
            new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(io.openim.android.ouicore.R.string.audio_video_service_tips1))
                .setContentText(getString(io.openim.android.ouicore.R.string.audio_video_service_tips2))
                .setSmallIcon(io.openim.android.ouicore.R.mipmap.ic_logo)
                .setContentIntent(hangPendingIntent).build();

        //Android 8.0 以上需包添加渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(notificationChannel);
        }
        manager.notify(NOTIFY_ID, notification);
        startForeground(NOTIFY_ID, notification);
    }

    @Override
    public void onCreate() {
        L.e(TAG, "onCreate");
        Postcard postcard = ARouter.getInstance().build(Routes.Main.SPLASH);
        LogisticsCenter.completion(postcard);
        postcardDestination = postcard.getDestination();
        showNotification();
        super.onCreate();

        callingService =
            (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
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
//        Open_im_sdk.setListenerForService(new OnListenerForService() {
//            @Override
//            public void onFriendApplicationAccepted(String s) {
//                IMUtil.sendNotice(System.currentTimeMillis());
//            }
//
//            @Override
//            public void onFriendApplicationAdded(String s) {
//                IMUtil.sendNotice(System.currentTimeMillis());
//            }
//
//            @Override
//            public void onGroupApplicationAccepted(String s) {
//                IMUtil.sendNotice(System.currentTimeMillis());
//            }
//
//            @Override
//            public void onGroupApplicationAdded(String s) {
//                IMUtil.sendNotice(System.currentTimeMillis());
//            }
//
//            @Override
//            public void onRecvNewMessage(String s) {
//
//            }
//        });
    }

    public void loginOpenIM(OnBase<String> stringOnBase) {
        if (IMUtil.isLogged()) return;
        L.e(TAG, "logging...");
        BaseApp.inst().loginCertificate = LoginCertificate.getCache(BaseApp.inst());
        if (null != BaseApp.inst().loginCertificate) {
            OpenIMClient.getInstance().login(stringOnBase, BaseApp.inst().loginCertificate.userID
                , BaseApp.inst().loginCertificate.imToken);
        }
    }
}
