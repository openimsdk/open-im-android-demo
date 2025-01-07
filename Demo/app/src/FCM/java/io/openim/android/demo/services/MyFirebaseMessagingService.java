package io.openim.android.demo.services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.L;

/**
 * Used for handling events from FCM
 *
 * @see <a href="https://firebase.google.com/docs/cloud-messaging/android/client/">FCM Doc</a>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final int NOTIFICATION_ID = 201;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        L.e("onMessageReceived: " + message.getData());
        if (!message.getData().isEmpty()) {
            L.e("Message data payload: " + message.getData());
            IMUtil.sendNotice(NOTIFICATION_ID);
        }
        if (message.getNotification() != null) {
            L.e("Message Notification Body: " + message.getNotification().getBody());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        L.e("onNewToken: " + token);
    }

}
