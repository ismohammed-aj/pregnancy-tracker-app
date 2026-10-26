package com.pfe.mamacareplus.model.request.community

data class CommentRequest(
    val postId: Int,
    val content: String
)