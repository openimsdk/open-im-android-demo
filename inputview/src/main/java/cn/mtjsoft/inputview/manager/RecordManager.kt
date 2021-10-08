package cn.mtjsoft.inputview.manager

import android.media.AudioRecord
import android.media.MediaRecorder
import cn.mtjsoft.inputview.Constant
import java.io.*
import kotlin.experimental.and

/**
 * 音频录制线程
 *
 * @author Shark
 */
class RecordManager {
    private var isRecording = 0 //录制状态，0:录制空闲 1:准备录制 2:录制中
    private var recordStartTime: Long = 0 //录制开始时间
    private var recordListener: OnRecordListener? = null

    private var mAudioRecord: AudioRecord? = null

    private lateinit var savePcmPath: String

    private var allDuration: Long = 0

    // 默认录制一分钟
    private var RECORD_TIME_MAX: Long = 60000

    /**
     * 开始录制视频
     * @param savePcmPath 保存地址 例如： /file/temp.pcm
     * @param maxTime 最长录制时间（毫秒）最少设置1秒
     */
    fun startRecord(savePcmPath: String, maxTime: Long, l: OnRecordListener) {
        RECORD_TIME_MAX = if (maxTime >= 1000) maxTime else 60000
        this.savePcmPath = savePcmPath
        isRecording = 1
        isRecordProgress = false
        recordListener = l
        allDuration = 0
        Constant.DEFAULT_EXECUTOR.execute(RecordRunnable())
    }

    /**
     * 外部调用结束录音
     */
    fun stopRecording() {
        isRecording = 0
        isRecordProgress = false
    }

    private fun stopRecord() {
        isRecording = 0
        isRecordProgress = false
        recordStartTime = 0
        recordListener = null
        mAudioRecord?.stop()
        mAudioRecord?.release()
    }

    fun isRecording(): Boolean {
        return isRecording != 0
    }

    fun calculateVolume(buffer: ByteArray): Int {
        var sumVolume = 0.0
        val avgVolume: Double
        val volume: Int
        var i = 0
        while (i < buffer.size) {
            val v1 = (buffer[i] and 0xFF.toByte()).toInt()
            val v2 = (buffer[i + 1] and 0xFF.toByte()).toInt()
            var temp = v1 + (v2 shl 8) // 小端
            if (temp >= 0x8000) {
                temp = 0xffff - temp
            }
            sumVolume += Math.abs(temp).toDouble()
            i += 2
        }
        avgVolume = sumVolume / buffer.size / 2
        volume = Math.log10(1 + avgVolume).toInt() * 10 / 6
        return volume
    }

    interface OnRecordListener {
        /**
         * 音频录制中
         *
         * @param currentDur 录制总时长
         * @param size       当次录制长度
         * @param bytes      录制的音频内容
         */
        fun onRecording(currentDur: Long, size: Int, bytes: ByteArray)

        fun onRecordOver(currentDur: Long, fileName: String, filePath: String)

        fun onError(e: Exception)
    }

