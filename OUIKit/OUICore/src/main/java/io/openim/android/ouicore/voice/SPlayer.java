package io.openim.android.ouicore.voice;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;


import androidx.annotation.RequiresApi;



import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.openim.android.ouicore.voice.cache.VoiceCacheUtils;
import io.openim.android.ouicore.voice.listener.PlayByAssetsListener;
import io.openim.android.ouicore.voice.listener.PlayerListener;
import io.openim.android.ouicore.voice.netWork.VoiceDownloadUtil;
import io.openim.android.ouicore.voice.player.AudioFocusManager;
import io.openim.android.ouicore.voice.player.SMediaPlayer;

public class SPlayer {
    public static final String TAG = "SPlayer";
    private static Context mContext;
    private static SMediaPlayer mMediaPlayer;
    private volatile static SPlayer instance;
    // 初始化wifi锁
    private WifiManager.WifiLock wifiLock;
    private boolean useWifiLock = false;//是否使用wifi锁
    private boolean setWakeMode = false;//使用使用唤醒锁
    private boolean isUseCache = true;
    private int corePoolSize = 2;//核心线程数量
    private int maximumPoolSize = 8;//最大线程数量
    // 初始化音频焦点管理器
    AudioFocusManager audioFocusManager;
    //创建基本线程池
    private ThreadPoolExecutor threadPoolExecutor;


    public static void init(Context context) {
        mContext = context;
        VoiceCacheUtils.init(context);
        VoiceDownloadUtil.init(context);
    }

    public static SPlayer instance() {
        if (instance == null) {
            synchronized (SPlayer.class) {
                if (instance == null) {
                    instance = new SPlayer();
                }
            }
        }
        return instance;
    }

    public SPlayer useWifiLock(boolean useWifiLock) {
        this.useWifiLock = useWifiLock;
        return this;
    }

    /**
     * @param setWakeMode 是否使用唤醒锁
     */
    public SPlayer useWakeMode(boolean setWakeMode) {
        this.setWakeMode = setWakeMode;
        return this;
    }

    public SPlayer setUseCache(boolean useCache) {
        isUseCache = useCache;
        return this;
    }

