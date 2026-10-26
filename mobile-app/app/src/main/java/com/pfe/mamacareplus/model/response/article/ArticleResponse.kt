package com.pfe.mamacareplus.model.response.article

data class ArticleResponse(
    val articleId: Int,
    val title: String,
    val category: String?,
    val content: String,
    val imageUrl: String?,
    val videoUrl: String?,
    val videoTitle: String?,
    val readingTimeMinutes: Int?,
    val createdAt: String?
)
