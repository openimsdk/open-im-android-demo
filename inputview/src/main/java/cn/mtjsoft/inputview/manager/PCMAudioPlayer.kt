package cn.mtjsoft.inputview.manager

import android.media.*
import cn.mtjsoft.inputview.Constant
import cn.mtjsoft.inputview.manager.PCMAudioPlayer
import java.io.DataInputStream
import java.io.FileInputStream
import java.util.concurrent.Future

/**
 * pcm 音频播放
 *
 */
class PCMAudioPlayer private constructor() {
    @Volatile
    private var audioTrack: AudioTrack? = null
    private var mMinBufferSize = 0
    private var currentAudioFrameLength = 0

    /**
     * 播放提示音
     *
     * @param audioPath 音频文件路径
     */
    fun startPlay(audioPath: String) {
        var fileInputStream: FileInputStream? = null
        try {
            if (audioTrack == null) {
                synchronized(this) {
                    if (audioTrack == null) {
                        mMinBufferSize = AudioTrack.getMinBufferSize(
                            DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG,
                            DEFAULT_AUDIO_FORMAT
                        )
                        val attributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                        val format = AudioFormat.Builder().setSampleRate(DEFAULT_SAMPLE_RATE)
                            .setChannelMask(DEFAULT_CHANNEL_CONFIG)
                            .setEncoding(DEFAULT_AUDIO_FORMAT)
                            .build()
                        audioTrack = AudioTrack(
                            attributes,
                            format,
                            mMinBufferSize,
                            DEFAULT_PLAY_MODE,
                            AudioManager.AUDIO_SESSION_ID_GENERATE
                        )
                    }
                }
            }
            resetPlay()
            Constant.DEFAULT_EXECUTOR.submit {
                fileInputStream = FileInputStream(audioPath)
                fileInputStream?.let {
                    // 计算音频的 frame length，因为音频为 16bit 格式，所以 1frame = 16bit = 2bytes，因此 totalFrames = fileLength / 2
                    currentAudioFrameLength = it.available() / 2
                    val tempBuffer = ByteArray(mMinBufferSize)
                    var readCount: Int
                    while (audioTrack != null && it.available() > 0) {
                        readCount = it.read(tempBuffer)
                        if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                            continue
                        }
                        if (readCount != 0 && readCount != -1) {
                            audioTrack?.write(tempBuffer, 0, readCount)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            fileInputStream?.close()
        }
    }

    /**
     * 停止播放
     */
    fun stopPlay() {
        try {
            audioTrack?.apply {
                if (state == AudioRecord.STATE_INITIALIZED) {
                    stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 暂停播放
     */
    fun pausePlay() {
        try {
            audioTrack?.apply {
                if (state == AudioRecord.STATE_INITIALIZED) {
                    pause()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 重置音频播放
     */
    private fun resetPlay() {
        try {
            audioTrack?.apply {
                if (state == AudioRecord.STATE_INITIALIZED) {
                    pause()
                    flush()
                    play()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 是否已暂停
     *
     * @return 暂停
     */
    private val isStopped: Boolean
        get() = currentAudioFrameLength == 0 || currentAudioFrameLength == audioTrack!!.playbackHeadPosition || audioTrack!!.playbackHeadPosition > currentAudioFrameLength

    /**
     * 释放音频播放
     */
    fun release() {
        try {
            audioTrack?.apply {
                if (state == AudioRecord.STATE_INITIALIZED) {
                    stop()
                    flush()
                    release()
                }
                audioTrack = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val DEFAULT_SAMPLE_RATE = Constant.FREQUENCY //采样频率
        private const val DEFAULT_CHANNEL_CONFIG = Constant.CHANNEL_CONFIG_OUT //注意是out
        private const val DEFAULT_AUDIO_FORMAT = Constant.AUDIO_FORMAT
        private const val DEFAULT_PLAY_MODE = Constant.PLAY_MODE
        private val pcmAudioPlayer = PCMAudioPlayer()
        val instance: PCMAudioPlayer
            get() = pcmAudioPlayer
    }
}