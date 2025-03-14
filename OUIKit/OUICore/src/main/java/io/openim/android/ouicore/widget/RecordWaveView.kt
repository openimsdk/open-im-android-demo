package io.openim.android.ouicore.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.lang.Math.*

class RecordWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.parseColor("#74B3FF")
    }
    private var count = 0
    private var lineHeight = 0f
    private var start = 0f
    private var ideaHeight = 0f
    private var density = 0f

    init {
        density = resources.displayMetrics.density
        ideaHeight = density * 3
        lineHeight = density * 3
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        count = floor((w / (density * 3)).toDouble()).toInt()
        if (count.rem(2) != 1) {
            count -= 1
        }
        start = (w - count * lineHeight) / 2f
    }

    private var degrees = 3.0

    private var currentIndex = -1

    /**
     * 获取中心点,做偏移操作
     */
    override fun onDraw(canvas: Canvas) {
        //修改高度
        (0 until count).forEach {
            val dy = abs(sin(toDegrees(degrees + start + lineHeight * it)))
            val f = (it + 1).rem(9)
            val dt = if (f <= 5) {
                f * amplitude
            } else {
                (10 - f) * amplitude
            }
            val lineWave = if (currentIndex != -1) {
                if (it == currentIndex) {
                    ideaHeight * 0.8f
                } else if (currentIndex - 1 == it || currentIndex + 1 == it) {
                    ideaHeight * 0.6f
                } else if (currentIndex - 2 == it || currentIndex + 2 == it) {
                    ideaHeight * 0.4f
                } else 0f
            } else 0f
            canvas.drawRoundRect(
                start + lineHeight * it,
                (measuredHeight / 2f - density * 4 - dy * dt - lineWave).toFloat(),
                start + lineHeight * it + density * 2,
                (measuredHeight / 2f + density * 4 + dy * dt + lineWave).toFloat(),
                density * 1,
                density * 1,
                paint
            )
        }
        updateWave()

    }

    private var amplitude = 0f

    private var dOffset = 0.002

    /**
     * 获取数据
     */
    private fun getAmplitude(): Int {
        return listener?.getValue() ?: 0
    }

    private var listener: WaveValueListener? = null

    fun setAmpListener(listener: WaveValueListener) {
        this.listener = listener
    }

    interface WaveValueListener {
        fun getValue(): Int
    }

    /**
     * 从中间辐射到两边
     */
    private var preValue = 0f
    private var preMaxValue = 0f//记录上次最大值,但是未达到此巅峰

    private fun updateWave() {
        var current = getAmplitude()
        if (current > 7) {
            current = 7
        }
        //巅峰到达最低
        if (preMaxValue != 0f && current != 0) {//
            if (preMaxValue < current) {
                preMaxValue = current.toFloat()
            }
        } else if (preMaxValue == 0f && current != 0 && preValue < current) {
            preMaxValue = current.toFloat()
        }
        if (preValue >= preMaxValue) {
            preMaxValue = 0f
        }
        if (preMaxValue == 0f) {
            if (preValue != 0f && current == 0) {
                preValue -= 0.1f
                if (preValue < 0) {
                    preValue = 0f
                }
            } else if (current < preValue) {
                preValue -= 0.1f
            } else {
                preValue = current.toFloat()
            }
        } else {
            if (preValue < preMaxValue && preMaxValue != 0f) {
                preValue += 0.5f
            }
            if (preValue > 7f) {
                preValue = 7f
            }
        }
        amplitude = preValue
        //没有信号波入侵 模拟一条从左到右的入侵信号
        if (preValue == 0f && current == 0 && preMaxValue == 0f) {
            offsetLine += 0.4f
            currentIndex = offsetLine.toInt()
            if (currentIndex > count) {
                offsetLine = 0f
                currentIndex = 0
            }
            amplitude = 0f
        } else {
            offsetLine = 0f
            currentIndex = -1
        }
        degrees += dOffset
        postInvalidateOnAnimation()
    }

    private var offsetLine = 0f
}
