package com.pfe.mamacareplus.api.community

import com.pfe.mamacareplus.model.request.community.CommentRequest
import com.pfe.mamacareplus.model.response.community.CommentResponse
import retrofit2.Response
import retrofit2.http.*

interface CommentApiService {

    @GET("api/comments/post/{postId}")
    suspend fun getCommentsByPost(
        @Path("postId") postId: Int
    ): Response<List<CommentResponse>>

    @POST("api/comments")
    suspend fun createComment(
        @Body request: CommentRequest
    ): Response<CommentResponse>

    @PUT("api/comments/{commentId}")
    suspend fun updateComment(
        @Path("commentId") commentId: Int,
        @Body request: CommentRequest
    ): Response<CommentResponse>

    @DELETE("api/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Int
    ): Response<Unit>
}