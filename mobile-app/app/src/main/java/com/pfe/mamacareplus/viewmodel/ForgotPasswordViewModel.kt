package com.pfe.mamacareplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application.applicationContext)

    private val _resetState = MutableStateFlow<PasswordResetUiState>(PasswordResetUiState.Idle)
    val resetState: StateFlow<PasswordResetUiState> = _resetState

    fun sendCode(email: String) {
        _resetState.value = PasswordResetUiState.Loading

        viewModelScope.launch {
            val result = authRepository.sendPasswordResetCode(email)
            _resetState.value = result.fold(
                onSuccess = { PasswordResetUiState.Success(it) },
                onFailure = { PasswordResetUiState.Error(it.message ?: "Impossible d'envoyer le code.") }
            )
        }
    }

    fun verifyCode(email: String, code: String) {
        _resetState.value = PasswordResetUiState.Loading

        viewModelScope.launch {
            val result = authRepository.verifyPasswordResetCode(email, code)
            _resetState.value = result.fold(
                onSuccess = { PasswordResetUiState.Success(it) },
                onFailure = { PasswordResetUiState.Error(it.message ?: "Code incorrect ou expiré.") }
            )
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        _resetState.value = PasswordResetUiState.Loading

        viewModelScope.launch {
            val result = authRepository.resetPassword(email, code, newPassword)
            _resetState.value = result.fold(
                onSuccess = { PasswordResetUiState.Success(it) },
                onFailure = { PasswordResetUiState.Error(it.message ?: "Impossible de modifier le mot de passe.") }
            )
        }
    }

    fun resetState() {
        _resetState.value = PasswordResetUiState.Idle
    }
}

sealed class PasswordResetUiState {
    object Idle : PasswordResetUiState()
    object Loading : PasswordResetUiState()
    data class Success(val message: String) : PasswordResetUiState()
    data class Error(val message: String) : PasswordResetUiState()
}
