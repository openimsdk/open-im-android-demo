package io.openim.android.ouicore.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 音频播放
 */
@SuppressLint("StaticFieldLeak")
object MediaPlayerUtil {

    private var mContext: Context? = null
    var mPlayer: MediaPlayer? = null

    private var isPause = false

    private var isPlaying = false

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun initMedia(mContext: Context, rawRes: Int) {
        mPlayer = MediaPlayer.create(mContext, rawRes)
        mPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun initMedia(mContext: Context, fad: AssetFileDescriptor) {
        mPlayer = MediaPlayer()
        mPlayer!!.setDataSource(fad)
    }

    fun initMedia(mContext: Context, path: String) {
        mPlayer = MediaPlayer()
        mPlayer!!.setDataSource(path)
    }

    fun initUriMedia(mContext: Context, uriPath: String) {
        mPlayer = MediaPlayer()
        mPlayer!!.setDataSource(mContext!!, Uri.parse(uriPath))
        mPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
    }

    fun setMediaListener(listener: MediaPlayerListener) {
        mPlayer!!.setOnCompletionListener {
            isPlaying = false
            listener.finish()
        }

        mPlayer!!.setOnErrorListener { mp, what, extra ->
            isPlaying = false
            listener.onErr(what)
            false
        }
        mPlayer!!.setOnPreparedListener {
            listener.prepare()
        }
    }

    fun playMedia() {
        if (mPlayer != null && !mPlayer!!.isPlaying) {
            mPlayer!!.start()
        }
    }

    fun prepare() {
        if (mPlayer != null && !mPlayer!!.isPlaying) {
            mPlayer!!.prepare()
        }
    }

    fun prepareAsync() {
        if (mPlayer != null && !mPlayer!!.isPlaying) {
            mPlayer!!.prepareAsync()
        }
    }

    fun pause() {
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.pause()
            isPause = true
        }
    }

    // 继续
    fun resume() {
        if (mPlayer != null && isPause) {
            mPlayer!!.start()
            isPause = false
        }
    }

    fun release() {
        if (mPlayer != null) {
            try {
                mPlayer!!.release()
            } catch (e: Exception) {
            }
            mPlayer = null
        }
        isPlaying = false
    }

}

interface MediaPlayerListener {
    fun finish()
    abstract fun onErr(what: Int)
    fun prepare()

}
