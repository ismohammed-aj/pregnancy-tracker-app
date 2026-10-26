package com.pfe.mamacareplus.ui.pregnancy

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import java.net.HttpURLConnection
import java.net.URL

private const val BABY_ASSET_PREFIX = "asset://"

enum class ComparisonMode(val label: String) {
    FRUIT("Fruit"),
    ANIMAL("Animal"),
    OBJECT("Objet")
}

data class BabyDevelopmentItem(
    val week: Int,
    val category: ComparisonMode,
    val title: String,
    val subtitle: String,
    val description: String,
    val imageName: String,
    val imageUrl: String?,
    val comparisonName: String,
    val fallbackRes: Int
)

data class BabyWeekData(
    val week: Int,
    val fruitComparison: String,
    val animalComparison: String,
    val objectComparison: String,
    val lengthCm: Double,
    val weightGrams: Double,
    val stage: String,
    val movementLevel: String,
    val development: String,
    val organs: String,
    val motherTip: String,
    val nutrition: String,
    val symptoms: String,
    val visualRes: Int,
    val fruitImageUrl: String?,
    val animalImageUrl: String?,
    val objectImageUrl: String?
) {
    fun comparisonFor(mode: ComparisonMode): String {
        return when (mode) {
            ComparisonMode.FRUIT -> fruitComparison
            ComparisonMode.ANIMAL -> animalComparison
            ComparisonMode.OBJECT -> objectComparison
        }
    }

    fun imageUrlFor(mode: ComparisonMode): String? {
        return when (mode) {
            ComparisonMode.FRUIT -> fruitImageUrl
            ComparisonMode.ANIMAL -> animalImageUrl
            ComparisonMode.OBJECT -> objectImageUrl
        }
    }

    fun itemFor(mode: ComparisonMode): BabyDevelopmentItem {
        val comparison = comparisonFor(mode).toDisplayName()
        val imageUrl = imageUrlFor(mode)
        return BabyDevelopmentItem(
            week = week,
            category = mode,
            title = "Semaine $week",
            subtitle = subtitleFor(mode, comparison),
            description = descriptionFor(mode, comparison),
            imageName = imageUrl?.substringAfterLast('/') ?: "placeholder",
            imageUrl = imageUrl,
            comparisonName = comparison,
            fallbackRes = visualRes
        )
    }

    private fun subtitleFor(mode: ComparisonMode, comparison: String): String {
        return when (mode) {
            ComparisonMode.FRUIT -> "Votre bébé a environ la taille de : $comparison"
            ComparisonMode.ANIMAL -> "Votre bébé peut être comparé à : $comparison"
            ComparisonMode.OBJECT -> "Votre bébé peut être imaginé comme : $comparison"
        }
    }

    private fun descriptionFor(mode: ComparisonMode, comparison: String): String {
        val categoryDetail = when (mode) {
            ComparisonMode.FRUIT ->
                "La catégorie Fruit donne un repère simple pour visualiser sa croissance."
            ComparisonMode.ANIMAL ->
                "La catégorie Animal ajoute une comparaison visuelle adaptée à cette semaine."
            ComparisonMode.OBJECT ->
                "La catégorie Objet utilise un élément du quotidien pour rendre la taille plus concrète."
        }
        return "À la semaine $week, le repère $comparison correspond à ${formatLength(lengthCm)} cm et ${formatWeight(weightGrams)}. $development $categoryDetail"
    }
}

class WeekSelector(context: Context) : HorizontalScrollView(context) {

    private val row = LinearLayout(context)
    private var selectedWeek = 1
    private var onWeekSelected: ((Int) -> Unit)? = null

