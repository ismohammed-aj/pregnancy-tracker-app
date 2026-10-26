package com.pfe.mamacareplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.response.profile.PregnancyProfileResponse
import com.pfe.mamacareplus.repository.profile.PregnancyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PregnancyViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = PregnancyRepository(application)

    private val _pregnancyState =
        MutableStateFlow<PregnancyUiState>(PregnancyUiState.Loading)

    val pregnancyState: StateFlow<PregnancyUiState> = _pregnancyState

    fun loadPregnancyProfile() {
        _pregnancyState.value = PregnancyUiState.Loading

        viewModelScope.launch {
            val result = repository.getPregnancyProfile()

            _pregnancyState.value = result.fold(
                onSuccess = { PregnancyUiState.Success(it) },
                onFailure = { PregnancyUiState.Error(it.message ?: "Erreur inconnue") }
            )
        }
    }

    fun createPregnancyProfile(
        startDate: String,
        dueDate: String,
        healthStatus: String?,
        notes: String?
    ) {
        _pregnancyState.value = PregnancyUiState.Loading

        viewModelScope.launch {
            val result = repository.createPregnancyProfile(
                startDate = startDate,
                dueDate = dueDate,
                healthStatus = healthStatus,
                notes = notes
            )

            _pregnancyState.value = result.fold(
                onSuccess = { PregnancyUiState.Success(it) },
                onFailure = { PregnancyUiState.Error(it.message ?: "Création impossible") }
            )
        }
    }

    fun updatePregnancyProfile(
        profileId: Int,
        startDate: String,
        dueDate: String,
        healthStatus: String?,
        notes: String?
    ) {
        _pregnancyState.value = PregnancyUiState.Loading

        viewModelScope.launch {
            val result = repository.updatePregnancyProfile(
                profileId = profileId,
                startDate = startDate,
                dueDate = dueDate,
                healthStatus = healthStatus,
                notes = notes
            )

            _pregnancyState.value = result.fold(
                onSuccess = { PregnancyUiState.Success(it) },
                onFailure = { PregnancyUiState.Error(it.message ?: "Modification impossible") }
            )
        }
    }
}

sealed class PregnancyUiState {
    object Loading : PregnancyUiState()
    data class Success(val profile: PregnancyProfileResponse) : PregnancyUiState()
    data class Error(val message: String) : PregnancyUiState()
}
