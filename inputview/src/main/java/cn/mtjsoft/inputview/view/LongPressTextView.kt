package cn.mtjsoft.inputview.view

import android.Manifest.permission.RECORD_AUDIO
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import cn.mtjsoft.inputview.Constant
import cn.mtjsoft.inputview.R
import cn.mtjsoft.inputview.manager.RecordManager
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 长按录音控件
 */
class LongPressTextView(private val mContext: Context, attrs: AttributeSet?) : AppCompatTextView(
    mContext, attrs
) {
    private var viewStop = false
    private var mPressListener: onLongPressListener? = null

    //音量显示
    private val mVolumeResources: TypedArray

    //松开取消弹框
    private var mDialogRemain: Dialog? = null
    private val mTvDuration: TextView
    private val mIvVolume: ImageView
    private val mTvVolumeTip: TextView

    private val mRecordManager = RecordManager()

    /**
     * 是否取消发送
     */
    var isCancel = false

    private lateinit var savePcmPath: String

    private var currentDur: Long = 0
    private var saveFileName: String = "temp_record_pcm.pcm"

    fun setOnLongPressListener(l: onLongPressListener) {
        savePcmPath = context.filesDir.absolutePath + File.separator + saveFileName

        mPressListener = l
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            pauseMusic(mContext)
            val curClickTime = System.currentTimeMillis()
            //避免系统时间修改后获取的时间比当前时间小
            if (curClickTime < lastClickTime) {
                lastClickTime = 0
            }
            if (curClickTime - lastClickTime < MIN_CLICK_DELAY_TIME) {
                return true
            }
            lastClickTime = curClickTime
            isCancel = false
            viewStop = false
            currentDur = 0
            startLongPress()
            return true
        } else if (event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP) {
            mDialogRemain?.dismiss()
            abandonAudioFocus(mContext)
            if (viewStop) {
                viewStop = false
                return super.onTouchEvent(event)
            }
            setStop()
            mRecordManager.stopRecording()
            if (!isCancel) {
                if (currentDur > 0) {
                    mPressListener?.onRecordOver(currentDur, saveFileName, savePcmPath)
                } else {
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.RECORD_AUDIO_TIME_SHORT),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                File(savePcmPath).delete()
            }
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            if (viewStop) {
                abandonAudioFocus(mContext)
                return super.onTouchEvent(event)
            }
            if (event.y < -50 && !isCancel) {
                isCancel = true
                setText(R.string.record_status_recording_up)
            } else if (event.y > -50 && isCancel) {
                isCancel = false
                setText(R.string.stop_press_record)
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 开始
     */
    private fun startLongPress() {
        // 检查是否有录音权限
        if (ContextCompat.checkSelfPermission(
                context,
                RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            setStop()
            // 没有权限
            Toast.makeText(
                context,
                context.resources.getString(R.string.RECORD_AUDIO_Permission),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (!validateMicAvailability()) {
            stop()
            mRecordManager.stopRecording()
            reset()
        } else {
            reset()
            setText(R.string.stop_press_record)
            isPressed = true
            setDuration(0, 0)
            mRecordManager.startRecord(
                savePcmPath,
                RECORD_TIME_MAX,
                object : RecordManager.OnRecordListener {

                    override fun onRecording(currentDur: Long, size: Int, bytes: ByteArray) {
                        this@LongPressTextView.currentDur = currentDur
                        val volume: Int = mRecordManager.calculateVolume(bytes)
                        this@LongPressTextView.postDelayed({
                            setDuration(volume, currentDur)
                        }, 200)
                    }

                    override fun onRecordOver(
                        currentDur: Long,
                        fileName: String,
                        filePath: String
                    ) {
                        this@LongPressTextView.currentDur = currentDur
                        if (currentDur >= RECORD_TIME_MAX) {
                            // 超时
                            this@LongPressTextView.post {
                                mDialogRemain?.dismiss()
                                setStop()
                                mRecordManager.stopRecording()
                                if (!isCancel) {
                                    mPressListener?.onRecordOver(currentDur, saveFileName, filePath)
                                } else {
                                    File(savePcmPath).delete()
                                }
                            }
                        }
                    }

                    override fun onError(e: Exception) {
                        this@LongPressTextView.post {
                            Toast.makeText(
                                context,
                                context.resources.getString(R.string.RECORD_AUDIO_ERROR),
                                Toast.LENGTH_SHORT
                            ).show()
                            setStop()
                        }
                    }
                })
        }
    }

    private fun setStop() {
        viewStop = true
        isPressed = false
        setText(R.string.press_record)
        release()
    }

    /**
     * 重置状态
     */
    private fun reset() {
        viewStop = false
        release()
    }

    /**
     * 停止
     */
    private fun stop() {
        viewStop = true
        release()
    }

    /**
     * 置成空闲状态
     */
    private fun release() {
        if (System.currentTimeMillis() - lastClickTime >= MIN_CLICK_DELAY_TIME) {
            visibility = VISIBLE
        }
        //        checkPopupRemainTime(Integer.MAX_VALUE);
    }

    /**
     * 录音过程中回调方法
     *
     * @param volume          当前录音音量
     * @param currentDuration 已录音长度
     */
    private fun setDuration(volume: Int, currentDuration: Long) {
        var volume = volume
        if (!viewStop) {
            if (volume > 5) {
                volume = 5
            }
        } else {
            mDialogRemain!!.dismiss()
        }
        checkPopupRemainTime(volume, currentDuration)
    }

    /**
     *
     * 校验倒计时弹窗显示状态
     *
     *
     *
     * 注1 倒计时小于、等于5秒时开始显示计时
     */
    private fun checkPopupRemainTime(volume: Int, currentDuration: Long) {
        if (!viewStop) {
            if (!mDialogRemain!!.isShowing) {
                mDialogRemain!!.show()
            }
            mTvDuration.text = getDuration(currentDuration)
            val remain = getRemainTime(RECORD_TIME_MAX - currentDuration)
            if (isCancel) {
                mTvVolumeTip.setText(R.string.record_status_recording_up)
            } else {
                if (remain <= 8) {
                    mTvVolumeTip.text = remain.toString()
                } else {
                    mTvVolumeTip.setText(R.string.on_the_cancel)
                }
            }
            mIvVolume.setImageResource(
                if (isCancel) R.mipmap.icon_up_canel else mVolumeResources.getResourceId(
                    volume,
                    0
                )
            )
        } else {
            mDialogRemain?.dismiss()
        }
    }

    interface onLongPressListener {
        fun onRecordOver(
            currentDur: Long,
            fileName: String,
            filePath: String
        )
    }

    /**
     * 暂停音乐播放
     *
     * @param context 上下文
     */
    private fun pauseMusic(context: Context) {
        val freshIntent = Intent()
        freshIntent.action = "com.android.music.musicservicecommand.pause"
        freshIntent.putExtra("command", "pause")
        context.sendBroadcast(freshIntent)
        val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        mAudioManager?.requestAudioFocus(
            { i: Int -> },
            AudioManager.STREAM_VOICE_CALL,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
    }

    /**
     * 时长转换为 02:05的形式，2分5秒
     */
    private fun getDuration(durationInMilli: Long): String {
        val sb = StringBuilder()
        val second = TimeUnit.MILLISECONDS.toSeconds(durationInMilli)
        if (second < 1) {
            return "00:00"
        }
        //多少小时
        val h = TimeUnit.SECONDS.toHours(second)
        //不足1小时，还剩多少分钟
        val m = TimeUnit.SECONDS.toMinutes(second) - TimeUnit.HOURS.toMinutes(h)
        //不足1分钟，还剩多少秒
        val s = second - TimeUnit.HOURS.toSeconds(h) - TimeUnit.MINUTES.toSeconds(m)
        if (h != 0L) {
            sb.append(formatTime(h)).append(":")
        }
        sb.append(formatTime(m)).append(":").append(formatTime(s))
        return sb.toString()
    }

    private fun formatTime(time: Long): String {
        return if (time < 10) {
            "0$time"
        } else time.toString()
    }

    private fun getRemainTime(timeInMilli: Long): Long {
        val second = TimeUnit.MILLISECONDS.toSeconds(timeInMilli)
        val remainMillis = timeInMilli - TimeUnit.SECONDS.toMillis(second)
        return if (remainMillis != 0L) {
            second + 1
        } else {
            second
        }
    }

    //麦克风通道被占用，返回true表示没有被占用，返回false表示被占用
    private fun validateMicAvailability(): Boolean {
        var available = true
        val bufferSize = AudioRecord.getMinBufferSize(
            Constant.FREQUENCY, Constant.CHANNEL_CONFIG,
            Constant.AUDIO_FORMAT
        )
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            Constant.FREQUENCY, Constant.CHANNEL_CONFIG,
            Constant.AUDIO_FORMAT,
            bufferSize
        )
        try {
            if (recorder.recordingState != AudioRecord.RECORDSTATE_STOPPED) {
                available = false
            }
            recorder.startRecording()
            if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                available = false
            }
        } finally {
            recorder.stop()
            recorder.release()
        }
        return available
    }

    companion object {
        var RECORD_TIME_MAX: Long = 60000

        /**
         * 两次点击按钮之间的点击间隔不能少于1000毫秒
         */
        private const val MIN_CLICK_DELAY_TIME = 1000
        private var lastClickTime: Long = 0

        /**
         * 放弃音乐焦点
         *
         * @param context 上下文
         */
        private fun abandonAudioFocus(context: Context) {
            val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            mAudioManager?.abandonAudioFocus(null)
        }
    }

    init {
        val inflater = LayoutInflater.from(context)
        val mRecordVoiceFloat = inflater.inflate(R.layout.layout_record_voice, null)
        val rootView = mRecordVoiceFloat.findViewById<View>(R.id.root_view)
        rootView.setOnClickListener(null)
        mIvVolume = mRecordVoiceFloat.findViewById(R.id.iv_volume)
        mTvVolumeTip = mRecordVoiceFloat.findViewById(R.id.tv_volume_tip)
        mTvDuration = mRecordVoiceFloat.findViewById(R.id.tv_volume_duration)
        if (mDialogRemain == null) {
            mDialogRemain = Dialog(mContext, R.style.dialogCustom)
            mDialogRemain?.setContentView(mRecordVoiceFloat)
            val window = mDialogRemain?.window
            window?.setDimAmount(0f)
        }
        mVolumeResources = mContext.resources.obtainTypedArray(R.array.record_volume_array)
    }
}
