package com.pfe.mamacareplus.repository.community

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.community.CommunityApiService
import com.pfe.mamacareplus.model.request.community.CommunityPostRequest
import com.pfe.mamacareplus.model.response.community.CommunityPostResponse

class CommunityRepository(context: Context) {

    private val api = ApiClient
        .create(context)
        .create(CommunityApiService::class.java)

    suspend fun getAllPosts(): Result<List<CommunityPostResponse>> {
        return try {
            val response = api.getAllPosts()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger les publications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(
        content: String,
        imageUrl: String?
    ): Result<CommunityPostResponse> {
        return try {
            val response = api.createPost(
                CommunityPostRequest(
                    content = content,
                    imageUrl = imageUrl
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de créer la publication"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: Int): Result<Unit> {
        return try {
            val response = api.deletePost(postId)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Impossible de supprimer la publication"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
