package io.openim.android.ouicalling;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AudioVideoService extends Service {

    private static final int NOTIFY_ID = 1000;
    private static final String TAG = "AudioVideoService";
    private Notification notification;

    protected void showNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent hangIntent = new Intent(this, getApplication().getClass());
        PendingIntent hangPendingIntent = PendingIntent.getActivity(this, 1002, hangIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String CHANNEL_ID = "AudioVideoService";
        String CHANNEL_NAME = getString(io.openim.android.ouicore.R.string.audio_video_service);
         notification = new NotificationCompat.Builder(this, CHANNEL_ID)
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
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        showNotification();
        startForeground(NOTIFY_ID, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }
}
