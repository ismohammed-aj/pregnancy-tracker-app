package com.pfe.mamacareplus.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.response.profile.FemmeProfileResponse
import com.pfe.mamacareplus.repository.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProfileRepository(application)

    private val _profileState =
        MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)

    val profileState: StateFlow<ProfileUiState> = _profileState

    private val _passwordChangeState =
        MutableStateFlow<PasswordChangeUiState>(PasswordChangeUiState.Idle)

    val passwordChangeState: StateFlow<PasswordChangeUiState> = _passwordChangeState

    fun loadProfile() {

        viewModelScope.launch {

            val result = repository.getProfile()

            _profileState.value = result.fold(
                onSuccess = {
                    ProfileUiState.Success(it)
                },
                onFailure = {
                    ProfileUiState.Error(it.message ?: "Erreur inconnue")
                }
            )
        }
    }

    fun updateProfile(username: String, birthDate: String?) {
        _profileState.value = ProfileUiState.Saving

        viewModelScope.launch {
            val result = repository.updateProfile(username, birthDate)

            _profileState.value = result.fold(
                onSuccess = {
                    ProfileUiState.Success(it)
                },
                onFailure = {
                    ProfileUiState.Error(it.message ?: "Impossible de modifier le profil")
                }
            )
        }
    }

    fun uploadAvatar(uri: Uri) {
        _profileState.value = ProfileUiState.Saving

        viewModelScope.launch {
            val result = repository.uploadAvatar(uri)

            _profileState.value = result.fold(
                onSuccess = {
                    ProfileUiState.Success(it)
                },
                onFailure = {
                    ProfileUiState.Error(it.message ?: "Impossible d'envoyer la photo")
                }
            )
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        _passwordChangeState.value = PasswordChangeUiState.Saving

        viewModelScope.launch {
            val result = repository.changePassword(
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )

            _passwordChangeState.value = result.fold(
                onSuccess = { PasswordChangeUiState.Success(it) },
                onFailure = { PasswordChangeUiState.Error(it.message ?: "Impossible de modifier le mot de passe") }
            )
        }
    }

    fun resetPasswordChangeState() {
        _passwordChangeState.value = PasswordChangeUiState.Idle
    }
}

sealed class ProfileUiState {

    object Loading : ProfileUiState()
    object Saving : ProfileUiState()

    data class Success(
        val profile: FemmeProfileResponse
    ) : ProfileUiState()

    data class Error(
        val message: String
    ) : ProfileUiState()
}

sealed class PasswordChangeUiState {
    object Idle : PasswordChangeUiState()
    object Saving : PasswordChangeUiState()
    data class Success(val message: String) : PasswordChangeUiState()
    data class Error(val message: String) : PasswordChangeUiState()
}
