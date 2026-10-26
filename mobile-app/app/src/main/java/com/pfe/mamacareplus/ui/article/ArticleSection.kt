package com.pfe.mamacareplus.ui.article

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

class ArticleSection(context: Context) : LinearLayout(context) {

    private val titleView = TextView(context)
    private val row = LinearLayout(context)

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            topMargin = 22.dp
        }

        titleView.includeFontPadding = false
        titleView.textSize = 21f
        titleView.setTypeface(null, Typeface.BOLD)
        titleView.setTextColor(Color.parseColor("#111111"))
        titleView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            marginEnd = 18.dp
        }
        addView(titleView)

        row.orientation = HORIZONTAL
        row.setPadding(0, 12.dp, 18.dp, 2.dp)

        addView(HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = OVER_SCROLL_NEVER
            clipToPadding = false
            addView(row, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    fun bind(title: String, articles: List<ArticleCardItem>) {
        titleView.text = title
        row.removeAllViews()
        articles.forEach { item ->
            row.addView(ArticleCard(context).apply { bind(item) })
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