    init {
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        clipToPadding = false
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(2.dp, 8.dp, 18.dp, 8.dp)
        addView(row, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    fun bind(currentWeek: Int, onSelected: (Int) -> Unit) {
        selectedWeek = currentWeek.coerceIn(1, 40)
        onWeekSelected = onSelected
        row.removeAllViews()

        (1..40).forEach { week ->
            row.addView(createWeekItem(week))
        }

        post {
            row.getChildAt(selectedWeek - 1)?.let { child ->
                smoothScrollTo((child.left - 72.dp).coerceAtLeast(0), 0)
            }
        }
    }

    private fun createWeekItem(week: Int): TextView {
        val selected = week == selectedWeek
        return TextView(context).apply {
            text = "S$week"
            gravity = Gravity.CENTER
            includeFontPadding = false
            textSize = if (selected) 12f else 11f
            setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
            setTextColor(Color.parseColor(if (selected) "#FFFFFF" else "#6F8380"))
            background = rounded(
                color = if (selected) "#24B8AE" else "#F3FAF8",
                strokeColor = if (selected) "#24B8AE" else "#DCEFEB",
                radius = 18.dp
            )
            elevation = if (selected) 3.dp.toFloat() else 0f
            scaleX = if (selected) 1.06f else 1f
            scaleY = if (selected) 1.06f else 1f
            layoutParams = LinearLayout.LayoutParams(54.dp, 38.dp).apply {
                marginEnd = 8.dp
            }
            setOnClickListener {
                if (week != selectedWeek) {
                    selectedWeek = week
                    onWeekSelected?.invoke(week)
                    animate().scaleX(1.08f).scaleY(1.08f).setDuration(160).start()
                }
            }
            applyModernPressAnimation(0.94f)
        }
    }
}

class BabySizeCard(context: Context) : LinearLayout(context) {

    private val weekLabel = TextView(context)
    private val image = ImageView(context)
    private val title = TextView(context)
    private val subtitle = TextView(context)
    private val description = TextView(context)
    private var imageRequestVersion = 0

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        background = roundedGradient("#F2FFFC", "#FFFFFF", 28.dp)
        elevation = 3.dp.toFloat()
        setPadding(20.dp, 18.dp, 20.dp, 20.dp)

        addView(weekLabel.apply {
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#24AFA5"))
            setPadding(12.dp, 6.dp, 12.dp, 6.dp)
            background = rounded("#E4FAF6", "#CDEFEA", 16.dp)
        })

        addView(FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 210.dp).apply {
                topMargin = 10.dp
            }

            addView(image.apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = "Comparaison de taille du bébé"
                layoutParams = FrameLayout.LayoutParams(210.dp, 210.dp, Gravity.CENTER)
            })
        })

        addView(title.apply {
            includeFontPadding = false
            gravity = Gravity.CENTER
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#173C3A"))
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
        })

        addView(subtitle.apply {
            gravity = Gravity.CENTER
            textSize = 13f
            setTextColor(Color.parseColor("#6E8581"))
            setLineSpacing(2f, 1f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = 8.dp
            }
        })

        addView(description.apply {
            gravity = Gravity.CENTER
            textSize = 12.5f
            setTextColor(Color.parseColor("#5F7773"))
            setLineSpacing(3f, 1f)
            maxLines = 6
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = 10.dp
            }
        })
    }

    fun bind(data: BabyWeekData, mode: ComparisonMode) {
        val item = data.itemFor(mode)
        weekLabel.text = item.category.label
        title.text = item.title
        subtitle.text =
            "${item.subtitle}\n${formatLength(data.lengthCm)} cm - ${formatWeight(data.weightGrams)} - ${data.stage}"
        description.text = item.description
        loadComparisonImage(item.imageUrl, item.fallbackRes)
    }

    private fun loadComparisonImage(imageUrl: String?, fallbackRes: Int) {
        val requestVersion = ++imageRequestVersion
        image.animate().cancel()
        image.alpha = 0f
        image.scaleX = 0.96f
        image.scaleY = 0.96f
        image.setImageResource(R.drawable.baby_development_placeholder)

        if (imageUrl.isNullOrBlank()) {
            image.setImageResource(fallbackRes)
            image.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(180).start()
            return
        }

        val resolvedUrl = imageUrl.trim()
        Thread {
            val bitmap = runCatching {
                if (resolvedUrl.startsWith(BABY_ASSET_PREFIX)) {
                    context.assets.open(resolvedUrl.removePrefix(BABY_ASSET_PREFIX)).use {
                        BitmapFactory.decodeStream(it)
                    }
                } else {
                    val connection = (URL(resolvedUrl.toApiUrl()).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 5000
                        readTimeout = 7000
                        instanceFollowRedirects = true
                    }
                    try {
                        if (connection.responseCode !in 200..299) return@runCatching null
                        connection.inputStream.use { BitmapFactory.decodeStream(it) }
                    } finally {
                        connection.disconnect()
                    }
                }
            }.getOrNull()

            image.post {
                if (requestVersion != imageRequestVersion) return@post
                if (bitmap != null) {
                    image.setImageBitmap(bitmap)
                } else {
                    image.setImageResource(fallbackRes)
                }
                image.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(180).start()
            }
        }.start()
    }

    private fun String.toApiUrl(): String {
        val value = trim()
        return when {
            value.startsWith("http://") || value.startsWith("https://") -> value
            value.startsWith("/") -> ApiClient.BASE_URL.trimEnd('/') + value
            else -> ApiClient.BASE_URL + value.trimStart('/')
        }
    }

}

