package io.openim.android.ouicalling.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import io.openim.android.ouicore.utils.L;

public class AudioVideoDaemonService extends AudioVideoService {

    private static final String TAG = "AudioVideoService2-----";

    @Override
    public void onCreate() {
        L.e(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.e(TAG, "onStartCommand");
        showNotification();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        L.e(TAG, "onDestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
