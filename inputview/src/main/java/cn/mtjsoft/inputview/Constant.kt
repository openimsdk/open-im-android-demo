package cn.mtjsoft.inputview

import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object
Constant {
    /**
     * 创建线程池
     */
    val DEFAULT_EXECUTOR = ThreadPoolExecutor(
        4, 8, 30, TimeUnit.SECONDS, LinkedBlockingQueue(1024)
    )

    /**
     * 录音播放参数
     */
    // 设置音频采样率，44100是目前的标准，单位hz
    const val FREQUENCY = 44100

    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道
    const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO

    const val CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_STEREO

    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    const val PLAY_MODE = AudioTrack.MODE_STREAM
}