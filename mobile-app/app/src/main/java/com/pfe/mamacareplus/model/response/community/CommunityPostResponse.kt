package com.pfe.mamacareplus.model.response.community

data class CommunityPostResponse(
    val postId: Int,
    val femmeId: Int,
    val username: String?,
    val content: String,
    val imageUrl: String?,
    val createdAt: String?,
    val updatedAt: String?
)