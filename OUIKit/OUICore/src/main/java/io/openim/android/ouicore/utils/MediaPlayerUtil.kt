package io.openim.android.ouicore.utils

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi


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

    fun loopPlay() {
        setMediaListener(object : MediaPlayerListener {
            override fun finish() {
                playMedia()
            }

            override fun onErr(what: Int) {}
            override fun prepare() {
                playMedia()
            }
        })
        playMedia()
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
            isPlaying = true
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
            isPlaying = true
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
