package io.openim.android.demo.services;

import android.content.Context;
import android.text.TextUtils;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTTransmitMessage;

import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.L;

/**
 * Used for handling custom data from Getui
 *
 * @see <a href="https://docs.getui.com/getui/mobile/android/androidstudio/">Getui Doc</a>
 */
public class GetuiPushIntentService extends GTIntentService {
    public static final int NOTIFICATION_ID = 202;
    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage gtTransmitMessage) {
        byte[] payload = gtTransmitMessage.getPayload();
        String data = new String(payload);
        L.d("receiver payload = " + data);
        if (!TextUtils.isEmpty(data))
            IMUtil.sendNotice(NOTIFICATION_ID);
    }
}
