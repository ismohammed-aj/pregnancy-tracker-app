package com.pfe.mamacareplus.repository.article

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.article.ArticleApiService
import com.pfe.mamacareplus.model.response.article.ArticleResponse

class ArticleRepository(context: Context) {

    private val api = ApiClient
        .create(context)
        .create(ArticleApiService::class.java)

    suspend fun getAllArticles(): Result<List<ArticleResponse>> {
        return try {
            val response = api.getAllArticles()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger les articles"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchArticles(keyword: String): Result<List<ArticleResponse>> {
        return try {
            val response = api.searchArticles(keyword)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Recherche impossible"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getArticlesByCategory(category: String): Result<List<ArticleResponse>> {
        return try {
            val response = api.getArticlesByCategory(category)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger les articles de cette catégorie"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
