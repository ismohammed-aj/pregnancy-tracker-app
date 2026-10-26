package com.pfe.mamacareplus.ui.common

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.pfe.mamacareplus.R

class MamaCareFormDialog(
    context: Context,
    private val title: String,
    private val content: View,
    private val positiveText: String = "Enregistrer",
    private val negativeText: String = "Annuler",
    private val onPositiveClick: (MamaCareFormDialog) -> Unit,
) : Dialog(context) {

    private lateinit var rootView: View
    private lateinit var cardView: View
    private var isAnimatingDismiss = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(createDialogView())
        setCanceledOnTouchOutside(true)
    }

    override fun show() {
        super.show()

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            setDimAmount(0.22f)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        rootView.alpha = 0f
        cardView.alpha = 0f
        cardView.scaleX = 0.94f
        cardView.scaleY = 0.94f
        rootView.animate()
            .alpha(1f)
            .setDuration(220L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        cardView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(240L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    override fun dismiss() {
        if (!isShowing || isAnimatingDismiss) {
            super.dismiss()
            return
        }

        isAnimatingDismiss = true
        rootView.animate()
            .alpha(0f)
            .setDuration(210L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        cardView.animate()
            .alpha(0f)
            .scaleX(0.96f)
            .scaleY(0.96f)
            .setDuration(210L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction { finishDismiss() }
            .start()
    }

    private fun finishDismiss() {
        isAnimatingDismiss = false
        super.dismiss()
    }

    private fun createDialogView(): View {
        rootView = FrameLayout(context).apply {
            setPadding(dp(20), dp(18), dp(20), dp(18))
            clipToPadding = false
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(20))
            background = roundedBackground(
                fillColor = ContextCompat.getColor(context, R.color.dialog_surface),
                strokeColor = ContextCompat.getColor(context, R.color.dialog_stroke),
                radiusDp = 28,
            )
            elevation = dpFloat(14f)
            translationZ = dpFloat(2f)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            )
        }
        cardView = card

        card.addView(TextView(context).apply {
            text = title
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, R.color.dark_text))
            setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        })

        card.addView(MaxHeightScrollView(context).apply {
            overScrollMode = ScrollView.OVER_SCROLL_IF_CONTENT_SCROLLS
            isFillViewport = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(12)
            }
            addView(content, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ))
        })

        card.addView(buttonRow())
        (rootView as FrameLayout).addView(card)
        return rootView
    }

    private fun buttonRow(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(22)
            }

            addView(dialogButton(negativeText, primary = false).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(46), 1f).apply {
                    marginEnd = dp(8)
                }
                setOnClickListener { dismiss() }
            })

            addView(dialogButton(positiveText, primary = true).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(46), 1f).apply {
                    marginStart = dp(8)
                }
                setOnClickListener { onPositiveClick(this@MamaCareFormDialog) }
            })
        }
    }

    private fun dialogButton(textValue: String, primary: Boolean): MaterialButton {
        return MaterialButton(context).apply {
            text = textValue
            setAllCaps(false)
            setTypeface(typeface, Typeface.BOLD)
            textSize = 13f
            minHeight = dp(46)
            minimumHeight = dp(46)
            insetTop = 0
            insetBottom = 0
            cornerRadius = dp(16)
            rippleColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.soft_accent))

            if (primary) {
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary))
                setTextColor(Color.WHITE)
                strokeWidth = 0
            } else {
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dialog_surface))
                setTextColor(ContextCompat.getColor(context, R.color.secondary_text))
                strokeWidth = dp(1)
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dialog_button_outline))
            }

            applyModernPressAnimation()
        }
    }

    private fun roundedBackground(fillColor: Int, strokeColor: Int, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpFloat(radiusDp.toFloat())
            setColor(fillColor)
            setStroke(dp(1), strokeColor)
        }
    }

    private fun dp(value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }

    private fun dpFloat(value: Float): Float {
        return value * context.resources.displayMetrics.density
    }

    private class MaxHeightScrollView(context: Context) : ScrollView(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val maxHeight = (resources.displayMetrics.heightPixels * 0.62f).toInt()
            val limitedHeightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
            super.onMeasure(widthMeasureSpec, limitedHeightSpec)
        }
    }
}
