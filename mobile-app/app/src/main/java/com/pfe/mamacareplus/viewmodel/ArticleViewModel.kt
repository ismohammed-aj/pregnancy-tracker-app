package com.pfe.mamacareplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.response.article.ArticleResponse
import com.pfe.mamacareplus.repository.article.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArticleViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ArticleRepository(application)

    private val _articleState =
        MutableStateFlow<ArticleUiState>(ArticleUiState.Loading)

    val articleState: StateFlow<ArticleUiState> = _articleState

    fun loadArticles() {
        _articleState.value = ArticleUiState.Loading

        viewModelScope.launch {
            val result = repository.getAllArticles()

            _articleState.value = result.fold(
                onSuccess = { articles ->
                    if (articles.isEmpty()) {
                        ArticleUiState.Empty
                    } else {
                        ArticleUiState.Success(articles)
                    }
                },
                onFailure = {
                    ArticleUiState.Error(it.message ?: "Impossible de charger les articles")
                }
            )
        }
    }

    fun searchArticles(keyword: String) {
        if (keyword.isBlank()) {
            loadArticles()
            return
        }

        _articleState.value = ArticleUiState.Loading

        viewModelScope.launch {
            val result = repository.searchArticles(keyword)

            _articleState.value = result.fold(
                onSuccess = { articles ->
                    if (articles.isEmpty()) {
                        ArticleUiState.Empty
                    } else {
                        ArticleUiState.Success(articles)
                    }
                },
                onFailure = {
                    ArticleUiState.Error(it.message ?: "Recherche impossible")
                }
            )
        }
    }

    fun loadArticlesByCategory(category: String) {
        _articleState.value = ArticleUiState.Loading

        viewModelScope.launch {
            val result = repository.getArticlesByCategory(category)

            _articleState.value = result.fold(
                onSuccess = { articles ->
                    if (articles.isEmpty()) {
                        ArticleUiState.Empty
                    } else {
                        ArticleUiState.Success(articles)
                    }
                },
                onFailure = {
                    ArticleUiState.Error(it.message ?: "Impossible de charger la catégorie")
                }
            )
        }
    }
}

sealed class ArticleUiState {
    object Loading : ArticleUiState()
    object Empty : ArticleUiState()
    data class Success(val articles: List<ArticleResponse>) : ArticleUiState()
    data class Error(val message: String) : ArticleUiState()
}