class ComparisonSelector(context: Context) : LinearLayout(context) {

    private val tabsRow = LinearLayout(context)
    private var selectedMode = ComparisonMode.FRUIT
    private var onModeChanged: ((ComparisonMode) -> Unit)? = null

    init {
        orientation = VERTICAL
        background = rounded("#FFFFFF", "#E3F2EF", 22.dp)
        elevation = 2.dp.toFloat()
        setPadding(16.dp, 14.dp, 16.dp, 14.dp)

        addView(LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

            addView(TextView(context).apply {
                text = "Comparaison par image"
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#173C3A"))
            })

            addView(TextView(context).apply {
                text = "Choisissez Animal, Fruit ou Objet"
                textSize = 11.5f
                setTextColor(Color.parseColor("#78908C"))
                setPadding(0, 3.dp, 0, 0)
            })
        })

        addView(tabsRow.apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = 12.dp
            }
        })
    }

    fun bind(selectedMode: ComparisonMode, onModeChanged: (ComparisonMode) -> Unit) {
        this.selectedMode = selectedMode
        this.onModeChanged = onModeChanged
        tabsRow.removeAllViews()

        listOf(ComparisonMode.ANIMAL, ComparisonMode.FRUIT, ComparisonMode.OBJECT)
            .forEachIndexed { index, mode ->
                tabsRow.addView(createTab(mode).apply {
                    layoutParams = LayoutParams(0, 42.dp, 1f).apply {
                        if (index > 0) marginStart = 8.dp
                    }
                })
            }
    }

    private fun createTab(mode: ComparisonMode): TextView {
        val selected = mode == selectedMode
        return TextView(context).apply {
            text = mode.label
            gravity = Gravity.CENTER
            includeFontPadding = false
            textSize = 13f
            setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
            setTextColor(Color.parseColor(if (selected) "#FFFFFF" else "#52716D"))
            background = rounded(
                color = if (selected) "#24B8AE" else "#F3FAF8",
                strokeColor = if (selected) "#24B8AE" else "#DCEFEB",
                radius = 18.dp
            )
            elevation = if (selected) 2.dp.toFloat() else 0f
            setOnClickListener {
                if (mode != selectedMode) {
                    selectedMode = mode
                    onModeChanged?.invoke(mode)
                }
            }
            applyModernPressAnimation(0.95f)
        }
    }
}

class BabyDevelopmentImageStrip(context: Context) : HorizontalScrollView(context) {

    private val row = LinearLayout(context)
    private var selectedWeek = 1

