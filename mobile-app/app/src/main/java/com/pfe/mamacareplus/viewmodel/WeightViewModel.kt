package com.pfe.mamacareplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.response.weight.WeightRecordResponse
import com.pfe.mamacareplus.repository.weight.WeightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeightViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = WeightRepository(application)

    private val _weightState =
        MutableStateFlow<WeightUiState>(WeightUiState.Loading)

    val weightState: StateFlow<WeightUiState> = _weightState

    fun loadWeightRecords() {
        _weightState.value = WeightUiState.Loading

        viewModelScope.launch {
            val result = repository.getWeightRecords()

            _weightState.value = result.fold(
                onSuccess = { WeightUiState.Success(it) },
                onFailure = { WeightUiState.Error(it.message ?: "Impossible de charger les poids") }
            )
        }
    }

    fun createWeightRecord(
        weightKg: Double,
        bmi: Double?,
        recordDate: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val result = repository.createWeightRecord(
                weightKg = weightKg,
                bmi = bmi,
                recordDate = recordDate,
                notes = notes
            )

            result.onSuccess {
                loadWeightRecords()
            }.onFailure {
                _weightState.value =
                    WeightUiState.Error(it.message ?: "Impossible d'ajouter le poids")
            }
        }
    }

    fun deleteWeightRecord(weightRecordId: Int) {
        viewModelScope.launch {
            val result = repository.deleteWeightRecord(weightRecordId)

            result.onSuccess {
                loadWeightRecords()
            }.onFailure {
                _weightState.value =
                    WeightUiState.Error(it.message ?: "Impossible de supprimer le poids")
            }
        }
    }
}

sealed class WeightUiState {
    object Loading : WeightUiState()
    data class Success(val records: List<WeightRecordResponse>) : WeightUiState()
    data class Error(val message: String) : WeightUiState()
}