    internal inner class RecordRunnable : Runnable {
        override fun run() {
            File(savePcmPath).delete()
            isRecording = 2
            isRecordProgress = true
            recordStartTime = System.currentTimeMillis()
            try {
                val bufferSize = AudioRecord.getMinBufferSize(
                    Constant.FREQUENCY, Constant.CHANNEL_CONFIG,
                    Constant.AUDIO_FORMAT
                )
                //实例化AudioRecord
                mAudioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC, Constant.FREQUENCY, Constant.CHANNEL_CONFIG,
                    Constant.AUDIO_FORMAT, bufferSize
                )
                mAudioRecord?.let {
                    //开始录制
                    it.startRecording()
                    val outputStream: OutputStream =
                        BufferedOutputStream(FileOutputStream(savePcmPath))
                    //定义缓冲
                    val buffer = ByteArray(bufferSize)
                    while (isRecording()) {
                        //从bufferSize中读取字节
                        val bufferReadResult = it.read(buffer, 0, bufferSize)
                        outputStream.write(buffer, 0, bufferReadResult)
                        val bytes = buffer.copyOf(bufferReadResult)
                        allDuration = System.currentTimeMillis() - recordStartTime
                        if (recordListener != null) {
                            recordListener!!.onRecording(allDuration, bufferReadResult, bytes)
                            if (allDuration >= RECORD_TIME_MAX) {
                                stopRecording()
                            }
                        }
                    }
                    outputStream.flush()
                    outputStream.close()
                    //录制结束
                    stopRecord()
                    if (allDuration > 0 && recordListener != null) {
                        recordListener!!.onRecordOver(
                            allDuration,
                            savePcmPath.substringAfterLast("/"),
                            savePcmPath
                        )
                    }
                }
            } catch (e: Exception) {
                isRecording = 0
                isRecordProgress = false
                if (recordListener != null) {
                    recordListener!!.onError(e)
                }
                recordStartTime = 0
                recordListener = null
                File(savePcmPath).delete()
            }
        }
    }

    companion object {
        /**
         * 是否正在录音，根据当前录音时长来判读
         */
        var isRecordProgress = false

        fun pcmToWav(inFilename: String, outFilename: String) {
            val fileInputStream: FileInputStream
            val out: FileOutputStream
            val totalAudioLen: Long
            val totalDataLen: Long
            val longSampleRate = Constant.FREQUENCY
            val channels = Constant.CHANNEL_CONFIG
            val byteRate = 16 * longSampleRate * channels / 8
            val bufferSize = AudioRecord.getMinBufferSize(
                Constant.FREQUENCY, Constant.CHANNEL_CONFIG,
                Constant.AUDIO_FORMAT
            )
            val data = ByteArray(bufferSize)
            try {
                fileInputStream = FileInputStream(inFilename)
                out = FileOutputStream(outFilename)
                totalAudioLen = fileInputStream.channel.size()
                totalDataLen = totalAudioLen + 36
                writeWaveFileHeader(
                    out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate
                )
                while (fileInputStream.read(data) != -1) {
                    out.write(data)
                }
                fileInputStream.close()
                out.close()
                File(inFilename).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @Throws(IOException::class)
        private fun writeWaveFileHeader(
            out: FileOutputStream, totalAudioLen: Long,
            totalDataLen: Long, sampleRate: Int, channels: Int, byteRate: Int
        ) {
            val header = ByteArray(44)
            header[0] = 'R'.toByte() // RIFF
            header[1] = 'I'.toByte()
            header[2] = 'F'.toByte()
            header[3] = 'F'.toByte()
            header[4] = (totalDataLen and 0xff).toByte() //数据大小
            header[5] = (totalDataLen shr 8 and 0xff).toByte()
            header[6] = (totalDataLen shr 16 and 0xff).toByte()
            header[7] = (totalDataLen shr 24 and 0xff).toByte()
            header[8] = 'W'.toByte() //WAVE
            header[9] = 'A'.toByte()
            header[10] = 'V'.toByte()
            header[11] = 'E'.toByte()
            //FMT Chunk
            header[12] = 'f'.toByte() // 'fmt '
            header[13] = 'm'.toByte()
            header[14] = 't'.toByte()
            header[15] = ' '.toByte() //过渡字节
            //数据大小
            header[16] = 16 // 4 bytes: size of 'fmt ' chunk
            header[17] = 0
            header[18] = 0
            header[19] = 0
            //编码方式 10H为PCM编码格式
            header[20] = 1 // format = 1
            header[21] = 0
            //通道数
            header[22] = channels.toByte()
            header[23] = 0
            //采样率，每个通道的播放速度
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = (sampleRate shr 8 and 0xff).toByte()
            header[26] = (sampleRate shr 16 and 0xff).toByte()
            header[27] = (sampleRate shr 24 and 0xff).toByte()
            //音频数据传送速率,采样率*通道数*采样深度/8
            header[28] = (byteRate and 0xff).toByte()
            header[29] = (byteRate shr 8 and 0xff).toByte()
            header[30] = (byteRate shr 16 and 0xff).toByte()
            header[31] = (byteRate shr 24 and 0xff).toByte()
            // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
            header[32] = (channels * 16 / 8).toByte()
            header[33] = 0
            //每个样本的数据位数
            header[34] = 16
            header[35] = 0
            //Data chunk
            header[36] = 'd'.toByte() //data
            header[37] = 'a'.toByte()
            header[38] = 't'.toByte()
            header[39] = 'a'.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = (totalAudioLen shr 8 and 0xff).toByte()
            header[42] = (totalAudioLen shr 16 and 0xff).toByte()
            header[43] = (totalAudioLen shr 24 and 0xff).toByte()
            out.write(header, 0, 44)
        }
    }
}