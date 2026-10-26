package com.pfe.mamacareplus.repository.community

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.community.CommentApiService
import com.pfe.mamacareplus.model.request.community.CommentRequest
import com.pfe.mamacareplus.model.response.community.CommentResponse

class CommentRepository(context: Context) {

    private val api = ApiClient
        .create(context)
        .create(CommentApiService::class.java)

    suspend fun getComments(postId: Int): Result<List<CommentResponse>> {
        return try {

            val response = api.getCommentsByPost(postId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger les commentaires"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createComment(
        postId: Int,
        content: String
    ): Result<CommentResponse> {

        return try {

            val response = api.createComment(
                CommentRequest(
                    postId = postId,
                    content = content
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible d'ajouter le commentaire"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(commentId: Int): Result<Unit> {

        return try {

            val response = api.deleteComment(commentId)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Impossible de supprimer le commentaire"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
