package com.pfe.mamacareplus.api.community

import com.pfe.mamacareplus.model.request.community.CommunityPostRequest
import com.pfe.mamacareplus.model.response.community.CommunityPostResponse
import retrofit2.Response
import retrofit2.http.*

interface CommunityApiService {

    @GET("api/community-posts")
    suspend fun getAllPosts(): Response<List<CommunityPostResponse>>

    @POST("api/community-posts")
    suspend fun createPost(
        @Body request: CommunityPostRequest
    ): Response<CommunityPostResponse>

    @PUT("api/community-posts/{postId}")
    suspend fun updatePost(
        @Path("postId") postId: Int,
        @Body request: CommunityPostRequest
    ): Response<CommunityPostResponse>

    @DELETE("api/community-posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: Int
    ): Response<Unit>
}