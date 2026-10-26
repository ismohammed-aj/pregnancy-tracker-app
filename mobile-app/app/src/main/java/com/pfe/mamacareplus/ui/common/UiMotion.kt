package com.pfe.mamacareplus.ui.common

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.pfe.mamacareplus.R

fun View.applyModernPressAnimation(pressedScale: Float = 0.96f) {
    setOnTouchListener { view, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                view.animate()
                    .scaleX(pressedScale)
                    .scaleY(pressedScale)
                    .alpha(0.9f)
                    .setDuration(90)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(170)
                    .setInterpolator(OvershootInterpolator(1.25f))
                    .start()
            }
        }
        false
    }
}

fun View.playSoftEntrance(delayMillis: Long = 0L) {
    alpha = 0f
    translationY = 18f * resources.displayMetrics.density
    animate()
        .alpha(1f)
        .translationY(0f)
        .setStartDelay(delayMillis)
        .setDuration(260)
        .setInterpolator(DecelerateInterpolator())
        .start()
}

fun TextInputLayout.bindLiveValidation(
    editText: EditText,
    invalidWhenBlank: Boolean = true,
    validator: (String) -> Boolean,
) {
    bindAnimatedFeedback(editText, invalidWhenBlank, validator)
}

fun TextInputLayout.applyPremiumInputFeedback(
    editText: EditText,
    invalidWhenBlank: Boolean = false,
    validator: ((String) -> Boolean)? = null,
) {
    bindAnimatedFeedback(editText, invalidWhenBlank, validator)
}

fun TextInputLayout.applyPremiumInputFeedback(
    invalidWhenBlank: Boolean = false,
    validator: ((String) -> Boolean)? = null,
) {
    editText?.let { bindAnimatedFeedback(it, invalidWhenBlank, validator) }
}

private fun TextInputLayout.bindAnimatedFeedback(
    editText: EditText,
    invalidWhenBlank: Boolean,
    validator: ((String) -> Boolean)?,
) {
    val colors = inputFeedbackColors(editText)
    val evaluator = ArgbEvaluator()
    var previousLength = editText.text?.length ?: 0
    var touchedByUser = false
    var currentColor = colors.neutralStroke
    var colorAnimator: ValueAnimator? = null

    fun setVisualColor(color: Int, labelColor: Int = color) {
        setBoxStrokeColorStateList(ColorStateList.valueOf(color))
        hintTextColor = ColorStateList.valueOf(labelColor)
        setStartIconTintList(ColorStateList.valueOf(labelColor))
        setEndIconTintList(ColorStateList.valueOf(labelColor))
        setErrorTextColor(ColorStateList.valueOf(colors.deleteColor))
    }

    fun animateColorTo(color: Int, labelColor: Int = color) {
        colorAnimator?.cancel()
        if (currentColor == color) {
            setVisualColor(color, labelColor)
            return
        }
        colorAnimator = ValueAnimator.ofObject(evaluator, currentColor, color).apply {
            duration = INPUT_ANIMATION_DURATION_MS
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                setVisualColor(animator.animatedValue as Int, labelColor)
            }
            start()
        }
        currentColor = color
    }

    fun animateScale(state: InputFeedbackState) {
        val targetScale = when (state) {
            InputFeedbackState.Neutral -> 1f
            InputFeedbackState.Focused, InputFeedbackState.Typing -> 1.01f
            InputFeedbackState.Deleting, InputFeedbackState.Invalid -> 0.997f
        }
        animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(INPUT_ANIMATION_DURATION_MS)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun validationState(value: String): InputFeedbackState {
        val invalid = (invalidWhenBlank && value.isBlank()) ||
            (value.isNotBlank() && validator?.invoke(value) == false)
        return if (invalid && touchedByUser) InputFeedbackState.Invalid else InputFeedbackState.Neutral
    }

    fun applyState(state: InputFeedbackState) {
        editText.setTextColor(colors.textColor)
        editText.setHintTextColor(colors.placeholderColor)
        when (state) {
            InputFeedbackState.Neutral -> animateColorTo(colors.neutralStroke, colors.labelColor)
            InputFeedbackState.Focused,
            InputFeedbackState.Typing -> animateColorTo(colors.typeColor)
            InputFeedbackState.Deleting,
            InputFeedbackState.Invalid -> animateColorTo(colors.deleteColor)
        }
        animateScale(state)
    }

    setVisualColor(colors.neutralStroke, colors.labelColor)
    editText.setTextColor(colors.textColor)
    editText.setHintTextColor(colors.placeholderColor)
    boxStrokeWidth = dp(1)
    boxStrokeWidthFocused = dp(2)
    elevation = maxOf(elevation, dpFloat(1f))

    editText.setOnFocusChangeListener { _, hasFocus ->
        val value = editText.text?.toString().orEmpty()
        applyState(
            when {
                hasFocus -> InputFeedbackState.Focused
                else -> validationState(value)
            },
        )
    }

    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            error = null
            val value = s?.toString().orEmpty()
            val newLength = value.length

            if (!editText.hasFocus()) {
                previousLength = newLength
                applyState(validationState(value))
                return
            }

            touchedByUser = true
            val state = when {
                newLength < previousLength -> InputFeedbackState.Deleting
                newLength > previousLength || value.isNotBlank() -> InputFeedbackState.Typing
                else -> InputFeedbackState.Focused
            }
            previousLength = newLength
            applyState(state)
        }
    })

    applyState(InputFeedbackState.Neutral)
}

