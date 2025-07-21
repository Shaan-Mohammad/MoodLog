package com.example.moodlog

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.floor

data class YearPixelData(
    val date: String,
    val mood: String = "",
    val color: String = "#F3F4F6",
    val emoji: String? = null
)

class YearPixelsView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    def: Int = 0
) : View(ctx, attrs, def) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var data: List<YearPixelData> = emptyList()
    private val pixelSize = 22f
    private val pixelSpacing = 8f
    private val labelHeight = 56f
    private val leftMargin = 64f
    private var onPixelClick: ((YearPixelData) -> Unit)? = null

    fun setPixelData(d: List<YearPixelData>) {
        data = d
        invalidate()
    }

    fun setOnPixelClickListener(f: (YearPixelData) -> Unit) {
        onPixelClick = f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val columns = 53
        val rows = 7
        val width =
            (leftMargin + columns * (pixelSize + pixelSpacing) + pixelSpacing).toInt()
        val height =
            (labelHeight + rows * (pixelSize + pixelSpacing) + pixelSpacing + 32).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawMonthLabels(canvas)
        drawDayLabels(canvas)
        drawPixels(canvas)
    }

    private fun drawMonthLabels(canvas: Canvas) {
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#374151")
            textSize = 28f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }
        val weekToMonth = intArrayOf(0, 4, 8, 13, 17, 21, 26, 30, 35, 39, 44, 48)
        for (i in months.indices) {
            val x = leftMargin + weekToMonth[i] * (pixelSize + pixelSpacing)
            canvas.drawText(months[i], x, 42f, tp)
        }
    }

    private fun drawDayLabels(canvas: Canvas) {
        val days = arrayOf("M", "T", "W", "T", "F", "S", "S")
        val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6B7280")
            textSize = 20f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        for (i in days.indices) {
            val y = labelHeight + i * (pixelSize + pixelSpacing) + pixelSize
            canvas.drawText(days[i], 18f, y, tp)
        }
    }

    private fun drawPixels(canvas: Canvas) {
        data.forEachIndexed { i, pixel ->
            val week = i / 7
            val day = i % 7
            val left = leftMargin + week * (pixelSize + pixelSpacing)
            val top = labelHeight + day * (pixelSize + pixelSpacing)
            // depth shadow
            paint.color = Color.parseColor("#E5E7EB")
            canvas.drawRoundRect(
                RectF(left - 2, top - 2, left + pixelSize + 2, top + pixelSize + 2),
                6f, 6f, paint
            )
            // pixel
            paint.color = Color.parseColor(pixel.color)
            canvas.drawRoundRect(
                RectF(left, top, left + pixelSize, top + pixelSize),
                6f, 6f, paint
            )
            // emoji overlay
            pixel.emoji?.takeIf { pixel.mood.isNotEmpty() }?.let {
                paint.color = Color.BLACK
                paint.textSize = 16f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(
                    it,
                    left + pixelSize / 2,
                    top + pixelSize * 0.75f,
                    paint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x - leftMargin
            val y = event.y - labelHeight
            if (x >= 0 && y >= 0) {
                val week = floor(x / (pixelSize + pixelSpacing)).toInt()
                val day = floor(y / (pixelSize + pixelSpacing)).toInt()
                val idx = week * 7 + day
                if (idx in data.indices) {
                    onPixelClick?.invoke(data[idx])
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
