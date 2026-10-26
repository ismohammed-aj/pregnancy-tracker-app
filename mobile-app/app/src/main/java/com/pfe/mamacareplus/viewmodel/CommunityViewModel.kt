package com.pfe.mamacareplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.response.community.CommunityPostResponse
import com.pfe.mamacareplus.repository.community.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = CommunityRepository(application)

    private val _communityState =
        MutableStateFlow<CommunityUiState>(CommunityUiState.Loading)

    val communityState: StateFlow<CommunityUiState> = _communityState

    fun loadPosts() {
        _communityState.value = CommunityUiState.Loading

        viewModelScope.launch {
            val result = repository.getAllPosts()

            _communityState.value = result.fold(
                onSuccess = { posts ->
                    if (posts.isEmpty()) {
                        CommunityUiState.Empty
                    } else {
                        CommunityUiState.Success(posts)
                    }
                },
                onFailure = {
                    CommunityUiState.Error(it.message ?: "Impossible de charger les publications")
                }
            )
        }
    }

    fun createPost(content: String, imageUrl: String?) {
        viewModelScope.launch {
            val result = repository.createPost(content, imageUrl)

            result.onSuccess {
                loadPosts()
            }.onFailure {
                _communityState.value =
                    CommunityUiState.Error(it.message ?: "Impossible de créer la publication")
            }
        }
    }

    fun deletePost(postId: Int) {
        viewModelScope.launch {
            val result = repository.deletePost(postId)

            result.onSuccess {
                loadPosts()
            }.onFailure {
                _communityState.value =
                    CommunityUiState.Error(it.message ?: "Impossible de supprimer la publication")
            }
        }
    }
}

sealed class CommunityUiState {
    object Loading : CommunityUiState()
    object Empty : CommunityUiState()
    data class Success(val posts: List<CommunityPostResponse>) : CommunityUiState()
    data class Error(val message: String) : CommunityUiState()
}