fun EditText.bindLiveValidationColor(
    invalidWhenBlank: Boolean = true,
    validator: (String) -> Boolean,
) {
    bindAnimatedPlainFeedback(invalidWhenBlank, validator)
}

fun EditText.applyPremiumInputFeedback(
    invalidWhenBlank: Boolean = false,
    validator: ((String) -> Boolean)? = null,
) {
    bindAnimatedPlainFeedback(invalidWhenBlank, validator)
}

private fun EditText.bindAnimatedPlainFeedback(
    invalidWhenBlank: Boolean,
    validator: ((String) -> Boolean)?,
) {
    val colors = inputFeedbackColors(this)
    val evaluator = ArgbEvaluator()
    var previousLength = text?.length ?: 0
    var touchedByUser = false
    var currentColor = colors.neutralStroke
    var colorAnimator: ValueAnimator? = null

    fun setVisualColor(color: Int) {
        backgroundTintList = null
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpFloat(16f)
            setColor(colors.fillColor)
            setStroke(if (color == colors.neutralStroke) dp(1) else dp(2), color)
        }
    }

    fun animateColorTo(color: Int) {
        colorAnimator?.cancel()
        if (currentColor == color) {
            setVisualColor(color)
            return
        }
        colorAnimator = ValueAnimator.ofObject(evaluator, currentColor, color).apply {
            duration = INPUT_ANIMATION_DURATION_MS
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                setVisualColor(animator.animatedValue as Int)
            }
            start()
        }
        currentColor = color
    }

    fun validationState(value: String): InputFeedbackState {
        val invalid = (invalidWhenBlank && value.isBlank()) ||
            (value.isNotBlank() && validator?.invoke(value) == false)
        return if (invalid && touchedByUser) InputFeedbackState.Invalid else InputFeedbackState.Neutral
    }

    fun applyState(state: InputFeedbackState) {
        setTextColor(colors.textColor)
        setHintTextColor(colors.placeholderColor)
        when (state) {
            InputFeedbackState.Neutral -> animateColorTo(colors.neutralStroke)
            InputFeedbackState.Focused,
            InputFeedbackState.Typing -> animateColorTo(colors.typeColor)
            InputFeedbackState.Deleting,
            InputFeedbackState.Invalid -> animateColorTo(colors.deleteColor)
        }
        val targetScale = when (state) {
            InputFeedbackState.Neutral -> 1f
            InputFeedbackState.Focused, InputFeedbackState.Typing -> 1.01f
            InputFeedbackState.Deleting, InputFeedbackState.Invalid -> 0.997f
        }
        animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(INPUT_ANIMATION_DURATION_MS)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    setTextColor(colors.textColor)
    setHintTextColor(colors.placeholderColor)
    setVisualColor(colors.neutralStroke)
    minHeight = maxOf(minHeight, dp(46))
    elevation = dpFloat(1.5f)

    setOnFocusChangeListener { _, hasFocus ->
        val value = text?.toString().orEmpty()
        applyState(
            when {
                hasFocus -> InputFeedbackState.Focused
                else -> validationState(value)
            },
        )
    }

    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            error = null
            val value = s?.toString().orEmpty()
            val newLength = value.length

            if (!hasFocus()) {
                previousLength = newLength
                applyState(validationState(value))
                return
            }

            touchedByUser = true
            val state = when {
                newLength < previousLength -> InputFeedbackState.Deleting
                newLength > previousLength || value.isNotBlank() -> InputFeedbackState.Typing
                else -> InputFeedbackState.Focused
            }
            previousLength = newLength
            applyState(state)
        }
    })

    applyState(InputFeedbackState.Neutral)
}

private enum class InputFeedbackState {
    Neutral,
    Focused,
    Typing,
    Deleting,
    Invalid,
}

private data class InputFeedbackColors(
    val textColor: Int,
    val labelColor: Int,
    val placeholderColor: Int,
    val neutralStroke: Int,
    val typeColor: Int,
    val deleteColor: Int,
    val fillColor: Int,
)

private fun inputFeedbackColors(view: View): InputFeedbackColors {
    return InputFeedbackColors(
        textColor = ContextCompat.getColor(view.context, R.color.input_text),
        labelColor = ContextCompat.getColor(view.context, R.color.input_label),
        placeholderColor = ContextCompat.getColor(view.context, R.color.input_placeholder),
        neutralStroke = ContextCompat.getColor(view.context, R.color.input_stroke),
        typeColor = ContextCompat.getColor(view.context, R.color.input_valid),
        deleteColor = ContextCompat.getColor(view.context, R.color.input_invalid),
        fillColor = ContextCompat.getColor(view.context, R.color.card_background),
    )
}

private fun View.dp(value: Int): Int {
    return (value * resources.displayMetrics.density).toInt()
}

private fun View.dpFloat(value: Float): Float {
    return value * resources.displayMetrics.density
}

private const val INPUT_ANIMATION_DURATION_MS = 220L
