package com.pfe.mamacareplus.model.response.community

data class CommentResponse(
    val commentId: Int,
    val postId: Int,
    val femmeId: Int,
    val username: String?,
    val content: String,
    val createdAt: String?
)