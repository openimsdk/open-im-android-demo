package io.openim.android.ouicore.utils;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewTreeObserver;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.sdk.models.ConversationInfo;

public class Common {
    /**
     * 主线程handler
     */
    public final static Handler UIHandler = new Handler(Looper.getMainLooper());


    public static String md5(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static int dp2px(float dp) {
        float scale = BaseApp.instance().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}

