package com.pfe.mamacareplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.response.community.CommentResponse
import com.pfe.mamacareplus.repository.community.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = CommentRepository(application)

    private val _commentState =
        MutableStateFlow<CommentUiState>(CommentUiState.Loading)

    val commentState: StateFlow<CommentUiState> = _commentState

    fun loadComments(postId: Int) {

        _commentState.value = CommentUiState.Loading

        viewModelScope.launch {

            val result = repository.getComments(postId)

            _commentState.value = result.fold(
                onSuccess = { CommentUiState.Success(it) },
                onFailure = {
                    CommentUiState.Error(it.message ?: "Impossible de charger les commentaires")
                }
            )
        }
    }

    fun createComment(postId: Int, content: String) {

        viewModelScope.launch {

            val result = repository.createComment(postId, content)

            result.onSuccess {
                loadComments(postId)
            }.onFailure {
                _commentState.value =
                    CommentUiState.Error(it.message ?: "Impossible d'ajouter le commentaire")
            }
        }
    }

    fun deleteComment(postId: Int, commentId: Int) {

        viewModelScope.launch {

            val result = repository.deleteComment(commentId)

            result.onSuccess {
                loadComments(postId)
            }.onFailure {
                _commentState.value =
                    CommentUiState.Error(it.message ?: "Impossible de supprimer le commentaire")
            }
        }
    }
}

sealed class CommentUiState {
    object Loading : CommentUiState()
    data class Success(val comments: List<CommentResponse>) : CommentUiState()
    data class Error(val message: String) : CommentUiState()
}
