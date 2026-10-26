package com.pfe.mamacareplus.api.article

import com.pfe.mamacareplus.model.response.article.ArticleResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ArticleApiService {

    @GET("api/articles")
    suspend fun getAllArticles(): Response<List<ArticleResponse>>

    @GET("api/articles/{articleId}")
    suspend fun getArticleById(
        @Path("articleId") articleId: Int
    ): Response<ArticleResponse>

    @GET("api/articles/category/{category}")
    suspend fun getArticlesByCategory(
        @Path("category") category: String
    ): Response<List<ArticleResponse>>

    @GET("api/articles/search")
    suspend fun searchArticles(
        @Query("keyword") keyword: String
    ): Response<List<ArticleResponse>>
}