    init {
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        clipToPadding = false
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(2.dp, 10.dp, 18.dp, 8.dp)
        addView(row, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    fun bind(
        items: List<BabyDevelopmentItem>,
        currentWeek: Int,
        onSelected: (BabyDevelopmentItem) -> Unit
    ) {
        selectedWeek = currentWeek.coerceIn(1, 40)
        row.removeAllViews()

        val sortedItems = items.sortedWith(compareBy<BabyDevelopmentItem> { it.week <= 0 }.thenBy { it.week })
        sortedItems.forEach { item ->
            row.addView(createImageItem(item, onSelected))
        }

        post {
            val index = sortedItems.indexOfFirst { it.week == selectedWeek }.coerceAtLeast(0)
            row.getChildAt(index)?.let { child ->
                smoothScrollTo((child.left - 48.dp).coerceAtLeast(0), 0)
            }
        }
    }

    private fun createImageItem(
        item: BabyDevelopmentItem,
        onSelected: (BabyDevelopmentItem) -> Unit
    ): LinearLayout {
        val selected = item.week == selectedWeek
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(9.dp, 9.dp, 9.dp, 8.dp)
            background = rounded(
                color = if (selected) "#E4FAF6" else "#FFFFFF",
                strokeColor = if (selected) "#24B8AE" else "#E3F2EF",
                radius = 20.dp
            )
            elevation = if (selected) 3.dp.toFloat() else 1.dp.toFloat()
            layoutParams = LinearLayout.LayoutParams(94.dp, 122.dp).apply {
                marginEnd = 10.dp
            }

            addView(ImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = "${item.category.label} semaine ${item.week}"
                setBabyDevelopmentImage(item.imageUrl, item.fallbackRes)
                layoutParams = LinearLayout.LayoutParams(66.dp, 66.dp)
            })

            addView(TextView(context).apply {
                text = "S${item.week}"
                gravity = Gravity.CENTER
                includeFontPadding = false
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor(if (selected) "#24AFA5" else "#173C3A"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8.dp
                }
            })

            addView(TextView(context).apply {
                text = item.comparisonName
                gravity = Gravity.CENTER
                textSize = 10.5f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                setTextColor(Color.parseColor("#78908C"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 3.dp
                }
            })

            setOnClickListener {
                if (item.week != selectedWeek) {
                    selectedWeek = item.week
                    onSelected(item)
                    animate().scaleX(1.04f).scaleY(1.04f).setDuration(130).withEndAction {
                        animate().scaleX(1f).scaleY(1f).setDuration(110).start()
                    }.start()
                }
            }
            applyModernPressAnimation(0.96f)
        }
    }
}

class GrowthStatCard(context: Context) : LinearLayout(context) {

    private val value = TextView(context)
    private val label = TextView(context)
    private val note = TextView(context)

    init {
        orientation = VERTICAL
        background = rounded("#FFFFFF", "#E3F2EF", 22.dp)
        elevation = 2.dp.toFloat()
        setPadding(14.dp, 13.dp, 14.dp, 13.dp)

        addView(value.apply {
            includeFontPadding = false
            textSize = 19f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#173C3A"))
        })

        addView(label.apply {
            textSize = 12.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#24AFA5"))
            setPadding(0, 7.dp, 0, 0)
        })

        addView(note.apply {
            textSize = 11.5f
            setTextColor(Color.parseColor("#78908C"))
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            setPadding(0, 5.dp, 0, 0)
        })
    }

    fun bind(valueText: String, labelText: String, noteText: String) {
        value.text = valueText
        label.text = labelText
        note.text = noteText
    }
}

class DevelopmentCard(context: Context) : LinearLayout(context) {

    private val title = TextView(context)
    private val body = TextView(context)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.TOP
        background = rounded("#FFFFFF", "#E8F4F2", 22.dp)
        elevation = 1.5.dp.toFloat()
        setPadding(14.dp, 13.dp, 14.dp, 13.dp)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = 10.dp
        }

        addView(View(context).apply {
            background = rounded("#24B8AE", "#24B8AE", 8.dp)
            layoutParams = LayoutParams(5.dp, LayoutParams.MATCH_PARENT).apply {
                marginEnd = 12.dp
            }
        })

