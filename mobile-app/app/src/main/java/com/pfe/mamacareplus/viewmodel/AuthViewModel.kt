package com.pfe.mamacareplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.response.auth.AuthResponse
import com.pfe.mamacareplus.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application.applicationContext)

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = authRepository.login(email, password)

            _authState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Connexion impossible") }
            )
        }
    }

    fun register(
        email: String,
        password: String,
        username: String,
        birthDate: String?,
        avatar: String? = null
    ) {
        _authState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = authRepository.register(
                email = email,
                password = password,
                username = username,
                birthDate = birthDate,
                avatar = avatar
            )

            _authState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Création du compte impossible") }
            )
        }
    }

    fun resetState() {
        _authState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val data: AuthResponse) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
