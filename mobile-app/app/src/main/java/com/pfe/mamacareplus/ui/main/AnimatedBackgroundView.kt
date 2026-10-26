package com.pfe.mamacareplus.ui.main

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class AnimatedBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private var progress = 0f
    private var animationEnabled = true

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 22000L
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
    }

    private val shapes = listOf(
        FloatingShape(ShapeKind.BUBBLE, 0.12f, 0.15f, 86f, Color.rgb(106, 215, 201), 42, 10f, 18f, 0.9f, 0.2f),
        FloatingShape(ShapeKind.HEART, 0.82f, 0.13f, 34f, Color.rgb(217, 107, 140), 44, 8f, 14f, 0.7f, 1.1f),
        FloatingShape(ShapeKind.BLOB, 0.70f, 0.33f, 126f, Color.rgb(234, 215, 245), 50, 16f, 12f, 0.55f, 2.0f),
        FloatingShape(ShapeKind.STAR, 0.19f, 0.47f, 31f, Color.rgb(255, 255, 255), 82, 12f, 10f, 0.8f, 2.8f),
        FloatingShape(ShapeKind.BUMP, 0.88f, 0.62f, 82f, Color.rgb(106, 215, 201), 34, 13f, 20f, 0.6f, 3.4f),
        FloatingShape(ShapeKind.BUBBLE, 0.23f, 0.74f, 118f, Color.rgb(255, 255, 255), 64, 18f, 12f, 0.5f, 4.1f),
        FloatingShape(ShapeKind.HEART, 0.64f, 0.84f, 27f, Color.rgb(217, 107, 140), 34, 10f, 15f, 0.75f, 5.2f),
        FloatingShape(ShapeKind.STAR, 0.41f, 0.24f, 24f, Color.rgb(234, 215, 245), 58, 8f, 12f, 0.95f, 5.9f)
    )

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        isClickable = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (animationEnabled) {
            animator.start()
        }
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    fun setAnimationEnabled(enabled: Boolean) {
        animationEnabled = enabled
        if (enabled && isAttachedToWindow && !animator.isStarted) {
            animator.start()
        } else if (!enabled) {
            animator.cancel()
            progress = 0f
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        shapes.forEach { shape ->
            val t = (progress * TWO_PI * shape.speed) + shape.phase
            val x = width * shape.x + sin(t) * shape.amplitudeX * density
            val y = height * shape.y + cos(t * 0.86f) * shape.amplitudeY * density
            val size = shape.size * density

            when (shape.kind) {
                ShapeKind.BUBBLE -> drawBubble(canvas, x, y, size, shape)
                ShapeKind.BLOB -> drawBlob(canvas, x, y, size, shape)
                ShapeKind.HEART -> drawHeart(canvas, x, y, size, shape)
                ShapeKind.STAR -> drawStar(canvas, x, y, size, shape)
                ShapeKind.BUMP -> drawBabyBump(canvas, x, y, size, shape)
            }
        }
    }

    private fun drawBubble(canvas: Canvas, x: Float, y: Float, size: Float, shape: FloatingShape) {
        val radius = size / 2f
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            x - radius * 0.35f,
            y - radius * 0.35f,
            radius * 1.15f,
            intArrayOf(
                Color.argb(shape.alpha + 24, 255, 255, 255),
                withAlpha(shape.color, shape.alpha),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.58f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(x, y, radius, paint)
        paint.shader = null
    }

    private fun drawBlob(canvas: Canvas, x: Float, y: Float, size: Float, shape: FloatingShape) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(shape.color, shape.alpha)
        canvas.drawOval(x - size * 0.48f, y - size * 0.34f, x + size * 0.48f, y + size * 0.34f, paint)
        paint.color = withAlpha(Color.WHITE, 36)
        canvas.drawCircle(x - size * 0.16f, y - size * 0.08f, size * 0.22f, paint)
    }

    private fun drawHeart(canvas: Canvas, x: Float, y: Float, size: Float, shape: FloatingShape) {
        val s = size / 2f
        path.reset()
        path.moveTo(x, y + s * 0.46f)
        path.cubicTo(x - s * 1.16f, y - s * 0.22f, x - s * 0.58f, y - s * 1.08f, x, y - s * 0.38f)
        path.cubicTo(x + s * 0.58f, y - s * 1.08f, x + s * 1.16f, y - s * 0.22f, x, y + s * 0.46f)
        path.close()

        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(shape.color, shape.alpha)
        canvas.drawPath(path, paint)
    }

    private fun drawStar(canvas: Canvas, x: Float, y: Float, size: Float, shape: FloatingShape) {
        val outer = size / 2f
        val inner = outer * 0.45f
        path.reset()
        repeat(10) { index ->
            val angle = -PI / 2.0 + index * PI / 5.0
            val radius = if (index % 2 == 0) outer else inner
            val pointX = x + cos(angle).toFloat() * radius
            val pointY = y + sin(angle).toFloat() * radius
            if (index == 0) path.moveTo(pointX, pointY) else path.lineTo(pointX, pointY)
        }
        path.close()

        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(shape.color, shape.alpha)
        canvas.drawPath(path, paint)
    }

    private fun drawBabyBump(canvas: Canvas, x: Float, y: Float, size: Float, shape: FloatingShape) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(shape.color, shape.alpha)
        canvas.drawCircle(x, y + size * 0.08f, size * 0.34f, paint)
        paint.color = withAlpha(Color.rgb(217, 107, 140), 28)
        canvas.drawCircle(x - size * 0.08f, y + size * 0.08f, size * 0.15f, paint)
        paint.color = withAlpha(Color.WHITE, 54)
        canvas.drawCircle(x - size * 0.13f, y - size * 0.05f, size * 0.09f, paint)
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))
    }

    private data class FloatingShape(
        val kind: ShapeKind,
        val x: Float,
        val y: Float,
        val size: Float,
        val color: Int,
        val alpha: Int,
        val amplitudeX: Float,
        val amplitudeY: Float,
        val speed: Float,
        val phase: Float
    )

    private enum class ShapeKind {
        BUBBLE,
        BLOB,
        HEART,
        STAR,
        BUMP
    }

    companion object {
        private const val TWO_PI = (Math.PI * 2.0).toFloat()
    }
}
