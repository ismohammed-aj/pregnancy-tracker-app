package com.pfe.mamacareplus.model.request.community

data class CommunityPostRequest(
    val content: String,
    val imageUrl: String? = null
)