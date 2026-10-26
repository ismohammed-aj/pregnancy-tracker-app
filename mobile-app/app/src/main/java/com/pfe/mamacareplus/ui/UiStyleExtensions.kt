package com.pfe.mamacareplus.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.ui.common.applyPremiumInputFeedback

fun LinearLayout.applyListCardStyle(bottomMarginDp: Int = 12) {
    setPadding(dp(16), dp(13), dp(16), dp(13))
    setBackgroundResource(R.drawable.bg_card_rounded)
    elevation = dpFloat(2f)
    layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        setMargins(0, 0, 0, dp(bottomMarginDp))
    }
}

fun LinearLayout.applyNestedCardStyle(bottomMarginDp: Int = 6) {
    setPadding(dp(12), dp(10), dp(12), dp(10))
    setBackgroundResource(R.drawable.bg_option_rounded)
    layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        setMargins(0, dp(6), 0, dp(bottomMarginDp))
    }
}

fun MaterialButton.applyCompactPrimaryButton(topMarginDp: Int = 10) {
    applyCompactButtonBase(topMarginDp)
    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D96B8C"))
    rippleColor = ColorStateList.valueOf(Color.parseColor("#FFF0F6"))
    setTextColor(Color.WHITE)
    strokeWidth = 0
}

fun MaterialButton.applyCompactOutlineButton(topMarginDp: Int = 10, destructive: Boolean = false) {
    applyCompactButtonBase(topMarginDp)
    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFC"))
    rippleColor = ColorStateList.valueOf(Color.parseColor("#FFF0F6"))
    strokeColor = ColorStateList.valueOf(Color.parseColor("#F2E0E8"))
    strokeWidth = dp(1)
    setTextColor(Color.parseColor(if (destructive) "#C24B67" else "#D6336C"))
}

fun EditText.applySoftInputStyle(topMarginDp: Int = 10) {
    setBackgroundResource(R.drawable.bg_input_rounded)
    setPadding(dp(14), dp(10), dp(14), dp(10))
    minHeight = dp(50)
    textSize = 14f
    setTextColor(ContextCompat.getColor(context, R.color.input_text))
    setHintTextColor(ContextCompat.getColor(context, R.color.input_placeholder))
    layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        setMargins(0, dp(topMarginDp), 0, 0)
    }
    applyPremiumInputFeedback()
}

private fun MaterialButton.applyCompactButtonBase(topMarginDp: Int) {
    minHeight = dp(36)
    minimumHeight = dp(36)
    insetTop = 0
    insetBottom = 0
    cornerRadius = dp(11)
    gravity = Gravity.CENTER
    textSize = 11.5f
    setAllCaps(false)
    setTypeface(typeface, Typeface.BOLD)
    iconPadding = dp(6)
    layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        dp(36)
    ).apply {
        setMargins(0, dp(topMarginDp), 0, 0)
    }
}

private fun View.dp(value: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value.toFloat(),
        resources.displayMetrics
    ).toInt()
}

private fun View.dpFloat(value: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value,
        resources.displayMetrics
    )
}