        addView(LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            addView(title)
            addView(body)
        })

        title.textSize = 13.5f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(Color.parseColor("#173C3A"))

        body.textSize = 12f
        body.setTextColor(Color.parseColor("#6F8380"))
        body.setLineSpacing(2f, 1f)
        body.setPadding(0, 6.dp, 0, 0)
    }

    fun bind(titleText: String, bodyText: String) {
        title.text = titleText
        body.text = bodyText
    }
}

class DevelopmentTimeline(context: Context) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        background = rounded("#FFFFFF", "#E8F4F2", 24.dp)
        elevation = 2.dp.toFloat()
        setPadding(16.dp, 14.dp, 16.dp, 8.dp)
    }

    fun bind(items: List<BabyWeekData>) {
        removeAllViews()
        addView(TextView(context).apply {
            text = "Chronologie du développement"
            includeFontPadding = false
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#173C3A"))
            setPadding(0, 0, 0, 10.dp)
        })

        items.forEach { item ->
            addView(LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.TOP
                setPadding(0, 0, 0, 12.dp)

                addView(TextView(context).apply {
                    text = "S${item.week}"
                    gravity = Gravity.CENTER
                    textSize = 11f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.WHITE)
                    background = rounded("#24B8AE", "#24B8AE", 14.dp)
                    layoutParams = LayoutParams(42.dp, 30.dp).apply {
                        marginEnd = 12.dp
                    }
                })

                addView(TextView(context).apply {
                    text = item.stage
                    textSize = 12.5f
                    setTextColor(Color.parseColor("#6F8380"))
                    setTypeface(null, Typeface.BOLD)
                    maxLines = 2
                    ellipsize = TextUtils.TruncateAt.END
                    layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                })
            })
        }
    }
}

private fun ImageView.setBabyDevelopmentImage(imageUrl: String?, fallbackRes: Int) {
    val resolvedUrl = imageUrl?.trim().orEmpty()
    val bitmap = if (resolvedUrl.startsWith(BABY_ASSET_PREFIX)) {
        runCatching {
            context.assets.open(resolvedUrl.removePrefix(BABY_ASSET_PREFIX)).use {
                BitmapFactory.decodeStream(it)
            }
        }.getOrNull()
    } else {
        null
    }

    if (bitmap != null) {
        setImageBitmap(bitmap)
    } else {
        setImageResource(fallbackRes)
    }
}

private fun String.toDisplayName(): String {
    return replace('_', ' ')
        .replace('-', ' ')
        .trim()
        .ifBlank { "référence visuelle" }
}

private fun View.rounded(color: String, strokeColor: String, radius: Int): GradientDrawable {
    return GradientDrawable().apply {
        setColor(Color.parseColor(color))
        setStroke(1.dp, Color.parseColor(strokeColor))
        cornerRadius = radius.toFloat()
    }
}

private fun View.roundedGradient(start: String, end: String, radius: Int): GradientDrawable {
    return GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(Color.parseColor(start), Color.parseColor(end))
    ).apply {
        setStroke(1.dp, Color.parseColor("#D9F1ED"))
        cornerRadius = radius.toFloat()
    }
}

private fun formatWeight(weight: Double): String {
    return if (weight >= 1000.0) {
        String.format("%.2f kg", weight / 1000.0)
    } else if (weight % 1.0 == 0.0) {
        "${weight.toInt()} g"
    } else {
        String.format("%.1f g", weight)
    }
}

private fun formatLength(length: Double): String {
    return if (length % 1.0 == 0.0) {
        length.toInt().toString()
    } else {
        String.format("%.1f", length)
    }
}

private val Int.dp: Int
    get() = (this * android.content.res.Resources.getSystem().displayMetrics.density).toInt()

private val Double.dp: Float
    get() = (this * android.content.res.Resources.getSystem().displayMetrics.density).toFloat()
