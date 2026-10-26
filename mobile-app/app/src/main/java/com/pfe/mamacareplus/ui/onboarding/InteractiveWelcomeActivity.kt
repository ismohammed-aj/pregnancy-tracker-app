package com.pfe.mamacareplus.ui.onboarding

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.databinding.ActivityInteractiveWelcomeBinding
import com.pfe.mamacareplus.ui.auth.LoginActivity
import kotlin.math.hypot

class InteractiveWelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInteractiveWelcomeBinding
    private var progressAnimator: ValueAnimator? = null
    private var pulseAnimator: ValueAnimator? = null
    private var completed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInteractiveWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.onboarding_background_top)
        window.navigationBarColor = getColor(R.color.onboarding_background_bottom)

        prepareEntranceState()
        playEntranceAnimation()
        startCirclePulse()
        bindCircleHold()
    }

    override fun onDestroy() {
        progressAnimator?.cancel()
        pulseAnimator?.cancel()
        super.onDestroy()
    }

    private fun prepareEntranceState() {
        binding.heroCircle.alpha = 0f
        binding.heroCircle.scaleX = 0.9f
        binding.heroCircle.scaleY = 0.9f
        binding.touchPulseRing.alpha = 0f
        binding.touchPulseRing.scaleX = 0.72f
        binding.touchPulseRing.scaleY = 0.72f
        binding.touchHintGroup.alpha = 0f
        binding.touchHintGroup.scaleX = 0.86f
        binding.touchHintGroup.scaleY = 0.86f
        binding.contentGroup.alpha = 0f
        binding.contentGroup.translationY = 24f
        binding.holdHint.alpha = 0f
        binding.holdHint.translationY = 20f
    }

    private fun playEntranceAnimation() {
        binding.heroCircle.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(680)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.touchPulseRing.animate()
            .alpha(0.32f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(120)
            .setDuration(560)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.touchHintGroup.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(180)
            .setDuration(560)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.contentGroup.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(260)
            .setDuration(520)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.holdHint.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(430)
            .setDuration(520)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun startCirclePulse() {
        pulseAnimator = ValueAnimator.ofFloat(0.96f, 1.03f).apply {
            duration = 1500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                binding.heroCircle.scaleX = value
                binding.heroCircle.scaleY = value
                binding.touchPulseRing.scaleX = value * 1.08f
                binding.touchPulseRing.scaleY = value * 1.08f
                binding.touchPulseRing.alpha = 0.22f + ((value - 0.96f) / 0.07f).coerceIn(0f, 1f) * 0.22f
                binding.touchHintGroup.scaleX = 1f + ((value - 1f) * 0.45f)
                binding.touchHintGroup.scaleY = 1f + ((value - 1f) * 0.45f)
            }
            start()
        }
    }

    private fun bindCircleHold() {
        val listener = android.view.View.OnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (!completed) startHoldProgress()
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!completed) resetHoldProgress()
                    true
                }

                else -> true
            }
        }

        binding.heroCircle.setOnTouchListener(listener)
        binding.touchHintGroup.setOnTouchListener(listener)
        binding.touchPulseRing.setOnTouchListener(listener)
        binding.heroIcon.setOnTouchListener(listener)
        binding.circleHint.setOnTouchListener(listener)
    }

    private fun startHoldProgress() {
        progressAnimator?.cancel()
        pulseAnimator?.cancel()
        binding.holdHint.text = getString(R.string.onboarding_hold_keep)
        binding.holdHint.animate().alpha(0.72f).setDuration(120).start()
        binding.touchPulseRing.animate().alpha(0.52f).scaleX(1.16f).scaleY(1.16f).setDuration(160).start()
        binding.touchHintGroup.animate().scaleX(0.94f).scaleY(0.94f).setDuration(160).start()

        val startProgress = binding.heroCircle.progressFraction
        val duration = ((1f - startProgress) * HOLD_DURATION_MS).toLong().coerceAtLeast(180L)

        progressAnimator = ValueAnimator.ofFloat(startProgress, 1f).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                updateHoldVisuals(animator.animatedValue as Float)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (binding.heroCircle.progressFraction >= 0.995f && !completed) {
                        completed = true
                        navigateToAuthChoice()
                    }
                }
            })
            start()
        }
    }

    private fun resetHoldProgress() {
        progressAnimator?.cancel()
        binding.holdHint.text = getString(R.string.onboarding_hold_button)
        binding.holdHint.animate().alpha(1f).setDuration(140).start()

        progressAnimator = ValueAnimator.ofFloat(binding.heroCircle.progressFraction, 0f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                updateHoldVisuals(animator.animatedValue as Float)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (!completed) startCirclePulse()
                }
            })
            start()
        }
    }

    private fun navigateToAuthChoice() {
        binding.holdHint.text = getString(R.string.onboarding_hold_done)
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(R.anim.onboarding_enter, R.anim.onboarding_exit)
        finish()
    }

    private fun updateHoldVisuals(progress: Float) {
        val clampedProgress = progress.coerceIn(0f, 1f)
        val circleScale = 1f + ((expandedCircleScale() - 1f) * clampedProgress)
        val textAlpha = (1f - clampedProgress * 1.45f).coerceIn(0f, 1f)
        val iconAlpha = (1f - clampedProgress * 1.2f).coerceIn(0f, 1f)

        binding.heroCircle.progressFraction = clampedProgress
        binding.heroCircle.scaleX = circleScale
        binding.heroCircle.scaleY = circleScale
        binding.touchHintGroup.alpha = iconAlpha
        binding.touchHintGroup.scaleX = 1f + clampedProgress * 0.18f
        binding.touchHintGroup.scaleY = 1f + clampedProgress * 0.18f
        binding.touchPulseRing.alpha = (0.5f - clampedProgress * 0.9f).coerceIn(0f, 0.5f)
        binding.touchPulseRing.scaleX = 1.08f + clampedProgress * 0.5f
        binding.touchPulseRing.scaleY = 1.08f + clampedProgress * 0.5f
        binding.topLabel.alpha = textAlpha
        binding.contentGroup.alpha = textAlpha
        binding.holdHint.alpha = (1f - clampedProgress * 1.1f).coerceIn(0f, 0.9f)
    }

    private fun expandedCircleScale(): Float {
        val circleSize = binding.heroCircle.width.coerceAtLeast(1).toFloat()
        val screenDiagonal = hypot(binding.root.width.toFloat(), binding.root.height.toFloat())
        return ((screenDiagonal / circleSize) * 1.16f).coerceAtLeast(3.2f)
    }

    companion object {
        private const val HOLD_DURATION_MS = 1900L
    }
}
