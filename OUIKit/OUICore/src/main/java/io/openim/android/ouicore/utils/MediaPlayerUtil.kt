package io.openim.android.ouicore.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import com.liulishuo.okdownload.OkDownloadProvider.context
import io.openim.android.ouicore.base.BaseApp


/**
 * 音频播放
 */
object MediaPlayerUtil {
    var mPlayer: MediaPlayer? = null
    private var isPause = false
    private var isPlaying = false

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun initMedia(mContext: Context, rawRes: Int) {
        mPlayer = MediaPlayer.create(mContext, rawRes)
        config()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun initMedia(mContext: Context, fad: AssetFileDescriptor) {
        mPlayer = MediaPlayer()
        mPlayer!!.setDataSource(fad)

        config()
    }

    fun initMedia(mContext: Context, path: String) {
        mPlayer = MediaPlayer()
        mPlayer!!.setDataSource(path)

        config()
    }

    fun initUriMedia(mContext: Context, uriPath: String) {
        mPlayer = MediaPlayer()
        mPlayer!!.setDataSource(mContext!!, Uri.parse(uriPath))

        config()
    }

    private fun config() {
//        mPlayer!!.setAudioAttributes(
//            AudioAttributes
//                .Builder()
//                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                .build()
//        )



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
