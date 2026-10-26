package com.pfe.mamacareplus.ui.article

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.model.response.article.ArticleResponse
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation

data class ArticleCardItem(
    val article: ArticleResponse,
    val badge: String? = null
)

class ArticleCard(context: Context) : FrameLayout(context) {

    private val imageView = ShapeableImageView(context)
    private val badgeView = TextView(context)
    private val titleView = TextView(context)
    private val iconBadge = FrameLayout(context)
    private val iconView = ImageView(context)

    init {
        background = GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            cornerRadius = 24.dp.toFloat()
        }
        clipToOutline = true
        elevation = 3.dp.toFloat()
        isClickable = true
        isFocusable = true
        foreground = context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground)).use {
            it.getDrawable(0)
        }
        applyModernPressAnimation()
        layoutParams = MarginLayoutParams(210.dp, 248.dp).apply {
            marginEnd = 12.dp
        }

        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(24.dp.toFloat())
            .build()
        addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        addView(FrameLayout(context).apply {
            setBackgroundResource(R.drawable.bg_article_card_scrim)
        }, LayoutParams(LayoutParams.MATCH_PARENT, 116.dp, Gravity.BOTTOM))

        badgeView.setTextColor(Color.WHITE)
        badgeView.textSize = 10f
        badgeView.setTypeface(null, Typeface.BOLD)
        badgeView.setBackgroundResource(R.drawable.bg_category_chip_selected)
        badgeView.setPadding(10.dp, 4.dp, 10.dp, 4.dp)
        addView(badgeView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START).apply {
            setMargins(12.dp, 12.dp, 12.dp, 12.dp)
        })

        iconBadge.setBackgroundResource(R.drawable.bg_article_icon_badge)
        iconBadge.addView(iconView, LayoutParams(16.dp, 16.dp, Gravity.CENTER))
        addView(iconBadge, LayoutParams(32.dp, 32.dp, Gravity.TOP or Gravity.END).apply {
            setMargins(12.dp, 12.dp, 12.dp, 12.dp)
        })

        titleView.setTextColor(Color.WHITE)
        titleView.textSize = 17f
        titleView.setTypeface(null, Typeface.BOLD)
        titleView.maxLines = 3
        titleView.ellipsize = TextUtils.TruncateAt.END
        titleView.setLineSpacing(2f, 1f)
        addView(titleView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.START).apply {
            setMargins(16.dp, 0, 16.dp, 18.dp)
        })
    }

    fun bind(item: ArticleCardItem) {
        val visual = context.articleVisualFor(item.article.category, item.article.imageUrl)
        imageView.setArticleImage(item.article.category, item.article.imageUrl)
        iconView.setImageResource(visual.iconRes)
        iconView.setColorFilter(Color.parseColor("#D6336C"))
        titleView.text = item.article.title
        badgeView.text = item.badge ?: (item.article.category ?: "Conseil")
        setOnClickListener { item.article.openArticleDetail(context) }
    }

    private inline fun <T> android.content.res.TypedArray.use(block: (android.content.res.TypedArray) -> T): T {
        return try {
            block(this)
        } finally {
            recycle()
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
