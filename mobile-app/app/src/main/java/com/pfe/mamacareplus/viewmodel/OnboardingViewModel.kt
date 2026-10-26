package com.pfe.mamacareplus.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.request.onboarding.OnboardingRequest
import com.pfe.mamacareplus.model.response.onboarding.OnboardingResponse
import com.pfe.mamacareplus.repository.onboarding.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = OnboardingRepository(application.applicationContext)

    private val _onboardingState = MutableStateFlow(OnboardingUiState())
    val onboardingState: StateFlow<OnboardingUiState> = _onboardingState

    fun completeOnboarding(request: OnboardingRequest) {
        _onboardingState.value = OnboardingUiState(isLoading = true)

        viewModelScope.launch {
            val result = repository.completeOnboarding(request)
            _onboardingState.value = result.fold(
                onSuccess = { response ->
                    Log.d("ONBOARDING", "Risk score = ${response.riskScore}")
                    Log.d("ONBOARDING", "Risk level = ${response.riskLevel}")
                    OnboardingUiState(
                        isLoading = false,
                        riskScore = response.riskScore ?: 0,
                        riskLevel = response.riskLevel ?: "Profil stable",
                        onboardingCompleted = response.onboardingCompleted == true,
                        showRiskResult = true,
                        response = response
                    )
                },
                onFailure = {
                    OnboardingUiState(
                        isLoading = false,
                        errorMessage = it.message ?: "Impossible de sauvegarder l'onboarding"
                    )
                }
            )
        }
    }

    fun resetState() {
        _onboardingState.value = OnboardingUiState()
    }
}

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val riskScore: Int? = null,
    val riskLevel: String? = null,
    val onboardingCompleted: Boolean = false,
    val showRiskResult: Boolean = false,
    val response: OnboardingResponse? = null
)
