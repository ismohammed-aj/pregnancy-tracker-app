package com.pfe.mamacareplus.ui.onboarding

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class HoldProgressCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(18f, 0f, 10f, Color.argb(26, 214, 51, 108))
    }
    private val softCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val baseRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(90, 234, 215, 245)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val arcBounds = RectF()

    var progressFraction: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        val centerX = width / 2f
        val centerY = height / 2f
        val outerRadius = size * 0.39f
        val innerRadius = size * 0.29f
        val strokeWidth = size * 0.045f

        softCirclePaint.color = Color.argb(90, 207, 239, 231)
        canvas.drawCircle(centerX - size * 0.24f, centerY - size * 0.22f, size * 0.15f, softCirclePaint)

        softCirclePaint.color = Color.argb(82, 234, 215, 245)
        canvas.drawCircle(centerX + size * 0.26f, centerY - size * 0.12f, size * 0.18f, softCirclePaint)

        softCirclePaint.color = Color.argb(75, 251, 234, 242)
        canvas.drawCircle(centerX + size * 0.18f, centerY + size * 0.27f, size * 0.16f, softCirclePaint)

        canvas.drawCircle(centerX, centerY, outerRadius, fillPaint)

        softCirclePaint.color = Color.argb(130, 255, 240, 246)
        canvas.drawCircle(centerX, centerY, innerRadius, softCirclePaint)

        baseRingPaint.strokeWidth = strokeWidth
        progressPaint.strokeWidth = strokeWidth
        progressPaint.shader = SweepGradient(
            centerX,
            centerY,
            intArrayOf(
                Color.parseColor("#D6336C"),
                Color.parseColor("#6AD7C9"),
                Color.parseColor("#D96B8C"),
            ),
            floatArrayOf(0f, 0.58f, 1f),
        )

        val ringInset = strokeWidth / 2f + size * 0.055f
        arcBounds.set(ringInset, ringInset, width - ringInset, height - ringInset)
        canvas.drawArc(arcBounds, -90f, 360f, false, baseRingPaint)
        canvas.drawArc(arcBounds, -90f, 360f * progressFraction, false, progressPaint)
    }
}
