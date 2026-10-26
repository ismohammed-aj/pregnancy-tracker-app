package com.pfe.mamacareplus.ui.article

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.shape.ShapeAppearanceModel
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.databinding.ActivityArticleDetailBinding
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.playSoftEntrance

class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.playSoftEntrance()

        val category = intent.getStringExtra(EXTRA_CATEGORY).orEmpty().ifBlank { "Conseils" }
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val content = intent.getStringExtra(EXTRA_CONTENT).orEmpty()
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
        val videoTitle = intent.getStringExtra(EXTRA_VIDEO_TITLE)
        val readingTime = intent.getIntExtra(EXTRA_READING_TIME, 3)
        val visual = articleVisualFor(category, imageUrl)

        binding.tvDetailCategory.text = category
        binding.tvDetailTitle.text = title
        binding.tvDetailReadingTime.text = "Lecture $readingTime min"
        binding.tvDetailContent.text = content
        binding.ivDetailImage.setArticleImage(category, imageUrl)
        binding.ivDetailImage.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(18.dp.toFloat())
            .build()
        binding.ivDetailIcon.setImageResource(visual.iconRes)
        setupVideoSection(videoUrl, videoTitle, category, imageUrl, visual.imageRes)
        binding.btnBack.applyModernPressAnimation()
        binding.btnPlayVideo.applyModernPressAnimation()
        binding.videoThumbnailLayer.applyModernPressAnimation(0.98f)
        binding.btnBack.setOnClickListener { finish() }
    }

    override fun onStop() {
        binding.videoView.stopPlayback()
        super.onStop()
    }

    private fun setupVideoSection(
        videoUrl: String?,
        videoTitle: String?,
        category: String?,
        imageUrl: String?,
        thumbnailRes: Int
    ) {
        if (videoUrl.isNullOrBlank()) {
            binding.videoSection.visibility = View.GONE
            return
        }

        binding.videoSection.visibility = View.VISIBLE
        binding.tvVideoTitle.text = videoTitle?.takeIf { it.isNotBlank() } ?: "Vidéo la plus regardée"
        binding.ivVideoThumbnail.setImageResource(thumbnailRes)
        binding.ivVideoThumbnail.setArticleImage(category, imageUrl)
        binding.videoView.visibility = View.GONE
        binding.tvVideoStatus.visibility = View.GONE

        val resolvedVideoUrl = videoUrl.toApiUrl()
        binding.videoThumbnailLayer.setOnClickListener { playVideo(resolvedVideoUrl) }
        binding.btnPlayVideo.setOnClickListener { playVideo(resolvedVideoUrl) }
    }

    private fun playVideo(videoUrl: String) {
        binding.tvVideoStatus.visibility = View.GONE
        binding.videoThumbnailLayer.visibility = View.GONE
        binding.videoView.visibility = View.VISIBLE

        val controller = MediaController(this).apply {
            setAnchorView(binding.videoView)
        }

        binding.videoView.setMediaController(controller)
        binding.videoView.setOnPreparedListener {
            binding.tvVideoStatus.visibility = View.GONE
            binding.videoView.start()
        }
        binding.videoView.setOnErrorListener { _, _, _ ->
            binding.videoView.stopPlayback()
            binding.videoView.visibility = View.GONE
            binding.videoThumbnailLayer.visibility = View.VISIBLE
            binding.tvVideoStatus.text = "Vidéo indisponible pour le moment"
            binding.tvVideoStatus.visibility = View.VISIBLE
            true
        }
        binding.videoView.setVideoURI(Uri.parse(videoUrl))
        binding.videoView.start()
    }

    private fun String.toApiUrl(): String {
        val value = trim()
        return when {
            value.startsWith("http://") || value.startsWith("https://") -> value
            value.startsWith("/") -> ApiClient.BASE_URL.trimEnd('/') + value
            else -> ApiClient.BASE_URL + value.trimStart('/')
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_CATEGORY = "article_category"
        const val EXTRA_TITLE = "article_title"
        const val EXTRA_READING_TIME = "article_reading_time"
        const val EXTRA_CONTENT = "article_content"
        const val EXTRA_IMAGE_URL = "article_image_url"
        const val EXTRA_VIDEO_URL = "article_video_url"
        const val EXTRA_VIDEO_TITLE = "article_video_title"
    }
}
