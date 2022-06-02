package io.openim.android.ouicore.voice.netWork;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import io.openim.android.ouicore.voice.cache.VoiceCacheUtils;
import io.openim.android.ouicore.voice.listener.OnDownloadListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SuppressLint("StaticFieldLeak")
public class VoiceDownloadUtil {
    private final String TAG = "VoiceDownloadUtil";
    private static VoiceDownloadUtil downloadUtil;
    private OkHttpClient okHttpClient;
    private static Context mContext;
    private DownHandler mDownHandler;
    private OnDownloadListener onDownloadListener;


    public static VoiceDownloadUtil instance() {
        if (downloadUtil == null) {
            downloadUtil = new VoiceDownloadUtil();
        }
        return downloadUtil;
    }

    public static void init(Context context) {
        mContext = context;
    }

    public VoiceDownloadUtil() {
        okHttpClient = new OkHttpClient();
    }


    public void download(final String url, final OnDownloadListener listener) {
        if (mContext == null) {
            throw new RuntimeException(TAG + "请在Application中使用 SPlayer.init()方法");
        } else {
//            if (mDownHandler == null) {
//                mDownHandler = new DownHandler();
//            }
            onDownloadListener = listener;
            String cacheUrl = VoiceCacheUtils.instance().hasCache(url);
            if (cacheUrl == null) {
                //没有缓存,下载文件
                Request request = new Request.Builder().url(url).build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        listener.onDownloadFailed(e);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) {
                        writeFile(url, response, listener);
                    }
                });
            } else {
                //有缓存,直接加载
                if (listener != null) {
                    listener.onDownloading(100);
                    listener.onDownloadSuccess(new File(VoiceCacheUtils.instance().getCachePath(), VoiceCacheUtils.instance().getMediaID(url)));
                }
            }
        }
    }

    public void download(final String url) {
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure" + e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                writeFile(url, response, null);
            }
        });
    }

    private void writeFile(String url, Response response, OnDownloadListener listener) {
        File dir = new File(VoiceCacheUtils.instance().getCachePath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, VoiceCacheUtils.instance().getMediaID(url));
        OutputStream outputStream = null;
        InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
        try {
            outputStream = new FileOutputStream(file);
            int len;
            byte[] buffer = new byte[1024 * 10];
            long sum = 0;
            long total = Objects.requireNonNull(response.body()).contentLength();
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
                sum += len;
                int progress = (int) (sum * 1.0f / total * 100);
                if (listener != null) {
                    listener.onDownloading(progress);
                }
            }
        } catch (IOException e) {
            if (listener != null) {
                listener.onDownloadFailed(e);
            }
        } finally {
            try {
                inputStream.close();
                if (outputStream != null)
                    outputStream.close();
                if (listener != null) {
                    listener.onDownloadSuccess(new File(VoiceCacheUtils.instance().getCachePath(), VoiceCacheUtils.instance().getMediaID(url)));
                }
            } catch (IOException e) {
                if (listener != null) {
                    listener.onDownloadFailed(e);
                }
            }
        }
    }


    private class DownHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1://有本地缓存
                    onDownloadListener.onDownloading(100);
                    onDownloadListener.onDownloadSuccess(new File(VoiceCacheUtils.instance().getCachePath(), VoiceCacheUtils.instance().getMediaID(String.valueOf(msg.obj))));
                    break;
                case 2://下载文件进度
                    onDownloadListener.onDownloading((Integer) msg.obj);
                    break;
                case 3://异常
                    onDownloadListener.onDownloadFailed((Exception) msg.obj);
                    break;
                case 4://下载完成
                    onDownloadListener.onDownloadSuccess((File) msg.obj);
                    break;
            }
        }
    }
}