    public void playByUrl(final String url, final PlayerListener listener) {
        if (mContext == null) {
            throw new RuntimeException("请在Application中使用 SPlayer.init()方法");
        } else {
            if (mMediaPlayer == null) {
                mMediaPlayer = new SMediaPlayer();
                audioFocusManager = new AudioFocusManager(mContext);
                mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                        mediaPlayer.reset();
                        return false;
                    }
                });
            } else {
                mMediaPlayer.reset();
            }
            if (threadPoolExecutor == null) {
                threadPoolExecutor = new ThreadPoolExecutor(
                        corePoolSize   //线程池中核心线程的数量。
                        , maximumPoolSize     //线程池中最大线程数量。
                        , 1     //非核心线程的超时时长，当系统中非核心线程闲置时间超过keepAliveTime之后，则会被回收。
                        , TimeUnit.SECONDS      //keepAliveTime这个参数的单位
                        , new LinkedBlockingQueue<Runnable>(50));//线程池中的任务队列，该队列主要用来存储已经被提交但是尚未执行的任务。存储在这里的任务是由ThreadPoolExecutor的execute方法提交来的。
            }
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                if (isUseCache) {
                    //使用缓存
                    String cacheUrl = VoiceCacheUtils.instance().hasCache(url);
                    if (cacheUrl == null) {
                        //没有缓存
//                        new Thread() {
////                            @Override
////                            public void run() {
////                                VoiceDownloadUtil.instance().download(url);
////                            }
////                        }.start();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                VoiceDownloadUtil.instance().download(url);
                            }
                        };
                        threadPoolExecutor.execute(runnable);
                        mMediaPlayer.setDataSource(url);
                        mMediaPlayer.prepareAsync();
                        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                            @Override
                            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                                //异步加载在线音乐监听
                                listener.Loading(mMediaPlayer, i);
                            }
                        });
                    } else {
                        //使用缓存
                        mMediaPlayer.setDataSource(cacheUrl);
                        mMediaPlayer.prepare();
                        listener.Loading(mMediaPlayer, 100);
                    }
                } else {
                    //不使用缓存
                    mMediaPlayer.setDataSource(url);
                    mMediaPlayer.prepareAsync();
                    mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                        @Override
                        public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                            //异步加载在线音乐监听
                            listener.Loading(mMediaPlayer, i);
                        }
                    });
                }
            } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
                listener.onError(e);
            }
            if (useWifiLock) {
                //使用wifi锁
                wifiLock = ((WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
                // 启用wifi锁
                wifiLock.acquire();
            }
            if (setWakeMode) {
                //使用唤醒锁
                mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
            }
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    listener.LoadSuccess(mMediaPlayer);
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    listener.onCompletion(mMediaPlayer);
                }
            });
        }
    }

    public void playByAsset(String fileName, final PlayerListener listener) {
        //打开Asset目录
        AssetManager assetManager = mContext.getAssets();
        mMediaPlayer = new SMediaPlayer();
        try {
            AssetFileDescriptor assetFileDescriptor = assetManager.openFd(fileName);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    mediaPlayer.reset();
                    return false;
                }
            });
            mMediaPlayer.prepare();
            mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                    //加载在线音乐监听
                    listener.Loading(mMediaPlayer, i);
                }
            });
            if (useWifiLock) {
                //使用wifi锁
                wifiLock = ((WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
                // 启用wifi锁
                wifiLock.acquire();
            }
            if (setWakeMode) {
                //使用唤醒锁
                mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
            }
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    listener.LoadSuccess(mMediaPlayer);
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    listener.onCompletion(mMediaPlayer);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playByAsset(String fileName) {
        //打开Asset目录
        AssetManager assetManager = mContext.getAssets();
        mMediaPlayer = new SMediaPlayer();
        try {
            AssetFileDescriptor assetFileDescriptor = assetManager.openFd(fileName);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    mediaPlayer.reset();
                    return false;
                }
            });
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mMediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playByAsset(String fileName, final PlayByAssetsListener listener) {
        //打开Asset目录
        AssetManager assetManager = mContext.getAssets();
        mMediaPlayer = new SMediaPlayer();
        try {
            AssetFileDescriptor assetFileDescriptor = assetManager.openFd(fileName);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    mediaPlayer.reset();
                    return false;
                }
            });
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    //加载完成自动播放
                    mMediaPlayer.start();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //播放完毕监听
                    listener.onCompletion(mMediaPlayer);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public SMediaPlayer getMediaPlayer() {
        return mMediaPlayer != null ? mMediaPlayer : (mMediaPlayer=new SMediaPlayer());
    }

    public void prepare() {
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        // 获取音频焦点
        if (!audioFocusManager.requestAudioFocus()) {
            Log.e(TAG, "获取音频焦点失败");
        }
        mMediaPlayer.start();
    }

    public void pause() {
        lockedWifiLock();
        // 取消音频焦点
        if (audioFocusManager != null) {
            audioFocusManager.abandonAudioFocus();
        }
        mMediaPlayer.pause();
    }

    public void stop() {
        lockedWifiLock();
        // 取消音频焦点
        if (audioFocusManager != null) {
            audioFocusManager.abandonAudioFocus();
        }
        mMediaPlayer.stop();
    }

    public void reset() {
        // 取消音频焦点
        if (audioFocusManager != null) {
            audioFocusManager.abandonAudioFocus();
        }
        mMediaPlayer.reset();
    }

    public void release() {
        // 取消音频焦点
        if (audioFocusManager != null) {
            audioFocusManager.abandonAudioFocus();
        }
        lockedWifiLock();
        mMediaPlayer.release();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void seekTo(long msec, int mode) {
        mMediaPlayer.seekTo(msec, mode);
    }

    public void seekTo(int msec) {
        mMediaPlayer.seekTo(msec);
    }

    public boolean isPlaying() {
        if (mMediaPlayer == null) {
            return false;
        } else {
            return mMediaPlayer.isPlaying();
        }
    }

    /**
     * 关闭wifi锁
     */
    private void lockedWifiLock() {
        if (useWifiLock) {
            // 关闭wifi锁
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
        }
    }

    public SPlayer setCacheDirPath(String dirPath) {
        VoiceCacheUtils.instance().setCacheDirPath(dirPath);
        return this;
    }

    public SPlayer setCachePath(String path) {
        VoiceCacheUtils.instance().setCachePath(path);
        return this;
    }

    //核心线程数量
    public SPlayer setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 7 && corePoolSize > 0) {
            this.corePoolSize = corePoolSize;
        }
        return this;
    }

    //最大线程数量
    public SPlayer setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize > this.corePoolSize) {
            this.maximumPoolSize = this.corePoolSize;
        } else {
            this.maximumPoolSize = Math.min(maximumPoolSize, 64);
        }
        return this;
    }

    public String getCacheSize() {
        return VoiceCacheUtils.instance().getFormatSize();
    }

    public void clearCache() {
        VoiceCacheUtils.instance().deleteCache();
    }
}
