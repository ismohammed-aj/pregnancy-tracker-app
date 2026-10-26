package com.pfe.mamacareplus.ui.article

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.model.response.article.ArticleResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.util.Locale
import java.net.URL

data class ArticleVisual(
    val imageRes: Int,
    val iconRes: Int,
    val remoteUrl: String?
)

fun Context.articleVisualFor(category: String?, imageUrl: String?): ArticleVisual {
    val key = imageUrl.articleImageKey()
    val lookup = "$key ${category.orEmpty()}".toSearchKey()
    val generatedRes = if (imageUrl.isLocalArticleImage() && key.isNotBlank()) {
        resources.getIdentifier(key, "drawable", packageName)
    } else {
        0
    }

    return ArticleVisual(
        imageRes = generatedRes.takeIf { it != 0 } ?: fallbackArticleImage(key, lookup),
        remoteUrl = imageUrl.toArticleRemoteUrl(),
        iconRes = when {
            "hydration" in lookup -> R.drawable.ic_water_drop_24
            "nutrition" in lookup -> R.drawable.ic_nutrition_24
            "signes" in lookup || "warning" in lookup || "risque" in lookup -> R.drawable.ic_warning_24
            "poids" in lookup || "tracking" in lookup -> R.drawable.ic_tracking_chart_24
            "bebe" in lookup -> R.drawable.ic_baby_24
            "activite" in lookup -> R.drawable.ic_activity_walk_24
            "rdv" in lookup || "rendez" in lookup || "consultation" in lookup || "echographie" in lookup -> R.drawable.ic_calendar_24
            "mental" in lookup || "sante" in lookup -> R.drawable.ic_health_heart_24
            else -> R.drawable.ic_article_24
        }
    )
}

fun ImageView.setArticleImage(category: String?, imageUrl: String?) {
    val visual = context.articleVisualFor(category, imageUrl)
    scaleType = ImageView.ScaleType.CENTER_CROP
    setImageResource(visual.imageRes)

    val remoteUrl = visual.remoteUrl ?: return
    tag = remoteUrl

    CoroutineScope(Dispatchers.IO).launch {
        val bitmap = runCatching {
            URL(remoteUrl).openStream().use { BitmapFactory.decodeStream(it) }
        }.getOrNull()

        withContext(Dispatchers.Main) {
            if (tag == remoteUrl && bitmap != null) {
                setImageBitmap(bitmap)
            }
        }
    }
}

fun ArticleResponse.openArticleDetail(context: Context) {
    context.startActivity(
        Intent(context, ArticleDetailActivity::class.java).apply {
            putExtra(ArticleDetailActivity.EXTRA_CATEGORY, category ?: "Conseils")
            putExtra(ArticleDetailActivity.EXTRA_TITLE, title)
            putExtra(ArticleDetailActivity.EXTRA_READING_TIME, readingTimeMinutes ?: 3)
            putExtra(ArticleDetailActivity.EXTRA_CONTENT, content)
            putExtra(ArticleDetailActivity.EXTRA_IMAGE_URL, imageUrl)
            putExtra(ArticleDetailActivity.EXTRA_VIDEO_URL, videoUrl)
            putExtra(ArticleDetailActivity.EXTRA_VIDEO_TITLE, videoTitle)
        }
    )
}

fun ArticleResponse.previewText(maxLength: Int): String {
    val clean = content.replace(Regex("\\s+"), " ").trim()
    return if (clean.length <= maxLength) clean else clean.take(maxLength - 3).trimEnd() + "..."
}

private fun Context.fallbackArticleImage(key: String, lookup: String): Int {
    return when (key) {
        "article_nutrition_assiette" -> R.drawable.fallback_article_nutrition_assiette
        "article_nutrition_micronutrients" -> R.drawable.fallback_article_nutrition_micronutrients
        "article_nutrition_nausees" -> R.drawable.fallback_article_nutrition_nausees
        "article_hydration" -> R.drawable.fallback_article_hydration
        "article_sante_signes" -> R.drawable.fallback_article_sante_signes
        "article_sante_sommeil" -> R.drawable.fallback_article_sante_sommeil
        "article_sante_poids" -> R.drawable.fallback_article_sante_poids
        "article_bebe_developpement" -> R.drawable.fallback_article_bebe_developpement
        "article_bebe_mouvements" -> R.drawable.fallback_article_bebe_mouvements
        "article_activite_douce" -> R.drawable.fallback_article_activite_douce
        "article_activite_dos" -> R.drawable.fallback_article_activite_dos
        "article_mental_respiration" -> R.drawable.fallback_article_mental_respiration
        "article_mental_soutien" -> R.drawable.fallback_article_mental_soutien
        "article_rdv_consultation" -> R.drawable.fallback_article_rdv_consultation
        "article_rdv_echographie" -> R.drawable.fallback_article_rdv_echographie
        "article_conseils_valise" -> R.drawable.fallback_article_conseils_valise
        "article_conseils_retour" -> R.drawable.fallback_article_conseils_retour
        else -> when {
            "hydration" in lookup -> R.drawable.fallback_article_hydration
            "nutrition" in lookup -> R.drawable.fallback_article_nutrition_assiette
            "bebe" in lookup -> R.drawable.fallback_article_bebe_developpement
            "activite" in lookup -> R.drawable.fallback_article_activite_douce
            "mental" in lookup -> R.drawable.fallback_article_mental_respiration
            "rdv" in lookup || "rendez" in lookup -> R.drawable.fallback_article_rdv_consultation
            "sante" in lookup -> R.drawable.fallback_article_sante_signes
            else -> R.drawable.bg_article_icon_area
        }
    }
}

private fun String.toSearchKey(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.ROOT)
}

fun String?.articleImageKey(): String {
    val value = this?.trim().orEmpty()
    if (value.isBlank()) return ""

    return when {
        value.startsWith("local://") -> value.removePrefix("local://")
        else -> value.substringAfterLast('/').substringBeforeLast('.')
    }
}

private fun String?.isLocalArticleImage(): Boolean {
    return this?.trim().orEmpty().startsWith("local://")
}

private fun String?.toArticleRemoteUrl(): String? {
    val value = this?.trim().orEmpty()
    if (value.isBlank() || value.startsWith("local://")) return null

    return when {
        value.startsWith("http://") || value.startsWith("https://") -> value
        value.startsWith("/") -> ApiClient.BASE_URL.trimEnd('/') + value
        else -> ApiClient.BASE_URL + value.trimStart('/')
    }
}
