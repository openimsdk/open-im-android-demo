package io.openim.android.ouicore.utils;

import static android.app.Notification.VISIBILITY_PUBLIC;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Objects;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.im.IMUtil;

public class NotificationUtil {
    public static final String CALL_CHANNEL_ID = "audio_video_service";
    public static final String MSG_NOTIFICATION = "msg_notification";
    public static final String RESIDENT_SERVICE = "resident_Service";

    public static NotificationCompat.Builder builder(String channel) {
        NotificationManager manager =
            (NotificationManager) BaseApp.inst().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (null == manager.getNotificationChannel(channel)) {
                if (Objects.equals(channel, MSG_NOTIFICATION))
                    buildMsgNotificationChannel();
            }
        }
        return new NotificationCompat.Builder(BaseApp.inst(), channel)
            .setSmallIcon(R.mipmap.ic_launcher_round);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void buildMsgNotificationChannel() {
        NotificationManager manager =
            (NotificationManager) BaseApp.inst().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(MSG_NOTIFICATION,
            BaseApp.inst().getString(R.string.msg_notification), NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setBypassDnd(true);
        notificationChannel.setLockscreenVisibility(VISIBILITY_PUBLIC);
        notificationChannel.enableLights(true);

        AudioAttributes audioAttributes =
            new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
        notificationChannel
            .setSound(Uri.parse("android.resource://" + BaseApp.inst().getPackageName() + "/" + R.raw.message_ring),
                audioAttributes);
        manager.createNotificationChannel(notificationChannel);
    }

    public static void sendNotify(int id, Notification notification) {
        NotificationManager manager =
            (NotificationManager) BaseApp.inst().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id, notification);

    }

    public static void cancelNotify(int id) {
        NotificationManager manager =
            (NotificationManager) BaseApp.inst().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(id);
    }

}
