package com.pfe.mamacareplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.response.pregnancy.BabyDevelopmentResponse
import com.pfe.mamacareplus.repository.pregnancy.BabyDevelopmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BabyDevelopmentViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = BabyDevelopmentRepository(application)

    private val _babyDevelopmentState =
        MutableStateFlow<BabyDevelopmentUiState>(BabyDevelopmentUiState.Idle)

    val babyDevelopmentState: StateFlow<BabyDevelopmentUiState> = _babyDevelopmentState

    fun loadDevelopmentByWeek(weekNumber: Int) {
        _babyDevelopmentState.value = BabyDevelopmentUiState.Loading

        viewModelScope.launch {
            val result = repository.getDevelopmentByWeek(weekNumber)

            _babyDevelopmentState.value = result.fold(
                onSuccess = { BabyDevelopmentUiState.Success(it) },
                onFailure = { BabyDevelopmentUiState.Error(it.message ?: "Impossible de charger le développement du bébé") }
            )
        }
    }
}

sealed class BabyDevelopmentUiState {
    object Idle : BabyDevelopmentUiState()
    object Loading : BabyDevelopmentUiState()
    data class Success(val development: BabyDevelopmentResponse) : BabyDevelopmentUiState()
    data class Error(val message: String) : BabyDevelopmentUiState()
}
